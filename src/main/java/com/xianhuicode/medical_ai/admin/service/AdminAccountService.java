package com.xianhuicode.medical_ai.admin.service;

import com.xianhuicode.medical_ai.admin.dto.AdminCreateReq;
import com.xianhuicode.medical_ai.admin.dto.AdminVO;

import java.util.List;

public interface AdminAccountService {

    List<AdminVO> list();

    AdminVO create(AdminCreateReq req);
}
