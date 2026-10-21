package com.xianhuicode.medical_ai.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表项。
 */
@Data
public class SessionVO {

    private Long id;

    private String title;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
