package com.xianhuicode.medical_ai.medication.dto;

import lombok.Data;

/**
 * 单条药物相互作用风险。
 */
@Data
public class InteractionVO {

    private String drugA;

    private String drugB;

    /** 高/中/低 */
    private String riskLevel;

    private String description;
}
