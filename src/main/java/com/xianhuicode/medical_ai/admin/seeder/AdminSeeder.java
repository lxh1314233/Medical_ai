package com.xianhuicode.medical_ai.admin.seeder;

import com.xianhuicode.medical_ai.admin.entity.AdminUser;
import com.xianhuicode.medical_ai.admin.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时初始化默认超级管理员（admin_user 为空时）。密码用 BCrypt 加密，无法用 SQL 种子硬编码。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.seed.username:admin}")
    private String defaultUsername;

    @Value("${admin.seed.password:admin123}")
    private String defaultPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserMapper.selectCount(null) > 0) {
            return;
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(defaultUsername);
        admin.setPassword(passwordEncoder.encode(defaultPassword));
        admin.setRole("SUPER_ADMIN");
        admin.setStatus(1);
        adminUserMapper.insert(admin);
        log.info("已初始化默认超级管理员: username={}（生产环境请通过 admin.seed.password 覆盖强密码）", defaultUsername);
    }
}
