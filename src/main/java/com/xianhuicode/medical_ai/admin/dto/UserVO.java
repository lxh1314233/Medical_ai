package com.xianhuicode.medical_ai.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图（后台管理）。
 */
@Data
public class UserVO {

    private Long id;

    private String phone;

    private String nickname;

    /** 1 正常 0 禁用 */
    private Integer status;

    private String registerType;

    private LocalDateTime createTime;
}
