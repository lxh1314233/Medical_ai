package com.xianhuicode.medical_ai.triage.dto;

import lombok.Data;

import java.util.List;

/**
 * 症状分诊结果。
 * 命中红旗时 redFlag=true，仅返回 redFlagMessage；否则返回科室推荐等结构化字段。
 */
@Data
public class TriageVO {

    /** 是否命中红旗（急危重症） */
    private boolean redFlag;

    /** 红旗提示文案（命中时返回，引导 120/急诊） */
    private String redFlagMessage;

    /** 推荐科室（1~3 个） */
    private List<String> departments;

    /** 紧急程度：立即就医 / 尽快就医 / 先观察 */
    private String urgencyLevel;

    /** 推荐理由 */
    private List<String> reasons;

    /** 观察要点 */
    private String observationNotes;

    /** 免责声明（固定附加，TRIAGE-05） */
    private String disclaimer;
}
