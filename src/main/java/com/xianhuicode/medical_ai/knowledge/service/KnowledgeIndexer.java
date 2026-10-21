package com.xianhuicode.medical_ai.knowledge.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xianhuicode.medical_ai.ai.embedding.EmbeddingService;
import com.xianhuicode.medical_ai.ai.vector.VectorStore;
import com.xianhuicode.medical_ai.knowledge.entity.KnowledgeEntry;
import com.xianhuicode.medical_ai.knowledge.mapper.KnowledgeEntryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识条目 → 向量库 的同步：全量重建 / 单条增量 / 删除。供知识 CRUD（后续 ADMIN）与手动 reindex 调用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeIndexer {

    private final KnowledgeEntryMapper knowledgeEntryMapper;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    /** 全量重建索引：把所有已上架条目 embed + upsert 进向量库，返回索引条数。 */
    public int reindexAll() {
        List<KnowledgeEntry> entries = knowledgeEntryMapper.selectList(
                Wrappers.<KnowledgeEntry>lambdaQuery()
                        .eq(KnowledgeEntry::getStatus, "PUBLISHED"));
        int count = 0;
        for (KnowledgeEntry e : entries) {
            indexEntry(e);
            count++;
        }
        log.info("向量索引全量重建完成，共 {} 条", count);
        return count;
    }

    /** 单条增量索引（知识发布/更新时调用）。 */
    public void indexEntry(Long id) {
        KnowledgeEntry e = knowledgeEntryMapper.selectById(id);
        if (e != null) {
            indexEntry(e);
        }
    }

    /** 移除单条索引（知识下架/删除时调用）。 */
    public void removeEntry(Long id) {
        vectorStore.delete(vectorStore.defaultNamespace(), List.of("kb-" + id));
    }

    private void indexEntry(KnowledgeEntry e) {
        String title = e.getTitle() == null ? "" : e.getTitle();
        String content = e.getContent() == null ? "" : e.getContent();
        float[] vec = embeddingService.embed(title + "\n" + content);

        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("title", title);
        meta.put("content", content);
        meta.put("source", e.getSource() == null ? "" : e.getSource());
        vectorStore.upsert(vectorStore.defaultNamespace(), "kb-" + e.getId(), vec, meta);
    }
}
