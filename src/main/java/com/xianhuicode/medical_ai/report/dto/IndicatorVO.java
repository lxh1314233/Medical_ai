package com.xianhuicode.medical_ai.report.dto;

import lombok.Data;

/**
 * 单个检验指标的解读结果。
 */
@Data
public class IndicatorVO {

    /** 指标名 */
    private String item;

    /** 原始数值（OCR 原样） */
    private String value;

    /** 单位 */
    private String unit;

    /** 参考范围（如 "3.5~5.3 mmol/L"） */
    private String refRange;

    /** 分级：正常 / 轻度 / 显著 / 危急 / 未收录 / 无法判断 */
    private String level;

    /** 指标含义 */
    private String meaning;

    /** 偏高/偏低原因或说明（正常时为 null） */
    private String reason;
}
