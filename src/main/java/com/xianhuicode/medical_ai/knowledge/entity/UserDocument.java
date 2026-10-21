package com.xianhuicode.medical_ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户私有知识库文档（上传文件 → 向量化入库）。
 */
@Data
@TableName("user_document")
public class UserDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String fileName;

    /** txt/md/pdf/docx */
    private String fileType;

    /** PROCESSING/READY/FAILED */
    private String status;

    private Integer chunkCount;

    private String errorMsg;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
