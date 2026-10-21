package com.xianhuicode.medical_ai.admin.controller;

import com.xianhuicode.medical_ai.admin.dto.AdminCreateReq;
import com.xianhuicode.medical_ai.admin.dto.AdminVO;
import com.xianhuicode.medical_ai.admin.service.AdminAccountService;
import com.xianhuicode.medical_ai.common.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员账号管理（ADMIN-05）。仅超级管理员可操作。
 */
@RestController
@RequestMapping("/api/admin/admins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @GetMapping
    public Result<List<AdminVO>> list() {
        return Result.ok(adminAccountService.list());
    }

    @PostMapping
    public Result<AdminVO> create(@Valid @RequestBody AdminCreateReq req) {
        return Result.ok(adminAccountService.create(req));
    }
}
