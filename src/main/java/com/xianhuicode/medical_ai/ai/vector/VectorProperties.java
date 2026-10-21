package com.xianhuicode.medical_ai.ai.vector;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 向量库配置。store=pinecone 走真实 Pinecone，store=memory 走本地内存实现。
 */
@Data
@Component
@ConfigurationProperties(prefix = "vector")
public class VectorProperties {

    /** pinecone | memory */
    private String store = "pinecone";

    /** 启动时是否自动全量重建索引（一般关闭，走手动 reindex）。 */
    private boolean autoIndex = false;

    private Pinecone pinecone = new Pinecone();

    @Data
    public static class Pinecone {

        private String apiKey;

        /** serverless index 的 Data Plane host，形如 https://xxx.svc.xxx.pinecone.io */
        private String indexHost;

        private String namespace = "medicalai";
    }
}
