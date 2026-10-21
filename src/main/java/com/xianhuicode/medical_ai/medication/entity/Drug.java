package com.xianhuicode.medical_ai.medication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药品药典（结构化，对应 drug 表）。
 */
@Data
@TableName("drug")
public class Drug {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 通用名 */
    private String genericName;

    /** 商品名，逗号分隔 */
    private String brandNames;

    /** 适应症 */
    private String indications;

    /** 用法用量 */
    private String dosage;

    /** 禁忌 */
    private String contraindications;

    /** 不良反应 */
    private String adverseReactions;

    /** 贮藏 */
    private String storage;

    /** 特殊人群警示 */
    private String specialPopulation;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
