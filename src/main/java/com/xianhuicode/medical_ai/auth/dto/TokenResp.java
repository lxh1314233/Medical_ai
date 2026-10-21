package com.xianhuicode.medical_ai.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录/刷新返回的令牌信息。
 */
@Data
@Builder
public class TokenResp {

    private String accessToken;

    private String refreshToken;

    /** access token 有效期（秒） */
    private long expiresIn;

    private Long userId;

    private String phone;
}
