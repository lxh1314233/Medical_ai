package com.xianhuicode.medical_ai.profile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康档案实体，对应 user_profile 表。敏感字段（过敏史/既往史/用药史/家族史）加密存储。
 */
@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String gender;

    private LocalDate birthDate;

    private String height;

    private String weight;

    /** 加密 */
    private String allergyHistory;

    /** 加密 */
    private String pastHistory;

    /** 加密 */
    private String medicationHistory;

    /** 加密 */
    private String familyHistory;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
