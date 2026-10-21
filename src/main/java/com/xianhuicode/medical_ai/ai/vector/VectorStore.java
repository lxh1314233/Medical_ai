package com.xianhuicode.medical_ai.ai.vector;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 向量库抽象：写入/检索向量。store=pinecone 走真实向量库，store=memory 走本地内存实现。
 * 所有方法带 namespace 参数：null 表示用默认（公共知识库）namespace；用户私有文档用 u{userId}。
 */
public interface VectorStore {

    /** 写入（或覆盖）指定 namespace 下的一条向量及其元数据。 */
    void upsert(String namespace, String id, float[] vector, Map<String, String> metadata);

    /** 近邻检索 top-K，按相似度降序。 */
    List<VectorHit> query(String namespace, float[] vector, int topK);

    /** 删除指定 id 的向量。 */
    void delete(String namespace, Collection<String> ids);

    /** 清空指定 namespace。 */
    void deleteAll(String namespace);

    /** 默认（公共知识库）namespace。 */
    String defaultNamespace();

    /** 是否已配置可用（key/host 非空）。未配置时调用方应跳过向量路、走回退。 */
    default boolean isConfigured() {
        return true;
    }

    /** 检索命中条目。 */
    record VectorHit(String id, Map<String, String> metadata, Double score) {
    }
}
