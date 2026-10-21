package com.xianhuicode.medical_ai.ai.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地内存向量库：余弦相似度近邻，供无凭据的本地开发/联调使用。按 namespace 分层存储。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "vector", name = "store", havingValue = "memory")
public class InMemoryVectorStore implements VectorStore {

    private final Map<String, Map<String, float[]>> vectors = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Map<String, String>>> metadata = new ConcurrentHashMap<>();

    @Override
    public void upsert(String namespace, String id, float[] vector, Map<String, String> meta) {
        String ns = resolveNamespace(namespace);
        vectors.computeIfAbsent(ns, k -> new ConcurrentHashMap<>()).put(id, vector.clone());
        metadata.computeIfAbsent(ns, k -> new ConcurrentHashMap<>())
                .put(id, new LinkedHashMap<>(meta == null ? Map.of() : meta));
    }

    @Override
    public List<VectorHit> query(String namespace, float[] vector, int topK) {
        String ns = resolveNamespace(namespace);
        Map<String, float[]> nsVectors = vectors.getOrDefault(ns, Map.of());
        Map<String, Map<String, String>> nsMeta = metadata.getOrDefault(ns, Map.of());
        return nsVectors.entrySet().stream()
                .map(e -> new VectorHit(e.getKey(), nsMeta.get(e.getKey()), cosine(vector, e.getValue())))
                .sorted(Comparator.comparingDouble(VectorHit::score).reversed())
                .limit(Math.max(0, topK))
                .toList();
    }

    @Override
    public void delete(String namespace, Collection<String> ids) {
        if (ids == null) {
            return;
        }
        String ns = resolveNamespace(namespace);
        Map<String, float[]> nsVectors = vectors.get(ns);
        Map<String, Map<String, String>> nsMeta = metadata.get(ns);
        if (nsVectors != null) {
            ids.forEach(nsVectors::remove);
        }
        if (nsMeta != null) {
            ids.forEach(nsMeta::remove);
        }
    }

    @Override
    public void deleteAll(String namespace) {
        String ns = resolveNamespace(namespace);
        vectors.remove(ns);
        metadata.remove(ns);
    }

    @Override
    public String defaultNamespace() {
        return "public";
    }

    private String resolveNamespace(String namespace) {
        return namespace == null ? defaultNamespace() : namespace;
    }

    private double cosine(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0;
        }
        double dot = 0;
        double na = 0;
        double nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
