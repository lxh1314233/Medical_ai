package com.xianhuicode.medical_ai.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建管理员请求。
 */
@Data
public class AdminCreateReq {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "角色不能为空")
    private String role;
}
