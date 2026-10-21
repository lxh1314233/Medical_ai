package com.xianhuicode.medical_ai.admin.service;

import com.xianhuicode.medical_ai.admin.dto.KnowledgeReq;
import com.xianhuicode.medical_ai.admin.dto.KnowledgeVO;
import com.xianhuicode.medical_ai.common.api.PageVO;

public interface AdminKnowledgeService {

    KnowledgeVO create(KnowledgeReq req);

    KnowledgeVO update(Long id, KnowledgeReq req);

    KnowledgeVO publish(Long id);

    KnowledgeVO offline(Long id);

    void delete(Long id);

    PageVO<KnowledgeVO> list(long page, long size, String status, String category, String keyword);

    KnowledgeVO detail(Long id);
}
