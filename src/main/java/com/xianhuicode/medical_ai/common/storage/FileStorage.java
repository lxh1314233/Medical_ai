package com.xianhuicode.medical_ai.common.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象：本地/OSS 切换。返回的路径为存储内相对路径（fileUrl）。
 */
public interface FileStorage {

    /**
     * 存储上传文件，返回相对路径（如 {@code 1/abc.png}）。
     */
    String store(Long userId, MultipartFile file);

    /**
     * 删除指定相对路径的文件（不存在则忽略）。
     */
    void delete(String path);
}
