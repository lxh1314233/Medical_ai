package com.xianhuicode.medical_ai.knowledge.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.ai.embedding.EmbeddingService;
import com.xianhuicode.medical_ai.ai.vector.VectorStore;
import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.exception.BizException;
import com.xianhuicode.medical_ai.knowledge.dto.DocumentVO;
import com.xianhuicode.medical_ai.knowledge.entity.UserDocument;
import com.xianhuicode.medical_ai.knowledge.mapper.UserDocumentMapper;
import com.xianhuicode.medical_ai.knowledge.service.DocumentTextExtractor;
import com.xianhuicode.medical_ai.knowledge.service.TextChunker;
import com.xianhuicode.medical_ai.knowledge.service.UserDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 用户私有知识库实现：上传 → 异步解析/切块/向量化入库；列表；删除（含向量清理）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDocumentServiceImpl implements UserDocumentService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_CHUNKS = 500;

    private final UserDocumentMapper userDocumentMapper;
    private final DocumentTextExtractor textExtractor;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    @Qualifier("indexExecutor")
    private final Executor indexExecutor;

    @Override
    public Long upload(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.KNOWLEDGE_FILE_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BizException(ResultCode.KNOWLEDGE_FILE_TOO_LARGE);
        }
        String fileName = file.getOriginalFilename() == null ? "未命名" : file.getOriginalFilename();
        String fileType = fileTypeOf(fileName);
        if (!isSupported(fileType)) {
            throw new BizException(ResultCode.KNOWLEDGE_FILE_TYPE_UNSUPPORTED);
        }

        UserDocument doc = new UserDocument();
        doc.setUserId(userId);
        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setStatus("PROCESSING");
        doc.setChunkCount(0);
        userDocumentMapper.insert(doc);
        Long docId = doc.getId();

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BizException(ResultCode.KNOWLEDGE_PARSE_FAILED);
        }
        indexExecutor.execute(() -> index(userId, docId, fileName, fileType, bytes));
        return docId;
    }

    @Override
    public List<DocumentVO> list(Long userId) {
        return userDocumentMapper.selectList(
                        Wrappers.<UserDocument>lambdaQuery()
                                .eq(UserDocument::getUserId, userId)
                                .orderByDesc(UserDocument::getId))
                .stream().map(this::toVO).toList();
    }

    @Override
    public void delete(Long userId, Long documentId) {
        UserDocument doc = userDocumentMapper.selectById(documentId);
        if (doc == null) {
            throw new BizException(ResultCode.KNOWLEDGE_DOC_NOT_FOUND);
        }
        if (!userId.equals(doc.getUserId())) {
            throw new BizException(ResultCode.KNOWLEDGE_DOC_NOT_OWNED);
        }
        int n = doc.getChunkCount() == null ? 0 : doc.getChunkCount();
        if (n > 0) {
            List<String> ids = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                ids.add("doc-" + documentId + "-" + i);
            }
            try {
                vectorStore.delete("u" + userId, ids);
            } catch (Exception e) {
                log.warn("删除文档向量失败 docId={}: {}", documentId, e.getMessage());
            }
        }
        userDocumentMapper.deleteById(documentId);
    }

    // ==================== 后台入库 ====================

    private void index(Long userId, Long docId, String fileName, String fileType, byte[] bytes) {
        try {
            String text = textExtractor.extract(bytes, fileType);
            List<String> chunks = TextChunker.chunk(text);
            if (chunks.size() > MAX_CHUNKS) {
                log.warn("文档 {} 块数 {} 超上限，截断为 {}", docId, chunks.size(), MAX_CHUNKS);
                chunks = chunks.subList(0, MAX_CHUNKS);
            }
            String namespace = "u" + userId;
            for (int i = 0; i < chunks.size(); i++) {
                float[] vec = embeddingService.embed(chunks.get(i));
                Map<String, String> meta = new LinkedHashMap<>();
                meta.put("title", fileName);
                meta.put("content", chunks.get(i));
                meta.put("source", fileName);
                meta.put("docId", String.valueOf(docId));
                meta.put("userId", String.valueOf(userId));
                vectorStore.upsert(namespace, "doc-" + docId + "-" + i, vec, meta);
            }
            UserDocument update = new UserDocument();
            update.setId(docId);
            update.setStatus("READY");
            update.setChunkCount(chunks.size());
            userDocumentMapper.updateById(update);
        } catch (Exception e) {
            log.error("文档向量化入库失败 docId={}: {}", docId, e.getMessage());
            UserDocument update = new UserDocument();
            update.setId(docId);
            update.setStatus("FAILED");
            update.setErrorMsg(truncate(e.getMessage(), 500));
            userDocumentMapper.updateById(update);
        }
    }

    private DocumentVO toVO(UserDocument d) {
        DocumentVO vo = new DocumentVO();
        vo.setId(d.getId());
        vo.setFileName(d.getFileName());
        vo.setFileType(d.getFileType());
        vo.setStatus(d.getStatus());
        vo.setChunkCount(d.getChunkCount());
        vo.setErrorMsg(d.getErrorMsg());
        vo.setCreateTime(d.getCreateTime());
        return vo;
    }

    private String fileTypeOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

    private boolean isSupported(String type) {
        return "txt".equals(type) || "md".equals(type) || "pdf".equals(type) || "docx".equals(type);
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
