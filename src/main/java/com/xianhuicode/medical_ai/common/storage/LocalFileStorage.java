package com.xianhuicode.medical_ai.common.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件存储：存 {local-dir}/{userId}/{uuid}.{ext}，相对路径作为 fileUrl 落库。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

    private final StorageProperties props;

    @Override
    public String store(Long userId, MultipartFile file) {
        try {
            String ext = extOf(file.getOriginalFilename());
            String dir = props.getLocalDir() + "/" + userId;
            Path dirPath = Paths.get(dir);
            Files.createDirectories(dirPath);
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dirPath.resolve(fileName);
            try (var in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return userId + "/" + fileName;
        } catch (IOException e) {
            log.error("本地文件存储失败: {}", e.getMessage());
            throw new IllegalStateException("文件保存失败", e);
        }
    }

    @Override
    public void delete(String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        try {
            // 防御：仅允许删除本地目录内的文件，防路径穿越
            Path root = Paths.get(props.getLocalDir()).toAbsolutePath().normalize();
            Path target = root.resolve(path).normalize();
            if (!target.startsWith(root)) {
                log.warn("拒绝删除本地目录外的路径: {}", path);
                return;
            }
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("删除本地文件失败 path={}: {}", path, e.getMessage());
        }
    }

    private String extOf(String fileName) {
        if (fileName == null) {
            return "bin";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "bin";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }
}
