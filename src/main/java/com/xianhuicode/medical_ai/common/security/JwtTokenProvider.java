package com.xianhuicode.medical_ai.common.security;

import com.xianhuicode.medical_ai.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与解析。
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String phone, String role) {
        return build(userId, phone, role, properties.getAccessTokenExpireMs());
    }

    public String createRefreshToken(Long userId) {
        return build(userId, null, null, properties.getRefreshTokenExpireMs());
    }

    private String build(Long userId, String phone, String role, long expireMs) {
        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(key);
        if (phone != null) {
            builder.claim("phone", phone);
        }
        if (role != null) {
            builder.claim("role", role);
        }
        return builder.compact();
    }

    /** 解析并校验签名与有效期，非法或过期抛出 {@link io.jsonwebtoken.JwtException} */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
