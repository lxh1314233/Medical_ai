package com.xianhuicode.medical_ai.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识条目新增/编辑请求。
 */
@Data
public class KnowledgeReq {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String source;

    private String category;
}
