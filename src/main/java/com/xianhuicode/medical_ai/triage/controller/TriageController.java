package com.xianhuicode.medical_ai.triage.controller;

import com.xianhuicode.medical_ai.common.api.Result;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import com.xianhuicode.medical_ai.triage.dto.TriageReq;
import com.xianhuicode.medical_ai.triage.dto.TriageVO;
import com.xianhuicode.medical_ai.triage.service.TriageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 症状分诊（导诊）。
 */
@RestController
@RequestMapping("/api/triage")
@RequiredArgsConstructor
public class TriageController {

    private final TriageService triageService;

    @PostMapping
    public Result<TriageVO> triage(@Valid @RequestBody TriageReq req) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(triageService.triage(userId, req));
    }
}
