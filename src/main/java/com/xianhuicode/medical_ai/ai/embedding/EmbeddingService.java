package com.xianhuicode.medical_ai.ai.embedding;

/**
 * 文本向量化抽象：把一段文本转为稠密向量，供向量库索引/检索。
 */
public interface EmbeddingService {

    /** 将文本转为向量。 */
    float[] embed(String text);

    /** 是否已配置可用（key/端点非空）。未配置时调用方应跳过向量路、走回退。 */
    default boolean isConfigured() {
        return true;
    }
}
