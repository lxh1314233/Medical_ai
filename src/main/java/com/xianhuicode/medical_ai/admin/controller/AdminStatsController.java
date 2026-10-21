package com.xianhuicode.medical_ai.admin.controller;

import com.xianhuicode.medical_ai.admin.dto.StatsOverviewVO;
import com.xianhuicode.medical_ai.admin.dto.TopQuestionVO;
import com.xianhuicode.medical_ai.admin.service.AdminStatsService;
import com.xianhuicode.medical_ai.common.api.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据统计（ADMIN-03）。所有管理员可看。
 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','OPERATOR','AUDITOR')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/overview")
    public Result<StatsOverviewVO> overview() {
        return Result.ok(adminStatsService.overview());
    }

    @GetMapping("/top-questions")
    public Result<List<TopQuestionVO>> topQuestions(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(adminStatsService.topQuestions(limit));
    }
}
