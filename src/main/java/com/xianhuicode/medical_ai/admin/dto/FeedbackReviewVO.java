package com.xianhuicode.medical_ai.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈审核队列视图（含提交用户）。
 */
@Data
public class FeedbackReviewVO {

    private Long id;

    private Long userId;

    private Long messageId;

    private String type;

    private String content;

    private String status;

    private LocalDateTime createTime;
}
