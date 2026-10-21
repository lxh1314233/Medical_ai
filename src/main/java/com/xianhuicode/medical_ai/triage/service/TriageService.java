package com.xianhuicode.medical_ai.triage.service;

import com.xianhuicode.medical_ai.triage.dto.TriageReq;
import com.xianhuicode.medical_ai.triage.dto.TriageVO;

/**
 * 症状分诊：红旗拦截 → 结构化分诊 → 落库。
 */
public interface TriageService {

    TriageVO triage(Long userId, TriageReq req);
}
