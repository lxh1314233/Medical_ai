package com.xianhuicode.medical_ai.medication.dto;

import lombok.Data;

import java.util.List;

/**
 * 药物相互作用检查结果（MED-03）。
 */
@Data
public class InteractionResultVO {

    /** 命中的相互作用（可能为空） */
    private List<InteractionVO> interactions;

    /** 固定说明：常见已知相互作用，具体请咨询医师/药师 */
    private String note;
}
