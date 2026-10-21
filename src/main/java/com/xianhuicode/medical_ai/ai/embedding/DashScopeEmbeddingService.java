package com.xianhuicode.medical_ai.ai.embedding;

import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.exception.BizException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 阿里云 DashScope 向量化（text-embedding-v3，OpenAI 兼容端点），经 LangChain4j OpenAiEmbeddingModel 调用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashScopeEmbeddingService implements EmbeddingService {

    private final EmbeddingProperties props;

    private volatile OpenAiEmbeddingModel model;

    @Override
    public float[] embed(String text) {
        try {
            Response<Embedding> response = model().embed(text);
            Embedding embedding = response == null ? null : response.content();
            return embedding == null ? null : embedding.vector();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("向量化失败: {}", e.getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "向量化服务异常，请稍后再试");
        }
    }

    @Override
    public boolean isConfigured() {
        return props.getApiKey() != null && !props.getApiKey().isBlank();
    }

    private OpenAiEmbeddingModel model() {
        if (model == null) {
            synchronized (this) {
                if (model == null) {
                    model = OpenAiEmbeddingModel.builder()
                            .apiKey(props.getApiKey())
                            .baseUrl(props.getBaseUrl())
                            .modelName(props.getModelName())
                            .dimensions(props.getDimension())
                            .timeout(Duration.ofMillis(props.getTimeoutMs()))
                            .build();
                }
            }
        }
        return model;
    }
}
