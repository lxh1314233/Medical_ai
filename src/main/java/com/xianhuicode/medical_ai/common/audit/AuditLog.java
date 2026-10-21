package com.xianhuicode.medical_ai.common.audit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核留痕（对应 audit_log 表）。跨模块共用：纠错反馈提交、聊天守卫命中、后台审核处理、用户管理等操作留痕（SYS-07）。
 */
@Data
@TableName("audit_log")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人（管理员 id），用户侧动作（纠错/守卫命中）时为提问用户 id */
    private Long operatorId;

    /** 目标类型，如 FEEDBACK / CHAT / USER */
    private String targetType;

    private Long targetId;

    /** 动作，如 SUBMIT / GUARD_HIT / REVIEW / DISABLE */
    private String action;

    private String detail;

    private LocalDateTime createTime;
}
