package com.xianhuicode.medical_ai.feedback.controller;

import com.xianhuicode.medical_ai.common.api.Result;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import com.xianhuicode.medical_ai.feedback.dto.FeedbackReq;
import com.xianhuicode.medical_ai.feedback.dto.FeedbackVO;
import com.xianhuicode.medical_ai.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 反馈评价：提交评价/纠错（FB-01/02）、查看处理状态（FB-03）。
 */
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public Result<FeedbackVO> submit(@Valid @RequestBody FeedbackReq req) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(feedbackService.submit(userId, req));
    }

    @GetMapping
    public Result<List<FeedbackVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(feedbackService.list(userId));
    }
}
