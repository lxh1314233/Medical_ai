package com.xianhuicode.medical_ai.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 反馈评价请求（FB-01/02）。
 */
@Data
public class FeedbackReq {

    /** HELPFUL / NOT_HELPFUL / CORRECTION */
    @NotBlank(message = "反馈类型不能为空")
    @Pattern(regexp = "HELPFUL|NOT_HELPFUL|CORRECTION", message = "反馈类型不合法")
    private String type;

    /** 被评价的回答（chat_message.id），HELPFUL/NOT_HELPFUL 必填 */
    private Long messageId;

    /** 纠错说明，CORRECTION 必填 */
    @Size(max = 500, message = "反馈说明过长")
    private String content;
}
