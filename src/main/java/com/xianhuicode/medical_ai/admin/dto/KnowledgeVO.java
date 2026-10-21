package com.xianhuicode.medical_ai.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识条目视图。
 */
@Data
public class KnowledgeVO {

    private Long id;

    private String title;

    private String content;

    private String source;

    private String category;

    private String status;

    private Integer version;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
