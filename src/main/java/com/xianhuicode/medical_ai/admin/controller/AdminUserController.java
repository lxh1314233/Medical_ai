package com.xianhuicode.medical_ai.admin.controller;

import com.xianhuicode.medical_ai.admin.dto.UserStatusReq;
import com.xianhuicode.medical_ai.admin.dto.UserVO;
import com.xianhuicode.medical_ai.admin.service.AdminUserService;
import com.xianhuicode.medical_ai.common.api.PageVO;
import com.xianhuicode.medical_ai.common.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理（ADMIN-04）。仅超级管理员/运营可操作。
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','OPERATOR')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Result<PageVO<UserVO>> list(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "20") long size,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) String nickname) {
        return Result.ok(adminUserService.list(page, size, phone, nickname));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusReq req) {
        adminUserService.updateStatus(id, req.getStatus());
        return Result.ok();
    }
}
