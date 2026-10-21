package com.xianhuicode.medical_ai.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员视图。
 */
@Data
public class AdminVO {

    private Long id;

    private String username;

    private String role;

    private Integer status;

    private LocalDateTime createTime;
}
