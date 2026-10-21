package com.xianhuicode.medical_ai.auth.controller;

import com.xianhuicode.medical_ai.auth.dto.LoginReq;
import com.xianhuicode.medical_ai.auth.dto.RefreshReq;
import com.xianhuicode.medical_ai.auth.dto.RegisterReq;
import com.xianhuicode.medical_ai.auth.dto.ResetPasswordReq;
import com.xianhuicode.medical_ai.auth.dto.SendCodeReq;
import com.xianhuicode.medical_ai.auth.dto.TokenResp;
import com.xianhuicode.medical_ai.auth.service.AuthService;
import com.xianhuicode.medical_ai.common.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：验证码、注册、登录、刷新、登出、找回密码。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeReq req) {
        authService.sendCode(req);
        return Result.ok();
    }

    @PostMapping("/register")
    public Result<TokenResp> register(@Valid @RequestBody RegisterReq req) {
        return Result.ok(authService.register(req));
    }


    @PostMapping("/login")
    public Result<TokenResp> login(@Valid @RequestBody LoginReq req) {
        return Result.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public Result<TokenResp> refresh(@Valid @RequestBody RefreshReq req) {
        return Result.ok(authService.refresh(req.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return Result.ok();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        authService.resetPassword(req);
        return Result.ok();
    }
}
