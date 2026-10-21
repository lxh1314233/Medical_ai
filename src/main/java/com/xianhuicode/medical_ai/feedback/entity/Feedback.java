package com.xianhuicode.medical_ai.feedback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈评价（对应 feedback 表）。type: HELPFUL/NOT_HELPFUL/CORRECTION；status: PENDING/PROCESSED。
 */
@Data
@TableName("feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 被评价的回答（chat_message.id），纠错时可为空 */
    private Long messageId;

    /** HELPFUL / NOT_HELPFUL / CORRECTION */
    private String type;

    /** 纠错说明（CORRECTION 时必填） */
    private String content;

    /** PENDING / PROCESSED */
    private String status;

    private LocalDateTime createTime;
}
