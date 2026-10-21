package com.xianhuicode.medical_ai.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 token 请求。
 */
@Data
public class RefreshReq {

    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
