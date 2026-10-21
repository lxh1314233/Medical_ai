package com.xianhuicode.medical_ai.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求。
 */
@Data
public class RegisterReq {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "1[3-9]\\d{9}", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度须在 6-20 位之间")
    private String password;

    private String nickname;
}
