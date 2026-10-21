package com.xianhuicode.medical_ai.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核留痕视图。
 */
@Data
public class AuditLogVO {

    private Long id;

    private Long operatorId;

    private String targetType;

    private Long targetId;

    private String action;

    private String detail;

    private LocalDateTime createTime;
}
