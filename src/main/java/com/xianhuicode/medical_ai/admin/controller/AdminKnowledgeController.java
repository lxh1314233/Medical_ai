package com.xianhuicode.medical_ai.admin.controller;

import com.xianhuicode.medical_ai.admin.dto.KnowledgeReq;
import com.xianhuicode.medical_ai.admin.dto.KnowledgeVO;
import com.xianhuicode.medical_ai.admin.service.AdminKnowledgeService;
import com.xianhuicode.medical_ai.common.api.PageVO;
import com.xianhuicode.medical_ai.common.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库管理（ADMIN-01）。仅超级管理员/运营可操作。
 */
@RestController
@RequestMapping("/api/admin/knowledge")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','OPERATOR')")
public class AdminKnowledgeController {

    private final AdminKnowledgeService adminKnowledgeService;

    @PostMapping
    public Result<KnowledgeVO> create(@Valid @RequestBody KnowledgeReq req) {
        return Result.ok(adminKnowledgeService.create(req));
    }

    @PutMapping("/{id}")
    public Result<KnowledgeVO> update(@PathVariable Long id, @Valid @RequestBody KnowledgeReq req) {
        return Result.ok(adminKnowledgeService.update(id, req));
    }

    @PutMapping("/{id}/publish")
    public Result<KnowledgeVO> publish(@PathVariable Long id) {
        return Result.ok(adminKnowledgeService.publish(id));
    }

    @PutMapping("/{id}/offline")
    public Result<KnowledgeVO> offline(@PathVariable Long id) {
        return Result.ok(adminKnowledgeService.offline(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminKnowledgeService.delete(id);
        return Result.ok();
    }

    @GetMapping
    public Result<PageVO<KnowledgeVO>> list(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "20") long size,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) String keyword) {
        return Result.ok(adminKnowledgeService.list(page, size, status, category, keyword));
    }

    @GetMapping("/{id}")
    public Result<KnowledgeVO> detail(@PathVariable Long id) {
        return Result.ok(adminKnowledgeService.detail(id));
    }
}
