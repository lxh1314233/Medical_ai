package com.xianhuicode.medical_ai.admin.service;

import com.xianhuicode.medical_ai.admin.dto.AuditLogVO;
import com.xianhuicode.medical_ai.admin.dto.FeedbackReviewVO;
import com.xianhuicode.medical_ai.common.api.PageVO;

public interface AdminReviewService {

    PageVO<FeedbackReviewVO> listFeedback(long page, long size, String type, String status);

    void processFeedback(Long id);

    PageVO<AuditLogVO> listAuditLogs(long page, long size);
}
