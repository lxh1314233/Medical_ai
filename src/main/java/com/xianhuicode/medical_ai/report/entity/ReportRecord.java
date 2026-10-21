package com.xianhuicode.medical_ai.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报告记录（对应 report_record 表）。ocr_result / interpretation 为加密文本。
 */
@Data
@TableName("report_record")
public class ReportRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 文件存储相对路径 */
    private String fileUrl;

    /** OCR 原始结果 JSON（加密） */
    private String ocrResult;

    /** 解读结果 JSON（加密） */
    private String interpretation;

    /** 异常指标标注 JSON（明文，仅含 item/value/level） */
    private String abnormalFlags;

    /** 危急值：1 是 0 否 */
    private Integer criticalFlag;

    /** PENDING / READY / FAILED */
    private String status;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
