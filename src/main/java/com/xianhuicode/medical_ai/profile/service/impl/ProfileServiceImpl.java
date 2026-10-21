package com.xianhuicode.medical_ai.profile.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.common.util.CryptoUtil;
import com.xianhuicode.medical_ai.profile.dto.ProfileReq;
import com.xianhuicode.medical_ai.profile.dto.ProfileVO;
import com.xianhuicode.medical_ai.profile.entity.UserProfile;
import com.xianhuicode.medical_ai.profile.mapper.UserProfileMapper;
import com.xianhuicode.medical_ai.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 健康档案服务实现：敏感字段加密落库、读取解密、逻辑删除。
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserProfileMapper profileMapper;
    private final CryptoUtil cryptoUtil;

    @Override
    public ProfileVO get(Long userId) {
        UserProfile p = getByUserId(userId);
        if (p == null) {
            return new ProfileVO();
        }
        return toVO(p);
    }

    @Override
    public void update(Long userId, ProfileReq req) {
        UserProfile p = getByUserId(userId);
        boolean isNew = p == null;
        if (isNew) {
            p = new UserProfile();
            p.setUserId(userId);
        }
        p.setGender(req.getGender());
        p.setBirthDate(req.getBirthDate());
        p.setHeight(req.getHeight());
        p.setWeight(req.getWeight());
        p.setAllergyHistory(cryptoUtil.encrypt(req.getAllergyHistory()));
        p.setPastHistory(cryptoUtil.encrypt(req.getPastHistory()));
        p.setMedicationHistory(cryptoUtil.encrypt(req.getMedicationHistory()));
        p.setFamilyHistory(cryptoUtil.encrypt(req.getFamilyHistory()));
        if (isNew) {
            profileMapper.insert(p);
        } else {
            profileMapper.updateById(p);
        }
    }

    @Override
    public void delete(Long userId) {
        UserProfile p = getByUserId(userId);
        if (p != null) {
            profileMapper.deleteById(p.getId());
        }
    }

    private UserProfile getByUserId(Long userId) {
        return profileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, userId));
    }

    private ProfileVO toVO(UserProfile p) {
        ProfileVO vo = new ProfileVO();
        vo.setGender(p.getGender());
        vo.setBirthDate(p.getBirthDate());
        vo.setHeight(p.getHeight());
        vo.setWeight(p.getWeight());
        vo.setAllergyHistory(cryptoUtil.decrypt(p.getAllergyHistory()));
        vo.setPastHistory(cryptoUtil.decrypt(p.getPastHistory()));
        vo.setMedicationHistory(cryptoUtil.decrypt(p.getMedicationHistory()));
        vo.setFamilyHistory(cryptoUtil.decrypt(p.getFamilyHistory()));
        return vo;
    }
}
