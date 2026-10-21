package com.xianhuicode.medical_ai.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送验证码请求。
 */
@Data
public class SendCodeReq {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "1[3-9]\\d{9}", message = "手机号格式不正确")
    private String phone;

    /** REGISTER / LOGIN / RESET_PASSWORD */
    @NotBlank(message = "场景不能为空")
    @Pattern(regexp = "REGISTER|LOGIN|RESET_PASSWORD", message = "场景不合法")
    private String scene;
}
