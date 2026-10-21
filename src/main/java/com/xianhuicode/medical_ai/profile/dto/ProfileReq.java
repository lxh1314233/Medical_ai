package com.xianhuicode.medical_ai.profile.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 健康档案维护请求，全部字段选填。
 */
@Data
public class ProfileReq {

    private String gender;

    private LocalDate birthDate;

    private String height;

    private String weight;

    private String allergyHistory;

    private String pastHistory;

    private String medicationHistory;

    private String familyHistory;
}
