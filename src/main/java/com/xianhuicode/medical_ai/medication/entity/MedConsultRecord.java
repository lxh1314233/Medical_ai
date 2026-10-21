package com.xianhuicode.medical_ai.medication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用药咨询记录（对应 med_consult_record 表）。
 */
@Data
@TableName("med_consult_record")
public class MedConsultRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String query;

    private String result;

    private LocalDateTime createTime;
}
