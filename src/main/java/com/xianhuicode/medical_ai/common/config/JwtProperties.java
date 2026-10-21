package com.xianhuicode.medical_ai.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 签名密钥，HMAC-SHA256 要求 ≥ 32 字节 */
    private String secret;

    /** access token 有效期（毫秒），默认 2 小时 */
    private long accessTokenExpireMs = 2 * 60 * 60 * 1000L;

    /** refresh token 有效期（毫秒），默认 7 天 */
    private long refreshTokenExpireMs = 7 * 24 * 60 * 60 * 1000L;
}
