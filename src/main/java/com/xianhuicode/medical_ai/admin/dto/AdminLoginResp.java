package com.xianhuicode.medical_ai.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 管理员登录返回的令牌信息。
 */
@Data
@Builder
public class AdminLoginResp {

    private String accessToken;

    /** access token 有效期（秒） */
    private long expiresIn;

    private String username;

    private String role;
}
