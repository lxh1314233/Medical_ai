package com.xianhuicode.medical_ai.feedback.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.chat.entity.ChatMessage;
import com.xianhuicode.medical_ai.chat.entity.ChatSession;
import com.xianhuicode.medical_ai.chat.mapper.ChatMessageMapper;
import com.xianhuicode.medical_ai.chat.mapper.ChatSessionMapper;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.common.audit.AuditLog;
import com.xianhuicode.medical_ai.common.audit.AuditLogMapper;
import com.xianhuicode.medical_ai.feedback.dto.FeedbackReq;
import com.xianhuicode.medical_ai.feedback.dto.FeedbackVO;
import com.xianhuicode.medical_ai.feedback.entity.Feedback;
import com.xianhuicode.medical_ai.feedback.mapper.FeedbackMapper;
import com.xianhuicode.medical_ai.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 反馈评价实现：评价/纠错落库；纠错写入 audit_log 待审队列（SYS-07）；可查处理状态（FB-03）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private static final Set<String> EVALUATION_TYPES = Set.of("HELPFUL", "NOT_HELPFUL");

    private final FeedbackMapper feedbackMapper;
    private final AuditLogMapper auditLogMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionMapper chatSessionMapper;

    @Override
    public FeedbackVO submit(Long userId, FeedbackReq req) {
        String type = req.getType();
        Long messageId = req.getMessageId();
        String content = req.getContent() == null ? "" : req.getContent().trim();

        if ("CORRECTION".equals(type)) {
            if (content.isEmpty()) {
                throw new BizException(ResultCode.BIZ_PARAM_ERROR, "纠错反馈需填写说明");
            }
        } else if (EVALUATION_TYPES.contains(type)) {
            if (messageId == null) {
                throw new BizException(ResultCode.BIZ_PARAM_ERROR, "评价需指定回答");
            }
        }

        if (messageId != null) {
            verifyMessageOwned(userId, messageId);
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setMessageId(messageId);
        feedback.setType(type);
        feedback.setContent(content.isEmpty() ? null : content);
        feedback.setStatus("PENDING");
        feedbackMapper.insert(feedback);

        if ("CORRECTION".equals(type)) {
            AuditLog log = new AuditLog();
            log.setTargetType("FEEDBACK");
            log.setTargetId(feedback.getId());
            log.setAction("SUBMIT");
            log.setDetail(content);
            auditLogMapper.insert(log);
        }

        return toVO(feedback);
    }

    @Override
    public List<FeedbackVO> list(Long userId) {
        return feedbackMapper.selectList(
                        Wrappers.<Feedback>lambdaQuery()
                                .eq(Feedback::getUserId, userId)
                                .orderByDesc(Feedback::getId))
                .stream().map(this::toVO).toList();
    }

    // ==================== 归属校验 ====================

    private void verifyMessageOwned(Long userId, Long messageId) {
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BizException(ResultCode.FEEDBACK_MESSAGE_NOT_FOUND);
        }
        ChatSession session = chatSessionMapper.selectById(message.getSessionId());
        if (session == null || !userId.equals(session.getUserId())) {
            throw new BizException(ResultCode.FEEDBACK_MESSAGE_NOT_OWNED);
        }
    }

    private FeedbackVO toVO(Feedback f) {
        FeedbackVO vo = new FeedbackVO();
        vo.setId(f.getId());
        vo.setMessageId(f.getMessageId());
        vo.setType(f.getType());
        vo.setContent(f.getContent());
        vo.setStatus(f.getStatus());
        vo.setCreateTime(f.getCreateTime());
        return vo;
    }
}
