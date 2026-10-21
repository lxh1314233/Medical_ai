package com.xianhuicode.medical_ai.knowledge.service;

import com.xianhuicode.medical_ai.knowledge.dto.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户私有知识库：上传、列表、删除。
 */
public interface UserDocumentService {

    /** 上传文件：落库 PROCESSING 并异步解析入库，返回文档 id。 */
    Long upload(Long userId, MultipartFile file);

    List<DocumentVO> list(Long userId);

    void delete(Long userId, Long documentId);
}
