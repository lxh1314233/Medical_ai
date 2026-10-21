package com.xianhuicode.medical_ai.knowledge.controller;

import com.xianhuicode.medical_ai.common.api.Result;
import com.xianhuicode.medical_ai.common.security.SecurityUtils;
import com.xianhuicode.medical_ai.knowledge.dto.DocumentVO;
import com.xianhuicode.medical_ai.knowledge.service.UserDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户私有知识库：上传文件 → 向量化入库；列表；删除。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeUploadController {

    private final UserDocumentService userDocumentService;

    @PostMapping("/upload")
    public Result<Long> upload(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(userDocumentService.upload(userId, file));
    }

    @GetMapping("/documents")
    public Result<List<DocumentVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(userDocumentService.list(userId));
    }

    @DeleteMapping("/documents/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        userDocumentService.delete(userId, id);
        return Result.ok();
    }
}
