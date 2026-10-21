package com.xianhuicode.medical_ai.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianhuicode.medical_ai.admin.dto.UserVO;
import com.xianhuicode.medical_ai.admin.service.AdminUserService;
import com.xianhuicode.medical_ai.auth.entity.SysUser;
import com.xianhuicode.medical_ai.auth.mapper.SysUserMapper;
import com.xianhuicode.medical_ai.common.api.PageVO;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.audit.AuditLog;
import com.xianhuicode.medical_ai.common.audit.AuditLogMapper;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户管理（ADMIN-04）：查询 + 禁用/解禁，操作留痕。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final SysUserMapper sysUserMapper;
    private final AuditLogMapper auditLogMapper;

    @Override
    public PageVO<UserVO> list(long page, long size, String phone, String nickname) {
        boolean hasPhone = phone != null && !phone.isBlank();
        boolean hasNickname = nickname != null && !nickname.isBlank();
        LambdaQueryWrapper<SysUser> qw = Wrappers.<SysUser>lambdaQuery()
                .like(hasPhone, SysUser::getPhone, phone)
                .like(hasNickname, SysUser::getNickname, nickname)
                .orderByDesc(SysUser::getId);
        Page<SysUser> p = sysUserMapper.selectPage(new Page<>(page, size), qw);
        return new PageVO<>(
                p.getRecords().stream().map(this::toVO).toList(),
                p.getTotal(), p.getCurrent(), p.getSize());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(ResultCode.BIZ_PARAM_ERROR, "状态仅支持 0/1");
        }
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.ADMIN_USER_NOT_FOUND);
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);

        AuditLog log = new AuditLog();
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setTargetType("USER");
        log.setTargetId(id);
        log.setAction(status == 1 ? "ENABLE" : "DISABLE");
        log.setDetail((status == 1 ? "解禁" : "禁用") + "用户 " + user.getPhone());
        auditLogMapper.insert(log);
    }

    private UserVO toVO(SysUser u) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setPhone(u.getPhone());
        vo.setNickname(u.getNickname());
        vo.setStatus(u.getStatus());
        vo.setRegisterType(u.getRegisterType());
        vo.setCreateTime(u.getCreateTime());
        return vo;
    }
}
