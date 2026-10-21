package com.xianhuicode.medical_ai.admin.dto;

import lombok.Data;

/**
 * 高频问题（ADMIN-03）。
 */
@Data
public class TopQuestionVO {

    private String content;

    private long count;
}
