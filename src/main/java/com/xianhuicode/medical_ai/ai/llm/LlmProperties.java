package com.xianhuicode.medical_ai.ai.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型配置。provider=mock 时走 {@link MockLlmGateway}，provider=openai 时走真实流式。
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /** mock | openai */
    private String provider = "mock";

    private String apiKey;

    /** OpenAI 兼容接口地址（DeepSeek/通义等） */
    private String baseUrl = "https://api.deepseek.com";

    private String modelName = "deepseek-chat";

    private Double temperature = 0.7;

    private long timeoutMs = 60000;
}
