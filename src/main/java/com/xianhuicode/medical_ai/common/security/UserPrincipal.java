package com.xianhuicode.medical_ai.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 当前登录用户主体，实现 {@link UserDetails} 供 Spring Security 与 JWT 过滤器复用。
 */
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String phone;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, String phone, String password, boolean enabled,
                         Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.phone = phone;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    /** 由 JWT 声明构造（无状态，密码为空） */
    public static UserPrincipal fromClaims(Long userId, String phone, String role) {
        String r = (role == null || role.isEmpty()) ? "USER" : role;
        List<GrantedAuthority> as = List.of(new SimpleGrantedAuthority("ROLE_" + r));
        return new UserPrincipal(userId, phone, "", true, as);
    }

    /** 由数据库用户构造（用于密码登录） */
    public static UserPrincipal fromUser(Long userId, String phone, String password, int status) {
        List<GrantedAuthority> as = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new UserPrincipal(userId, phone, password, status == 1, as);
    }

    public Long getUserId() {
        return userId;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return phone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
