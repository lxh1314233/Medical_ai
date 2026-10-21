package com.xianhuicode.medical_ai.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求。
 */
@Data
public class LoginReq {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** PASSWORD / SMS */
    @NotBlank(message = "登录类型不能为空")
    private String type;

    private String password;

    private String code;
}
