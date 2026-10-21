package com.xianhuicode.medical_ai.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送消息请求。sessionId 为空时新建会话。
 */
@Data
public class SendMessageReq {

    /** 会话 id，空则新建 */
    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容过长")
    private String message;
}
