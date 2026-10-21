package com.xianhuicode.medical_ai.knowledge.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户文档列表项。
 */
@Data
public class DocumentVO {

    private Long id;

    private String fileName;

    private String fileType;

    /** PROCESSING/READY/FAILED */
    private String status;

    private Integer chunkCount;

    private String errorMsg;

    private LocalDateTime createTime;
}
