package com.xianhuicode.medical_ai.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检验指标值对照表（对应 lab_reference 表），报告解读规则引擎的数据源。
 */
@Data
@TableName("lab_reference")
public class LabReference {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 指标名（唯一） */
    private String itemName;

    /** 别名，逗号分隔，用于 OCR 文本匹配 */
    private String alias;

    private String unit;

    private Double refLow;

    private Double refHigh;

    private Double criticalLow;

    private Double criticalHigh;

    /** 指标含义 */
    private String meaning;

    /** 偏高常见原因 */
    private String highReason;

    /** 偏低常见原因 */
    private String lowReason;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
