package com.xianhuicode.medical_ai.ai.llm;

import com.xianhuicode.medical_ai.common.api.ResultCode;
import com.xianhuicode.medical_ai.common.exception.BizException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * LangChain4j 真实流式网关（OpenAI 兼容协议）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "llm", name = "provider", havingValue = "openai")
public class LangChain4jLlmGateway implements LlmGateway {

    private final LlmProperties props;

    private volatile OpenAiStreamingChatModel model;

    @Override
    public void stream(List<LlmMessage> messages, Consumer<String> onToken) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        //StreamingChatResponseHandler回调接口
        //chat方法本身是一个非阻塞异步方法如果没有Latch 会直接调用回调接口，立即返回结果
        //与我们的逻辑不符合  我们这里需要完整的回答将其保存起来
        model().chat(toLangchainMessages(messages), new StreamingChatResponseHandler() {
            @Override
            //部分响应回调 即每个token就会回调
            public void onPartialResponse(String token) {
                if (token != null && !token.isEmpty()) {
                    //进行回调，执行Consumer<String> onToken的实现
                    onToken.accept(token);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
                latch.countDown();
            }
        });

        await(latch);
        if (error.get() != null) {
            log.error("LLM 流式调用失败: {}", error.get().getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "模型服务异常，请稍后再试");
        }
    }

    @Override
    public String generate(List<LlmMessage> messages) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        StringBuilder sb = new StringBuilder();
        model().chat(toLangchainMessages(messages), new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String token) {
                if (token != null) {
                    sb.append(token);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                error.set(t);
                latch.countDown();
            }
        });

        await(latch);
        if (error.get() != null) {
            log.error("LLM 生成调用失败: {}", error.get().getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "模型服务异常，请稍后再试");
        }
        return sb.toString();
    }

    private OpenAiStreamingChatModel model() {
        if (model == null) {
            synchronized (this) {
                if (model == null) {
                    model = OpenAiStreamingChatModel.builder()
                            .apiKey(props.getApiKey())
                            .baseUrl(props.getBaseUrl())
                            .modelName(props.getModelName())
                            .temperature(props.getTemperature())
                            .timeout(Duration.ofMillis(props.getTimeoutMs()))
                            .build();
                }
            }
        }
        return model;
    }

    private List<ChatMessage> toLangchainMessages(List<LlmMessage> messages) {
        return messages.stream().map(this::toLangchainMessage).toList();
    }

    private ChatMessage toLangchainMessage(LlmMessage m) {
        return switch (m.role()) {
            case "system" -> SystemMessage.from(m.content());
            case "assistant" -> AiMessage.from(m.content());
            default -> UserMessage.from(m.content());
        };
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await(props.getTimeoutMs() + 5000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultCode.INTERNAL_ERROR, "模型调用被中断");
        }
    }
}
