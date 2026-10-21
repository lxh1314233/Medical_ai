package com.xianhuicode.medical_ai.ai.vector;

import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pinecone 向量库实现：直接走 Data Plane REST（serverless index host），Api-Key 鉴权。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "vector", name = "store", havingValue = "pinecone", matchIfMissing = true)
public class PineconeVectorStore implements VectorStore {

    private final VectorProperties props;
    private final ObjectMapper objectMapper;

    private volatile RestClient restClient;

    @Override
    public void upsert(String namespace, String id, float[] vector, Map<String, String> metadata) {
        Map<String, Object> body = Map.of(
                "vectors", List.of(Map.of(
                        "id", id,
                        "values", toList(vector),
                        "metadata", metadata == null ? Map.of() : metadata)),
                "namespace", resolveNamespace(namespace));
        postVoid("/vectors/upsert", body);
    }

    @Override
    public List<VectorHit> query(String namespace, float[] vector, int topK) {
        Map<String, Object> body = Map.of(
                "vector", toList(vector),
                "topK", topK,
                "includeMetadata", true,
                "namespace", resolveNamespace(namespace));
        String json = postJson("/query", body);
        return parseMatches(json);
    }

    @Override
    public void delete(String namespace, java.util.Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        postVoid("/vectors/delete", Map.of("ids", List.copyOf(ids), "namespace", resolveNamespace(namespace)));
    }

    @Override
    public void deleteAll(String namespace) {
        postVoid("/vectors/delete", Map.of("deleteAll", true, "namespace", resolveNamespace(namespace)));
    }

    @Override
    public String defaultNamespace() {
        VectorProperties.Pinecone p = props.getPinecone();
        return p == null || p.getNamespace() == null ? "" : p.getNamespace();
    }

    @Override
    public boolean isConfigured() {
        VectorProperties.Pinecone p = props.getPinecone();
        return p != null && isNotBlank(p.getApiKey()) && isNotBlank(p.getIndexHost());
    }

    // ==================== 内部 ====================

    private String resolveNamespace(String namespace) {
        return namespace == null ? defaultNamespace() : namespace;
    }

    private void postVoid(String path, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            client().post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .toBodilessEntity();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Pinecone 调用失败 path={}: {}", path, e.getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "向量库服务异常，请稍后再试");
        }
    }

    private String postJson(String path, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            return client().post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(String.class);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Pinecone 调用失败 path={}: {}", path, e.getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "向量库服务异常，请稍后再试");
        }
    }

    private RestClient client() {
        if (restClient == null) {
            synchronized (this) {
                if (restClient == null) {
                    restClient = RestClient.builder()
                            .baseUrl(props.getPinecone().getIndexHost())
                            .defaultHeader("Api-Key", props.getPinecone().getApiKey())
                            .build();
                }
            }
        }
        return restClient;
    }

    private List<Float> toList(float[] vector) {
        List<Float> list = new ArrayList<>(vector.length);
        for (float v : vector) {
            list.add(v);
        }
        return list;
    }

    private List<VectorHit> parseMatches(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode matches = root == null ? null : root.get("matches");
            if (matches == null || !matches.isArray()) {
                return List.of();
            }
            List<VectorHit> hits = new ArrayList<>();
            Iterator<JsonNode> it = matches.iterator();
            while (it.hasNext()) {
                JsonNode m = it.next();
                String id = m.path("id").asText(null);
                double score = m.path("score").asDouble(0.0);
                hits.add(new VectorHit(id, toMeta(m.get("metadata")), score));
            }
            return hits;
        } catch (Exception e) {
            log.warn("解析 Pinecone 查询结果失败: {}", e.getMessage());
            return List.of();
        }
    }

    private Map<String, String> toMeta(JsonNode meta) {
        Map<String, String> map = new LinkedHashMap<>();
        if (meta != null && meta.isObject()) {
            meta.forEachEntry((k, v) -> map.put(k, v.isTextual() ? v.asText() : v.toString()));
        }
        return map;
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
