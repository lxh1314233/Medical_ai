package com.xianhuicode.medical_ai.profile.controller;

import com.xianhuicode.medical_ai.common.api.Result;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import com.xianhuicode.medical_ai.profile.dto.ProfileReq;
import com.xianhuicode.medical_ai.profile.dto.ProfileVO;
import com.xianhuicode.medical_ai.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康档案接口：查询、维护、删除（隐私删除入口）。
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public Result<ProfileVO> get() {
        return Result.ok(profileService.get(SecurityUtils.getCurrentUserId()));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody ProfileReq req) {
        profileService.update(SecurityUtils.getCurrentUserId(), req);
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> delete() {
        profileService.delete(SecurityUtils.getCurrentUserId());
        return Result.ok();
    }
}
