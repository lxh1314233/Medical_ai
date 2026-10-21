package com.xianhuicode.medical_ai.medication.controller;

import com.xianhuicode.medical_ai.common.api.Result;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import com.xianhuicode.medical_ai.medication.dto.DrugInfoVO;
import com.xianhuicode.medical_ai.medication.dto.InteractionReq;
import com.xianhuicode.medical_ai.medication.dto.InteractionResultVO;
import com.xianhuicode.medical_ai.medication.service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用药咨询：药品信息查询、药物相互作用检查。
 */
@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping("/{name}")
    public Result<DrugInfoVO> query(@PathVariable("name") String name) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(medicationService.query(userId, name));
    }

    @PostMapping("/interactions")
    public Result<InteractionResultVO> interactions(@Valid @RequestBody InteractionReq req) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(medicationService.checkInteractions(userId, req));
    }
}
