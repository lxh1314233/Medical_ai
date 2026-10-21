package com.xianhuicode.medical_ai.report.service.impl;

import com.xianhuicode.medical_ai.ai.ocr.OcrGateway;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.common.storage.FileStorage;
import com.xianhuicode.medical_ai.common.util.CryptoUtil;
import com.xianhuicode.medical_ai.report.dto.IndicatorVO;
import com.xianhuicode.medical_ai.report.dto.ReportDetailVO;
import com.xianhuicode.medical_ai.report.entity.ReportRecord;
import com.xianhuicode.medical_ai.report.mapper.ReportRecordMapper;
import com.xianhuicode.medical_ai.report.service.ReportInterpreter;
import com.xianhuicode.medical_ai.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 报告解读实现：上传校验 → 本地存储 → 落库 PENDING → 异步 OCR + 规则解读 → 加密落库。
 * 解读/异常/危急值均由 {@link ReportInterpreter} 规则引擎判定，不调用 LLM。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final ReportRecordMapper reportRecordMapper;
    private final ReportInterpreter interpreter;
    private final OcrGateway ocrGateway;
    private final FileStorage fileStorage;
    private final CryptoUtil cryptoUtil;
    private final ObjectMapper objectMapper;
    @Qualifier("indexExecutor")
    private final Executor indexExecutor;

    @Override
    public Long upload(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.REPORT_FILE_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BizException(ResultCode.REPORT_FILE_TOO_LARGE);
        }
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String fileType = fileTypeOf(fileName);
        if (!isSupported(fileType)) {
            throw new BizException(ResultCode.REPORT_FILE_TYPE_UNSUPPORTED);
        }

        String path = fileStorage.store(userId, file);

        ReportRecord record = new ReportRecord();
        record.setUserId(userId);
        record.setFileUrl(path);
        record.setStatus("PENDING");
        record.setCriticalFlag(0);
        reportRecordMapper.insert(record);
        Long reportId = record.getId();

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BizException(ResultCode.REPORT_OCR_FAILED);
        }
        indexExecutor.execute(() -> process(reportId, fileType, bytes));
        return reportId;
    }

    @Override
    public ReportDetailVO detail(Long userId, Long reportId) {
        ReportRecord record = reportRecordMapper.selectById(reportId);
        if (record == null) {
            throw new BizException(ResultCode.REPORT_NOT_FOUND);
        }
        if (!userId.equals(record.getUserId())) {
            throw new BizException(ResultCode.REPORT_NOT_OWNED);
        }

        ReportDetailVO vo = new ReportDetailVO();
        vo.setId(record.getId());
        vo.setStatus(record.getStatus());
        vo.setCreateTime(record.getCreateTime());
        vo.setCriticalFlag(record.getCriticalFlag() != null && record.getCriticalFlag() == 1);

        if ("READY".equals(record.getStatus())) {
            try {
                String plain = cryptoUtil.decrypt(record.getInterpretation());
                ReportDetailVO detail = objectMapper.readValue(plain, ReportDetailVO.class);
                vo.setSummary(detail.getSummary());
                vo.setDisclaimer(detail.getDisclaimer());
                vo.setIndicators(detail.getIndicators());
            } catch (Exception e) {
                log.error("报告解读结果解密失败 reportId={}: {}", reportId, e.getMessage());
                throw new BizException(ResultCode.INTERNAL_ERROR, "报告数据读取失败");
            }
        } else if ("FAILED".equals(record.getStatus())) {
            vo.setMessage("报告解读失败，请重新上传。");
        } else {
            vo.setMessage("报告正在解析中，请稍后刷新查看。");
        }
        return vo;
    }

    @Override
    public void delete(Long userId, Long reportId) {
        ReportRecord record = reportRecordMapper.selectById(reportId);
        if (record == null) {
            throw new BizException(ResultCode.REPORT_NOT_FOUND);
        }
        if (!userId.equals(record.getUserId())) {
            throw new BizException(ResultCode.REPORT_NOT_OWNED);
        }
        try {
            fileStorage.delete(record.getFileUrl());
        } catch (Exception e) {
            log.warn("删除报告文件失败 reportId={}: {}", reportId, e.getMessage());
        }
        reportRecordMapper.deleteById(reportId);
    }

    // ==================== 后台处理 ====================

    private void process(Long reportId, String fileType, byte[] bytes) {
        try {
            List<OcrGateway.OcrIndicator> ocr = ocrGateway.recognize(bytes, fileType);
            ReportDetailVO detail = interpreter.interpret(ocr);

            String ocrJson = objectMapper.writeValueAsString(ocr);
            String interpretationJson = objectMapper.writeValueAsString(detail);
            String abnormalJson = objectMapper.writeValueAsString(abnormalFlags(detail));

            ReportRecord update = new ReportRecord();
            update.setId(reportId);
            update.setOcrResult(cryptoUtil.encrypt(ocrJson));
            update.setInterpretation(cryptoUtil.encrypt(interpretationJson));
            update.setAbnormalFlags(abnormalJson);
            update.setCriticalFlag(detail.isCriticalFlag() ? 1 : 0);
            update.setStatus("READY");
            reportRecordMapper.updateById(update);
        } catch (Exception e) {
            log.error("报告解读失败 reportId={}: {}", reportId, e.getMessage());
            ReportRecord update = new ReportRecord();
            update.setId(reportId);
            update.setStatus("FAILED");
            reportRecordMapper.updateById(update);
        }
    }

    private List<IndicatorVO> abnormalFlags(ReportDetailVO detail) {
        if (detail.getIndicators() == null) {
            return List.of();
        }
        return detail.getIndicators().stream()
                .filter(i -> ReportInterpreter.ABNORMAL_LEVELS.contains(i.getLevel()))
                .toList();
    }

    // ==================== 工具 ====================

    private String fileTypeOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

    private boolean isSupported(String type) {
        return "jpg".equals(type) || "jpeg".equals(type) || "png".equals(type) || "pdf".equals(type);
    }
}
