package com.xianhuicode.medical_ai.admin.controller;

import com.xianhuicode.medical_ai.admin.dto.AuditLogVO;
import com.xianhuicode.medical_ai.admin.dto.FeedbackReviewVO;
import com.xianhuicode.medical_ai.admin.service.AdminReviewService;
import com.xianhuicode.medical_ai.common.api.PageVO;
import com.xianhuicode.medical_ai.common.api.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容审核（ADMIN-02）：反馈审核 + 留痕查看。查看开放给所有管理员，处理仅超级管理员/运营。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','OPERATOR','AUDITOR')")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping("/feedbacks")
    public Result<PageVO<FeedbackReviewVO>> listFeedback(@RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "20") long size,
                                                         @RequestParam(required = false) String type,
                                                         @RequestParam(required = false) String status) {
        return Result.ok(adminReviewService.listFeedback(page, size, type, status));
    }

    @PutMapping("/feedbacks/{id}/process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','OPERATOR')")
    public Result<Void> process(@PathVariable Long id) {
        adminReviewService.processFeedback(id);
        return Result.ok();
    }

    @GetMapping("/audit-logs")
    public Result<PageVO<AuditLogVO>> auditLogs(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size) {
        return Result.ok(adminReviewService.listAuditLogs(page, size));
    }
}
