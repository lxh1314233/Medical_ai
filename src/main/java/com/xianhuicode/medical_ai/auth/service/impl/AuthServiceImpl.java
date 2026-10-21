package com.xianhuicode.medical_ai.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.auth.dto.LoginReq;
import com.xianhuicode.medical_ai.auth.dto.RefreshReq;
import com.xianhuicode.medical_ai.auth.dto.RegisterReq;
import com.xianhuicode.medical_ai.auth.dto.ResetPasswordReq;
import com.xianhuicode.medical_ai.auth.dto.SendCodeReq;
import com.xianhuicode.medical_ai.auth.dto.TokenResp;
import com.xianhuicode.medical_ai.auth.entity.SysUser;
import com.xianhuicode.medical_ai.auth.mapper.SysUserMapper;
import com.xianhuicode.medical_ai.auth.service.AuthService;
import com.xianhuicode.medical_ai.auth.service.SmsService;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.config.JwtProperties;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.common.security.JwtTokenProvider;
import com.xianhuicode.medical_ai.common.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * 认证服务实现：注册、密码/验证码登录、刷新、登出、找回密码。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String ROLE_USER = "USER";

    private final SysUserMapper sysUserMapper;
    private final SmsService smsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void sendCode(SendCodeReq req) {
        smsService.sendCode(req.getPhone(), req.getScene());
    }

    @Override
    public TokenResp register(RegisterReq req) {
        if (existsByPhone(req.getPhone())) {
            throw new BizException(ResultCode.AUTH_PHONE_EXISTS);
        }
        smsService.verifyOrThrow(req.getPhone(), "REGISTER", req.getCode());

        SysUser user = new SysUser();
        user.setPhone(req.getPhone());
        user.setNickname(req.getNickname());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setStatus(1);
        user.setRegisterType("PASSWORD");
        sysUserMapper.insert(user);
        log.info("用户注册成功: phone={}", req.getPhone());
        return buildTokenResp(user);
    }

    @Override
    public TokenResp login(LoginReq req) {
        if ("PASSWORD".equals(req.getType())) {
            try {
                Authentication auth = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(req.getPhone(), req.getPassword()));
                UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
                return buildTokenResp(principal.getUserId(), principal.getPhone());
            } catch (AuthenticationException e) {
                throw new BizException(ResultCode.AUTH_PASSWORD_ERROR);
            }
        }
        if ("SMS".equals(req.getType())) {
            smsService.verifyOrThrow(req.getPhone(), "LOGIN", req.getCode());
            SysUser user = getByPhone(req.getPhone());
            if (user == null) {
                throw new BizException(ResultCode.AUTH_ACCOUNT_NOT_FOUND);
            }
            if (user.getStatus() != 1) {
                throw new BizException(ResultCode.AUTH_ACCOUNT_DISABLED);
            }
            return buildTokenResp(user);
        }
        throw new BizException(ResultCode.BIZ_PARAM_ERROR, "登录类型不合法");
    }

    @Override
    public TokenResp refresh(String refreshToken) {
        Claims claims = jwtTokenProvider.parse(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BizException(ResultCode.AUTH_ACCOUNT_DISABLED);
        }
        return buildTokenResp(user);
    }

    @Override
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authorizationHeader.substring(7);
        try {
            Claims claims = jwtTokenProvider.parse(token);
            Date expiration = claims.getExpiration();
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "1", Duration.ofMillis(ttl));
            }
        } catch (Exception e) {
            log.debug("登出时 token 解析失败: {}", e.getMessage());
        }
    }

    @Override
    public void resetPassword(ResetPasswordReq req) {
        smsService.verifyOrThrow(req.getPhone(), "RESET_PASSWORD", req.getCode());
        SysUser user = getByPhone(req.getPhone());
        if (user == null) {
            throw new BizException(ResultCode.AUTH_ACCOUNT_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    private boolean existsByPhone(String phone) {
        return sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getPhone, phone)) > 0;
    }

    private SysUser getByPhone(String phone) {
        return sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getPhone, phone));
    }

    private TokenResp buildTokenResp(SysUser user) {
        return buildTokenResp(user.getId(), user.getPhone());
    }

    private TokenResp buildTokenResp(Long userId, String phone) {
        String accessToken = jwtTokenProvider.createAccessToken(userId, phone, ROLE_USER);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        return TokenResp.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getAccessTokenExpireMs() / 1000)
                .userId(userId)
                .phone(phone)
                .build();
    }
}
