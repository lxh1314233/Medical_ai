package com.xianhuicode.medical_ai.ai.ocr;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OCR 配置。provider=mock 时走 {@link MockOcrGateway}，provider=dashscope 时走 Qwen-VL 视觉识别。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ocr")
public class OcrProperties {

    /** mock | dashscope */
    private String provider = "mock";

    /** DashScope API Key（与 embedding 共用 DASHSCOPE_API_KEY） */
    private String apiKey;

    /** OpenAI 兼容接口地址 */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /** 视觉模型 */
    private String model = "qwen-vl-plus";

    private long timeoutMs = 60000;
}
