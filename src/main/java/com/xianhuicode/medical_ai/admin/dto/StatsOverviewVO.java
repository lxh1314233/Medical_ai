package com.xianhuicode.medical_ai.admin.dto;

import lombok.Data;

/**
 * 数据统计看板总览（ADMIN-03）。
 */
@Data
public class StatsOverviewVO {

    /** 用户总数 */
    private long userCount;

    /** 问答提问数（chat_message.role=user） */
    private long chatQuestionCount;

    /** 分诊次数 */
    private long triageCount;

    /** 报告解读次数 */
    private long reportCount;

    /** 反馈总数 */
    private long feedbackCount;

    /** 待审纠错数 */
    private long pendingCorrectionCount;

    /** 满意评价数（HELPFUL） */
    private long helpfulCount;

    /** 参与满意度评价总数（HELPFUL + NOT_HELPFUL） */
    private long evaluationCount;

    /** 满意度（百分比，0~100，保留两位小数；无评价时为 null） */
    private Double satisfactionRate;
}
