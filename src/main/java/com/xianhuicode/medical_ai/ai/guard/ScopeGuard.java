package com.xianhuicode.medical_ai.ai.guard;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 能力边界拦截（确诊/开处方/推荐品牌等超范围请求），降级为引导线下就医。
 */
@Component
@Order(3)
public class ScopeGuard implements Guard {

    private static final String REPLY = "抱歉，我作为健康科普助手，无法提供疾病确诊、开具处方或推荐具体药品品牌等服务。建议您前往正规医疗机构，由执业医师面诊后规范诊疗。我可以为您提供相关疾病的科普知识或就诊科室参考。";

    private static final List<String> KEYWORDS = List.of(
            "确诊", "帮我诊断", "给我诊断", "我是什么病", "是不是得了", "开处方", "开药", "给我开", "帮我开",
            "处方", "推荐品牌", "推荐药", "什么药最好", "鉴定", "伤残", "司法", "开证明", "索赔"
    );

    @Override
    public Optional<GuardResult> check(String input) {
        if (input == null) {
            return Optional.empty();
        }
        boolean hit = KEYWORDS.stream().anyMatch(input::contains);
        return hit ? Optional.of(new GuardResult("SCOPE", REPLY)) : Optional.empty();
    }
}
