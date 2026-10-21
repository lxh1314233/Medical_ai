package com.xianhuicode.medical_ai.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.admin.dto.AdminCreateReq;
import com.xianhuicode.medical_ai.admin.dto.AdminVO;
import com.xianhuicode.medical_ai.admin.entity.AdminUser;
import com.xianhuicode.medical_ai.admin.mapper.AdminUserMapper;
import com.xianhuicode.medical_ai.admin.service.AdminAccountService;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.audit.AuditLog;
import com.xianhuicode.medical_ai.common.audit.AuditLogMapper;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 管理员账号管理（ADMIN-05）：列表 + 创建（角色分级），仅超级管理员可操作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAccountServiceImpl implements AdminAccountService {

    private static final Set<String> ROLES = Set.of("SUPER_ADMIN", "OPERATOR", "AUDITOR");

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogMapper auditLogMapper;

    @Override
    public List<AdminVO> list() {
        return adminUserMapper.selectList(
                        Wrappers.<AdminUser>lambdaQuery().orderByAsc(AdminUser::getId))
                .stream().map(this::toVO).toList();
    }

    @Override
    public AdminVO create(AdminCreateReq req) {
        String role = req.getRole() == null ? null : req.getRole().trim().toUpperCase();
        if (role == null || !ROLES.contains(role)) {
            throw new BizException(ResultCode.ADMIN_ROLE_INVALID);
        }
        if (adminUserMapper.selectCount(
                Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getUsername, req.getUsername().trim())) > 0) {
            throw new BizException(ResultCode.ADMIN_USERNAME_EXISTS);
        }

        AdminUser admin = new AdminUser();
        admin.setUsername(req.getUsername().trim());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        admin.setRole(role);
        admin.setStatus(1);
        adminUserMapper.insert(admin);

        AuditLog log = new AuditLog();
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setTargetType("ADMIN");
        log.setTargetId(admin.getId());
        log.setAction("CREATE");
        log.setDetail("创建管理员 " + admin.getUsername() + "（" + role + "）");
        auditLogMapper.insert(log);
        return toVO(admin);
    }

    private AdminVO toVO(AdminUser a) {
        AdminVO vo = new AdminVO();
        vo.setId(a.getId());
        vo.setUsername(a.getUsername());
        vo.setRole(a.getRole());
        vo.setStatus(a.getStatus());
        vo.setCreateTime(a.getCreateTime());
        return vo;
    }
}
