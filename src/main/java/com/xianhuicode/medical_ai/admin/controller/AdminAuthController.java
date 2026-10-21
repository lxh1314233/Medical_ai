package com.xianhuicode.medical_ai.admin.controller;

import com.xianhuicode.medical_ai.admin.dto.AdminLoginReq;
import com.xianhuicode.medical_ai.admin.dto.AdminLoginResp;
import com.xianhuicode.medical_ai.admin.service.AdminAuthService;
import com.xianhuicode.medical_ai.common.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员登录（ADMIN-05）。该路径在 SecurityConfig 中 permitAll。
 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<AdminLoginResp> login(@Valid @RequestBody AdminLoginReq req) {
        return Result.ok(adminAuthService.login(req));
    }
}
