package com.xianhuicode.medical_ai.profile.service;

import com.xianhuicode.medical_ai.profile.dto.ProfileReq;
import com.xianhuicode.medical_ai.profile.dto.ProfileVO;

/**
 * 健康档案服务。
 */
public interface ProfileService {

    ProfileVO get(Long userId);

    void update(Long userId, ProfileReq req);

    void delete(Long userId);
}
