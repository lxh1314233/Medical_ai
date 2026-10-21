package com.xianhuicode.medical_ai.ai.llm;

import java.util.List;
import java.util.function.Consumer;

/**
 * 大模型网关抽象：与 LangChain4j 具体类型解耦，便于 mock 与真实模型切换。
 */
public interface LlmGateway {

    /**
     * 流式生成：逐 token 回调 onToken，阻塞直到生成完成或抛异常。
     *
     * @param messages 多轮消息（role: system/user/assistant）
     * @param onToken  增量文本回调
     */
    void stream(List<LlmMessage> messages, Consumer<String> onToken);

    /**
     * 同步生成完整文本，阻塞直到生成完成或抛异常。
     * 用于结构化输出（如症状分诊）——由调用方自行解析返回内容（如 JSON）。
     */
    String generate(List<LlmMessage> messages);

    /** 通用消息表示，role 取值 system/user/assistant */
    record LlmMessage(String role, String content) {
    }
}
