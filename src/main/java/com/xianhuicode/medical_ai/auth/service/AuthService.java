package com.xianhuicode.medical_ai.auth.service;

import com.xianhuicode.medical_ai.auth.dto.LoginReq;
import com.xianhuicode.medical_ai.auth.dto.RefreshReq;
import com.xianhuicode.medical_ai.auth.dto.RegisterReq;
import com.xianhuicode.medical_ai.auth.dto.ResetPasswordReq;
import com.xianhuicode.medical_ai.auth.dto.SendCodeReq;
import com.xianhuicode.medical_ai.auth.dto.TokenResp;

/**
 * 认证服务。
 */
public interface AuthService {

    void sendCode(SendCodeReq req);

    TokenResp register(RegisterReq req);

    TokenResp login(LoginReq req);

    TokenResp refresh(String refreshToken);

    void logout(String authorizationHeader);

    void resetPassword(ResetPasswordReq req);
}
