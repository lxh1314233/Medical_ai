package com.xianhuicode.medical_ai.triage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 症状分诊请求：结构化表单 + 自由文本。
 */
@Data
public class TriageReq {

    /** 主诉症状（必填） */
    @NotBlank(message = "请描述主要症状")
    private String chiefComplaint;

    /** 持续时间，如「3 天」 */
    private String duration;

    /** 严重程度，如「轻微 / 中等 / 严重」 */
    private String severity;

    /** 伴随症状 */
    private String accompanyingSymptoms;

    /** 既往史 */
    private String pastHistory;
}
