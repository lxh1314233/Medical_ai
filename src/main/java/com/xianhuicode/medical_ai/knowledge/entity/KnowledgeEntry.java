package com.xianhuicode.medical_ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库条目（供 RAG 检索）。
 */
@Data
@TableName("knowledge_entry")
public class KnowledgeEntry {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String source;

    private String category;

    /** PENDING / PUBLISHED / OFFLINE */
    private String status;

    private Integer version;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
