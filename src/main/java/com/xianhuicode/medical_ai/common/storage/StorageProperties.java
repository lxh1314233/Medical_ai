package com.xianhuicode.medical_ai.common.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件存储配置。type=local 时走 {@link LocalFileStorage}，OSS 实现预留。
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /** local | oss */
    private String type = "local";

    /** 本地存储根目录（相对工作目录） */
    private String localDir = "uploads";
}
