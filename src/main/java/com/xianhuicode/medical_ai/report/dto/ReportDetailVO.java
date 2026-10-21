package com.xianhuicode.medical_ai.report.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报告解读详情。READY 时返回完整解读；PENDING/FAILED 时仅返回 status 与 message。
 */
@Data
public class ReportDetailVO {

    private Long id;

    /** PENDING / READY / FAILED */
    private String status;

    /** 是否含危急值 */
    private boolean criticalFlag;

    /** 综合小结 */
    private String summary;

    /** 免责声明 */
    private String disclaimer;

    /** 逐项指标解读 */
    private List<IndicatorVO> indicators;

    /** 非 READY 状态下的提示文案 */
    private String message;

    private LocalDateTime createTime;
}
