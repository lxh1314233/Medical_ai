package com.xianhuicode.medical_ai.feedback.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈评价记录（FB-03 反馈追踪）。
 */
@Data
public class FeedbackVO {

    private Long id;

    private Long messageId;

    private String type;

    private String content;

    /** PENDING / PROCESSED */
    private String status;

    private LocalDateTime createTime;
}
