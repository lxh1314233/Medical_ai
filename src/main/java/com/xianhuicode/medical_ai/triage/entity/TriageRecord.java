package com.xianhuicode.medical_ai.triage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 症状分诊记录（对应 triage_record 表）。
 */
@Data
@TableName("triage_record")
public class TriageRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 症状输入（结构化字段拼接后的完整文本） */
    private String symptomInput;

    /** 推荐科室（JSON 数组文本，如 ["呼吸内科","发热门诊"]） */
    private String departments;

    /** 紧急程度：立即就医 / 尽快就医 / 先观察 */
    private String urgencyLevel;

    /** 红旗标识：1 是 0 否 */
    private Integer redFlag;

    private String resultSummary;

    private LocalDateTime createTime;
}
