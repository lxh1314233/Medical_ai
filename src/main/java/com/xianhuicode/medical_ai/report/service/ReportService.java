package com.xianhuicode.medical_ai.report.service;

import com.xianhuicode.medical_ai.report.dto.ReportDetailVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 报告解读：上传 → 异步 OCR + 解读；查询；删除。
 */
public interface ReportService {

    Long upload(Long userId, MultipartFile file);

    ReportDetailVO detail(Long userId, Long reportId);

    void delete(Long userId, Long reportId);
}
