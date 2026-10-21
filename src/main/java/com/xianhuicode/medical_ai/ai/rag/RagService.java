package com.xianhuicode.medical_ai.ai.rag;

import java.util.List;

/**
 * RAG 检索抽象：给定问题返回 top-K 条知识片段。实现为向量检索（Pinecone / 内存向量库）。
 */
public interface RagService {

    List<KnowledgeHit> search(Long userId, String query, int topK);

    /** 检索命中条目 */
    record KnowledgeHit(String title, String content, String source) {
    }
}
