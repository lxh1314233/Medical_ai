package com.xianhuicode.medical_ai.ai.embedding;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 向量化配置（阿里云 DashScope，OpenAI 兼容端点）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "embedding")
public class EmbeddingProperties {

    private String apiKey;

    /** OpenAI 兼容 embeddings 端点（DashScope compatible-mode）。 */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private String modelName = "text-embedding-v3";

    /** 向量维度，必须与 Pinecone index 建库维度一致。 */
    private Integer dimension = 1024;

    private long timeoutMs = 60000;
}
