package com.xianhuicode.medical_ai.auth.service.impl;

import com.xianhuicode.medical_ai.auth.service.SmsService;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * 短信服务 mock 实现：验证码存 Redis，真实短信供应商接入前打印到日志。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private static final String KEY_PREFIX = "sms:code:";
    private static final long CODE_TTL_MINUTES = 5;
    private static final long SEND_INTERVAL_SECONDS = 60;

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void sendCode(String phone, String scene) {
        String key = KEY_PREFIX + scene + ":" + phone;
        String lockKey = key + ":lock";
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new BizException(ResultCode.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后再试");
        }
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(CODE_TTL_MINUTES));
        redisTemplate.opsForValue().set(lockKey, "1", Duration.ofSeconds(SEND_INTERVAL_SECONDS));
        log.info("[MOCK SMS] phone={}, scene={}, code={}", phone, scene, code);
    }

    @Override
    public void verifyOrThrow(String phone, String scene, String code) {
        if (code == null || code.isEmpty()) {
            throw new BizException(ResultCode.AUTH_VERIFY_CODE_ERROR);
        }
        String key = KEY_PREFIX + scene + ":" + phone;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached == null || !cached.equals(code)) {
            throw new BizException(ResultCode.AUTH_VERIFY_CODE_ERROR);
        }
        redisTemplate.delete(key);
    }
}
