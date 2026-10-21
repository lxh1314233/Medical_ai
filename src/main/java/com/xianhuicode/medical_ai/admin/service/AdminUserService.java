package com.xianhuicode.medical_ai.admin.service;

import com.xianhuicode.medical_ai.admin.dto.UserVO;
import com.xianhuicode.medical_ai.common.api.PageVO;

public interface AdminUserService {

    PageVO<UserVO> list(long page, long size, String phone, String nickname);

    void updateStatus(Long id, Integer status);
}
