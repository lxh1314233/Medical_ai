package com.xianhuicode.medical_ai.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.auth.entity.SysUser;
import com.xianhuicode.medical_ai.auth.mapper.SysUserMapper;
import com.xianhuicode.medical_ai.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 按手机号加载用户，供 Spring Security 密码认证使用。
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getPhone, phone));
        if (user == null) {
            throw new UsernameNotFoundException("账号不存在");
        }
        return UserPrincipal.fromUser(user.getId(), user.getPhone(), user.getPassword(), user.getStatus());
    }
}
