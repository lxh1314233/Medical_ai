package com.xianhuicode.medical_ai.chat.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.ai.guard.Guard;
import com.xianhuicode.medical_ai.ai.guard.GuardResult;
import com.xianhuicode.medical_ai.ai.llm.LlmGateway;
import com.xianhuicode.medical_ai.ai.rag.RagService;
import com.xianhuicode.medical_ai.chat.dto.MessageVO;
import com.xianhuicode.medical_ai.chat.dto.RefVO;
import com.xianhuicode.medical_ai.chat.dto.SendMessageReq;
import com.xianhuicode.medical_ai.chat.dto.SessionVO;
import com.xianhuicode.medical_ai.chat.entity.ChatMessage;
import com.xianhuicode.medical_ai.chat.entity.ChatSession;
import com.xianhuicode.medical_ai.chat.mapper.ChatMessageMapper;
import com.xianhuicode.medical_ai.chat.mapper.ChatSessionMapper;
import com.xianhuicode.medical_ai.chat.service.ChatService;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.audit.AuditLog;
import com.xianhuicode.medical_ai.common.audit.AuditLogMapper;
import com.xianhuicode.medical_ai.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * 智能问答编排实现：安全拦截 → RAG 检索 → 模型流式生成 → 落库 → 溯源。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final int HISTORY_LIMIT = 10;
    private static final int TOP_K = 3;

    private static final String DISCLAIMER =
            "\n\n---\n*以上内容为健康科普参考，不构成诊断或治疗建议，不能替代执业医师的面诊。如有不适请及时就医。*";

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final AuditLogMapper auditLogMapper;
    private final LlmGateway llmGateway;
    private final RagService ragService;
    private final List<Guard> guards;
    private final ObjectMapper objectMapper;
    @Qualifier("chatExecutor")
    private final Executor chatExecutor;

    @Override
    public void stream(Long userId, SendMessageReq req, SseEmitter emitter) {
        chatExecutor.execute(() -> doStream(userId, req, emitter));
    }

    @Override
    public void requireOwned(Long userId, Long sessionId) {
        getOwnedSession(userId, sessionId);
    }

    @Override
    public List<SessionVO> listSessions(Long userId) {
        return chatSessionMapper.selectList(
                        Wrappers.<ChatSession>lambdaQuery()
                                .eq(ChatSession::getUserId, userId)
                                .orderByDesc(ChatSession::getUpdateTime))
                .stream().map(this::toSessionVO).toList();
    }

    @Override
    public List<MessageVO> listMessages(Long userId, Long sessionId) {
        getOwnedSession(userId, sessionId);
        return chatMessageMapper.selectList(
                        Wrappers.<ChatMessage>lambdaQuery()
                                .eq(ChatMessage::getSessionId, sessionId)
                                .orderByAsc(ChatMessage::getId))
                .stream().map(this::toMessageVO).toList();
    }

    @Override
    public void rename(Long userId, Long sessionId, String title) {
        ChatSession session = getOwnedSession(userId, sessionId);
        session.setTitle(title);
        chatSessionMapper.updateById(session);
    }

    @Override
    public void delete(Long userId, Long sessionId) {
        getOwnedSession(userId, sessionId);
        chatMessageMapper.delete(
                Wrappers.<ChatMessage>lambdaQuery().eq(ChatMessage::getSessionId, sessionId));
        chatSessionMapper.deleteById(sessionId);
    }

    // ==================== 流式编排 ====================

    private void doStream(Long userId, SendMessageReq req, SseEmitter emitter) {
        try {
            //1. 构建一个 session对象
            ChatSession session = resolveSession(userId, req.getSessionId(), req.getMessage());
            //2. 返回当前用户的最近10条信息
            List<ChatMessage> history = loadHistory(session.getId(), HISTORY_LIMIT);
            //3. 将最近的一条消息保存
            saveUserMessage(session.getId(), req.getMessage());
            //4.
            Optional<GuardResult> guardHit = guards.stream()
                    //将guard  转换为optional<guard>
                    .map(g -> g.check(req.getMessage()))
                    //过滤空
                    .filter(Optional::isPresent)
                    //提取非空值并转换为guardresult
                    .map(Optional::get)
                    .findFirst();
            //守卫命中后的统一处理
            if (guardHit.isPresent()) {
                String reply = guardHit.get().reply();
                ChatMessage assistant = saveAssistant(session.getId(), reply, List.of());
                recordGuardHit(userId, assistant.getId(), req.getMessage());
                emitDelta(emitter, reply);
                emitDone(emitter, session.getId(), assistant.getId(), List.of());
                emitter.complete();
                return;
            }
            //正常情况下，用户提问处理流程
            //1. 检查配置是否正确
            //2. 将大模型实例化（有锁）每个线程只有一个大模型实例，将用户提问向量化  返回向量供大模型检索
            //3.  search方法将向量数据库的相似度最接近的top_k个结果返回
            List<RagService.KnowledgeHit> hits = ragService.search(userId, req.getMessage(), TOP_K);
            //将该会话的10条最近用户提问，以及返回的向量，和这次请求的用户提问聚合在一起
            List<LlmGateway.LlmMessage> messages = buildMessages(history, hits, req.getMessage());

            StringBuilder full = new StringBuilder();
            //第二个参数Consumer<String> onToken  用于自定义每个大模型返回的token
            //此代码是异步执行的，只要有结果就立即返回，但响应可能还没有结束需要一个线程等待工具CountDownLatch latch = new CountDownLatch(1);
            // 执行回调链  StreamingChatResponseHandler.onPartialResponse-> onToken.accept(token)
            llmGateway.stream(messages, token -> {
                full.append(token);//将响应累加
                emitDelta(emitter, token);//将大模型的响应实时推送到前端
            });

            full.append(DISCLAIMER);
            emitDelta(emitter, DISCLAIMER);

            List<RefVO> refs = toRefs(hits);
            ChatMessage assistant = saveAssistant(session.getId(), full.toString(), refs);
            emitDone(emitter, session.getId(), assistant.getId(), refs);
            emitter.complete();
        } catch (StreamAbortedException e) {
            log.debug("客户端断开，终止流式输出");
        } catch (Exception e) {
            log.error("对话流式处理失败: userId={}", userId, e);
            try {
                emitError(emitter, "服务异常，请稍后再试");
            } catch (Exception ignored) {
            }
            emitter.completeWithError(e);
        }
    }

    private ChatSession resolveSession(Long userId, Long sessionId, String message) {
        if (sessionId == null) {
            ChatSession session = new ChatSession();
            session.setUserId(userId);
            session.setTitle(titleOf(message));
            chatSessionMapper.insert(session);
            return session;
        }
        return getOwnedSession(userId, sessionId);
    }

    private ChatSession getOwnedSession(Long userId, Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BizException(ResultCode.CHAT_SESSION_NOT_FOUND);
        }
        if (!userId.equals(session.getUserId())) {
            throw new BizException(ResultCode.CHAT_SESSION_NOT_OWNED);
        }
        return session;
    }

    private String titleOf(String message) {
        String clean = message == null ? "" : message.replaceAll("\\s+", " ").trim();
        if (clean.isEmpty()) {
            return "新会话";
        }
        return clean.length() <= 20 ? clean : clean.substring(0, 20);
    }

    private List<ChatMessage> loadHistory(Long sessionId, int limit) {
        List<ChatMessage> list = chatMessageMapper.selectList(
                Wrappers.<ChatMessage>lambdaQuery()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByDesc(ChatMessage::getId)
                        .last("LIMIT " + limit));
        //反转消息队列
        Collections.reverse(list);
        return list;
    }

    private void saveUserMessage(Long sessionId, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole("user");
        message.setContent(content);
        chatMessageMapper.insert(message);
    }

    private ChatMessage saveAssistant(Long sessionId, String content, List<RefVO> refs) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole("assistant");
        message.setContent(content);
        message.setRefs(refs.isEmpty() ? null : toJson(refs));
        chatMessageMapper.insert(message);
        return message;
    }

    /** 守卫命中即高风险问答，写入审核留痕进入待审队列（SYS-07）。 */
    private void recordGuardHit(Long userId, Long messageId, String question) {
        AuditLog log = new AuditLog();
        log.setOperatorId(userId);
        log.setTargetType("CHAT");
        log.setTargetId(messageId);
        log.setAction("GUARD_HIT");
        log.setDetail(question);
        auditLogMapper.insert(log);
    }

    private List<LlmGateway.LlmMessage> buildMessages(List<ChatMessage> history,
                                                      List<RagService.KnowledgeHit> hits,
                                                      String question) {
        List<LlmGateway.LlmMessage> messages = new ArrayList<>();
        messages.add(new LlmGateway.LlmMessage("system", buildSystemPrompt(hits)));
        for (ChatMessage h : history) {
            messages.add(new LlmGateway.LlmMessage(h.getRole(), h.getContent()));
        }
        messages.add(new LlmGateway.LlmMessage("user", question));
        return messages;
    }

    private String buildSystemPrompt(List<RagService.KnowledgeHit> hits) {
        StringBuilder sb = new StringBuilder(
                "你是一名严谨的医疗健康科普助手，仅提供健康知识科普，不得诊断疾病、开具处方或推荐药品品牌。回答需通俗易懂、结构清晰。");
        if (!hits.isEmpty()) {
            sb.append("以下是知识库检索到的参考资料，请优先基于它们回答并保持准确：\n");
            for (int i = 0; i < hits.size(); i++) {
                RagService.KnowledgeHit h = hits.get(i);
                sb.append("[").append(i + 1).append("] ").append(h.title()).append("：").append(h.content()).append("\n");
            }
        }
        return sb.toString();
    }

    // ==================== SSE 事件 ====================

    private void emitDelta(SseEmitter emitter, String token) {
        sendEvent(emitter, Map.of("type", "delta", "content", token));
    }

    private void emitDone(SseEmitter emitter, Long sessionId, Long messageId, List<RefVO> refs) {
        sendEvent(emitter, Map.of(
                "type", "done",
                "sessionId", sessionId,
                "messageId", messageId,
                "refs", refs));
    }

    private void emitError(SseEmitter emitter, String message) {
        sendEvent(emitter, Map.of("type", "error", "message", message));
    }

    private void sendEvent(SseEmitter emitter, Map<String, Object> event) {
        try {
            emitter.send(SseEmitter.event().data(toJson(event)));
        } catch (IOException e) {
            throw new StreamAbortedException(e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("JSON 序列化失败", e);
            return "{}";
        }
    }

    // ==================== 转换 ====================

    private SessionVO toSessionVO(ChatSession s) {
        SessionVO vo = new SessionVO();
        vo.setId(s.getId());
        vo.setTitle(s.getTitle());
        vo.setCreateTime(s.getCreateTime());
        vo.setUpdateTime(s.getUpdateTime());
        return vo;
    }

    private MessageVO toMessageVO(ChatMessage m) {
        MessageVO vo = new MessageVO();
        vo.setId(m.getId());
        vo.setRole(m.getRole());
        vo.setContent(m.getContent());
        vo.setRefs(parseRefs(m.getRefs()));
        vo.setCreateTime(m.getCreateTime());
        return vo;
    }

    private List<RefVO> toRefs(List<RagService.KnowledgeHit> hits) {
        return hits.stream().map(h -> new RefVO(h.title(), h.source())).toList();
    }

    private List<RefVO> parseRefs(String refsJson) {
        if (refsJson == null || refsJson.isBlank()) {
            return List.of();
        }
        try {
            RefVO[] arr = objectMapper.readValue(refsJson, RefVO[].class);
            return List.of(arr);
        } catch (Exception e) {
            log.warn("解析 refs 失败: {}", refsJson);
            return List.of();
        }
    }

    /** 客户端断开导致流式终止的信号。 */
    private static final class StreamAbortedException extends RuntimeException {
        StreamAbortedException(IOException cause) {
            super(cause);
        }
    }
}
