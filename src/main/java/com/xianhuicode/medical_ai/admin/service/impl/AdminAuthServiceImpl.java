package com.xianhuicode.medical_ai.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.admin.dto.AdminLoginReq;
import com.xianhuicode.medical_ai.admin.dto.AdminLoginResp;
import com.xianhuicode.medical_ai.admin.entity.AdminUser;
import com.xianhuicode.medical_ai.admin.mapper.AdminUserMapper;
import com.xianhuicode.medical_ai.admin.service.AdminAuthService;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.config.JwtProperties;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理员登录：校验账号/密码/状态，签发带角色声明的 JWT（复用患者链路，role=SUPER_ADMIN/OPERATOR/AUDITOR）。
 */
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    public AdminLoginResp login(AdminLoginReq req) {
        AdminUser admin = adminUserMapper.selectOne(
                Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getUsername, req.getUsername()));
        if (admin == null) {
            throw new BizException(ResultCode.ADMIN_ACCOUNT_NOT_FOUND);
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BizException(ResultCode.ADMIN_ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            throw new BizException(ResultCode.ADMIN_PASSWORD_ERROR);
        }
        String token = jwtTokenProvider.createAccessToken(admin.getId(), admin.getUsername(), admin.getRole());
        return AdminLoginResp.builder()
                .accessToken(token)
                .expiresIn(jwtProperties.getAccessTokenExpireMs() / 1000)
                .username(admin.getUsername())
                .role(admin.getRole())
                .build();
    }
}
