package com.xianhuicode.medical_ai.ai.guard;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 敏感内容过滤（涉政/色情/暴力等无关有害内容）。
 */
@Component
@Order(1)
public class SensitiveFilter implements Guard {

    private static final String REPLY = "抱歉，本系统仅提供医疗健康相关咨询，无法回答无关或敏感内容。如有健康问题，请描述您的症状或疑问。";

    private static final List<String> KEYWORDS = List.of(
            "色情", "裸聊", "赌博", "博彩", "枪支", "炸药", "炸弹", "恐怖", "暴力", "杀人", "贩毒", "毒品"
    );

    @Override
    public Optional<GuardResult> check(String input) {
        if (input == null) {
            return Optional.empty();
        }
        boolean hit = KEYWORDS.stream().anyMatch(input::contains);
        return hit ? Optional.of(new GuardResult("SENSITIVE", REPLY)) : Optional.empty();
    }
}
