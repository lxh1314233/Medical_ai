package com.xianhuicode.medical_ai.chat.service;

import com.xianhuicode.medical_ai.chat.dto.MessageVO;
import com.xianhuicode.medical_ai.chat.dto.SendMessageReq;
import com.xianhuicode.medical_ai.chat.dto.SessionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 智能问答：会话管理 + 流式对话编排。
 */
public interface ChatService {

    /** 异步流式处理一条消息，经 SseEmitter 推送增量内容。 */
    void stream(Long userId, SendMessageReq req, SseEmitter emitter);

    /** 校验会话存在且属于当前用户（否则抛 404/403）。 */
    void requireOwned(Long userId, Long sessionId);

    List<SessionVO> listSessions(Long userId);

    List<MessageVO> listMessages(Long userId, Long sessionId);

    void rename(Long userId, Long sessionId, String title);

    void delete(Long userId, Long sessionId);
}
