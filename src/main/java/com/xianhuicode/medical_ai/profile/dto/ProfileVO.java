package com.xianhuicode.medical_ai.profile.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 健康档案返回对象（敏感字段已解密）。
 */
@Data
public class ProfileVO {

    private String gender;

    private LocalDate birthDate;

    private String height;

    private String weight;

    private String allergyHistory;

    private String pastHistory;

    private String medicationHistory;

    private String familyHistory;
}
