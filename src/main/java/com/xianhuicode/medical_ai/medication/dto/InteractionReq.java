package com.xianhuicode.medical_ai.medication.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 药物相互作用检查请求：当前服用/拟服用的药品名列表。
 */
@Data
public class InteractionReq {

    @NotEmpty(message = "请至少填写一种药品")
    private List<String> drugs;
}
