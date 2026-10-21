package com.xianhuicode.medical_ai.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 会话重命名请求。
 */
@Data
public class RenameSessionReq {

    @NotBlank(message = "标题不能为空")
    @Size(max = 64, message = "标题过长")
    private String title;
}
