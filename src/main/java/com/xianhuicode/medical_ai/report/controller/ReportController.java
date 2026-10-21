package com.xianhuicode.medical_ai.report.controller;

import com.xianhuicode.medical_ai.common.api.Result;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import com.xianhuicode.medical_ai.report.dto.ReportDetailVO;
import com.xianhuicode.medical_ai.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 报告解读：上传报告图片/PDF → OCR + 规则解读；查询；删除。
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public Result<Long> upload(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(reportService.upload(userId, file));
    }

    @GetMapping("/{id}")
    public Result<ReportDetailVO> detail(@PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(reportService.detail(userId, id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        reportService.delete(userId, id);
        return Result.ok();
    }
}
