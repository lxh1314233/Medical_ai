package com.xianhuicode.medical_ai.knowledge.controller;

import com.xianhuicode.medical_ai.common.api.Result;
import com.xianhuicode.medical_ai.knowledge.service.KnowledgeIndexer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库运维：向量索引重建（需登录；角色门禁待 ADMIN-01 补齐）。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeIndexer knowledgeIndexer;

    @PostMapping("/reindex")
    public Result<Integer> reindex() {
        return Result.ok(knowledgeIndexer.reindexAll());
    }
}
