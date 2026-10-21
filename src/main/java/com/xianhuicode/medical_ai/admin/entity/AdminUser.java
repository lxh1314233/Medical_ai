package com.xianhuicode.medical_ai.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员（对应 admin_user 表）。role: SUPER_ADMIN/OPERATOR/AUDITOR。
 */
@Data
@TableName("admin_user")
public class AdminUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 密文 */
    private String password;

    /** SUPER_ADMIN / OPERATOR / AUDITOR */
    private String role;

    /** 1 正常 0 禁用 */
    private Integer status;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
