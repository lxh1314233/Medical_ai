package com.xianhuicode.medical_ai.admin.service;

import com.xianhuicode.medical_ai.admin.dto.AdminLoginReq;
import com.xianhuicode.medical_ai.admin.dto.AdminLoginResp;

public interface AdminAuthService {

    AdminLoginResp login(AdminLoginReq req);
}
