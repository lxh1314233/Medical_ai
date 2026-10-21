package com.xianhuicode.medical_ai.ai.rag;

import com.xianhuicode.medical_ai.ai.embedding.EmbeddingService;
import com.xianhuicode.medical_ai.ai.vector.VectorStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 向量检索（唯一 RAG 实现）：embedding 问题 → 公共 + 用户私有 namespace 近邻召回 → 映射溯源。
 * 未配置 / 空命中 / 异常时返回空结果，不降级到关键词检索。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PineconeRagService implements RagService {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    @Override
    public List<KnowledgeHit> search(Long userId, String query, int topK) {
        if (!embeddingService.isConfigured() || !vectorStore.isConfigured()) {
            log.debug("embedding/向量库未配置，跳过向量检索");
            return List.of();
        }
        try {
            // 将用户问题向量化
            float[] queryVec = embeddingService.embed(query);
            if (queryVec == null || queryVec.length == 0) {
                return List.of();
            }
            // 公共知识 + 用户私有文档，分别检索后合并（公共在前、私有在后）
            List<VectorStore.VectorHit> hits = new ArrayList<>();
            hits.addAll(vectorStore.query(vectorStore.defaultNamespace(), queryVec, topK));
            if (userId != null) {
                hits.addAll(vectorStore.query("u" + userId, queryVec, topK));
            }
            if (hits.isEmpty()) {
                log.debug("向量库无命中");
                return List.of();
            }
            // 将向量中的信息提取出来
            List<KnowledgeHit> result = hits.stream()
                    .map(this::toKnowledgeHit)
                    .filter(h -> !h.title().isBlank() || !h.content().isBlank())
                    .toList();
            log.debug("向量命中 {} 条", result.size());
            return result;
        } catch (Exception e) {
            log.warn("向量检索失败: {}", e.getMessage());
            return List.of();
        }
    }

    private KnowledgeHit toKnowledgeHit(VectorStore.VectorHit hit) {
        Map<String, String> meta = hit.metadata() == null ? Map.of() : hit.metadata();
        String title = meta.getOrDefault("title", "");
        String content = meta.getOrDefault("content", "");
        String source = meta.getOrDefault("source", "");
        return new KnowledgeHit(title, content, source);
    }
}
