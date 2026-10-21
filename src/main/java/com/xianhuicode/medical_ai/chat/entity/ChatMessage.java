package com.xianhuicode.medical_ai.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话消息。role: user/assistant；refs 存参考来源的 JSON 数组文本。
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    /** user / assistant */
    private String role;

    private String content;

    /** 参考来源 JSON 数组文本（[{title,source}]） */
    private String refs;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
