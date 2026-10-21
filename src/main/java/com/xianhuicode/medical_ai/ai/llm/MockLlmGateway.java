package com.xianhuicode.medical_ai.ai.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Mock 模型：无外部依赖即可跑通流式链路，用于开发联调。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "llm", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockLlmGateway implements LlmGateway {

    @Override
    public void stream(List<LlmMessage> messages, Consumer<String> onToken) {
        String question = messages.stream()
                .filter(m -> "user".equals(m.role()))
                .reduce((first, second) -> second)
                .map(LlmMessage::content)
                .orElse("您的问题");
        String answer = buildAnswer(question);

        int step = 3;
        for (int i = 0; i < answer.length(); i += step) {
            int end = Math.min(i + step, answer.length());
            onToken.accept(answer.substring(i, end));
            sleep(20);
        }
    }

    @Override
    public String generate(List<LlmMessage> messages) {
        // 固定返回结构化分诊 JSON，供 mock 联调走通「LLM 输出 → JSON 解析」链路
        return "{\"departments\":[\"全科\",\"内科\"],\"reasons\":[\"mock 分诊：建议线下咨询确认\"],\"urgencyLevel\":\"先观察\",\"observationNotes\":\"观察症状变化，若加重请及时就医\"}";
    }

    private String buildAnswer(String question) {
        String q = question.length() > 30 ? question.substring(0, 30) + "…" : question;
        return "关于「" + q + "」，以下是一般性的健康科普参考：\n\n" +
                "1. 多数常见不适可先通过休息、清淡饮食、适量饮水等方式观察。\n" +
                "2. 若症状持续、加重，或出现高热、呼吸困难、剧烈疼痛等情况，请及时就医。\n" +
                "3. 具体诊疗请以医生面诊为准，勿自行用药。\n\n" +
                "（本回答由 mock 模型生成，仅供开发联调演示。）";
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
