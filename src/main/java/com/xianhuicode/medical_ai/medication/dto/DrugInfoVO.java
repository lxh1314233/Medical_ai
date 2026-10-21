package com.xianhuicode.medical_ai.medication.dto;

import lombok.Data;

import java.util.List;

/**
 * 药品信息查询结果（MED-01/02/04）。
 * 未收录时 found=false，仅返回 disclaimer/说明，不做任何事实性补充。
 */
@Data
public class DrugInfoVO {

    /** 是否收录于药典 */
    private boolean found;

    /** 未收录时的引导文案（found=false 时返回，如「未收录该药品…」） */
    private String message;

    private String genericName;

    private String brandNames;

    private String indications;

    private String dosage;

    private String contraindications;

    private String adverseReactions;

    private String storage;

    private String specialPopulation;

    /** 结合用户档案生成的个性化警示（MED-04：儿童/老人/孕妇/哺乳/肝肾不全/过敏） */
    private List<String> warnings;

    /** 免责声明（固定附加） */
    private String disclaimer;
}
