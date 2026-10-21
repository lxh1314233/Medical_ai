package com.xianhuicode.medical_ai.chat.controller;

import com.xianhuicode.medical_ai.chat.dto.MessageVO;
import com.xianhuicode.medical_ai.chat.dto.RenameSessionReq;
import com.xianhuicode.medical_ai.chat.dto.SendMessageReq;
import com.xianhuicode.medical_ai.chat.dto.SessionVO;
import com.xianhuicode.medical_ai.chat.service.ChatService;
import com.xianhuicode.medical_ai.common.api.Result;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 智能问答：流式消息 + 会话管理。
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter send(@Valid @RequestBody SendMessageReq req) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (req.getSessionId() != null) {
            chatService.requireOwned(userId, req.getSessionId());
        }
        //创建一个服务端向客户端的发射器，超时时间设置为0
        SseEmitter emitter = new SseEmitter(0L);
        chatService.stream(userId, req, emitter);
        return emitter;
    }

    @GetMapping("/sessions")
    public Result<List<SessionVO>> sessions() {
        return Result.ok(chatService.listSessions(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/sessions/{id}/messages")
    public Result<List<MessageVO>> messages(@PathVariable Long id) {
        return Result.ok(chatService.listMessages(SecurityUtils.getCurrentUserId(), id));
    }

    @PutMapping("/sessions/{id}")
    public Result<Void> rename(@PathVariable Long id, @Valid @RequestBody RenameSessionReq req) {
        chatService.rename(SecurityUtils.getCurrentUserId(), id, req.getTitle());
        return Result.ok();
    }

    @DeleteMapping("/sessions/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        chatService.delete(SecurityUtils.getCurrentUserId(), id);
        return Result.ok();
    }
}
