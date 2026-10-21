package com.xianhuicode.medical_ai.medication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药物相互作用（对应 drug_interaction 表）。
 */
@Data
@TableName("drug_interaction")
public class DrugInteraction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String drugA;

    private String drugB;

    /** 高/中/低 */
    private String riskLevel;

    private String description;

    private LocalDateTime createTime;
}
