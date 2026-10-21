package com.xianhuicode.medical_ai.medication.service;

import com.xianhuicode.medical_ai.medication.dto.DrugInfoVO;
import com.xianhuicode.medical_ai.medication.dto.InteractionReq;
import com.xianhuicode.medical_ai.medication.dto.InteractionResultVO;

/**
 * 用药咨询：药品信息查询、药物相互作用检查。
 */
public interface MedicationService {

    DrugInfoVO query(Long userId, String name);

    InteractionResultVO checkInteractions(Long userId, InteractionReq req);
}
