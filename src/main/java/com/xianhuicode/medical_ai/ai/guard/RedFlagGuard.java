package com.xianhuicode.medical_ai.ai.guard;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 红旗症状拦截（急危重症），命中立即引导 120/急诊，终止普通问答。
 */
@Component
@Order(2)
public class RedFlagGuard implements Guard {

    private static final String REPLY = "⚠️ 您描述的情况可能属于急危重症，请立即拨打 120 或前往最近的急诊就医，不要等待网络回复。本系统无法提供紧急医疗处置。";

    private static final List<String> KEYWORDS = List.of(
            "胸痛", "胸闷", "呼吸困难", "喘不上气", "窒息", "意识障碍", "昏迷", "晕倒", "晕厥", "休克",
            "大出血", "吐血", "咯血", "便血", "黑便", "抽搐", "癫痫", "剧烈头痛", "口角歪斜", "偏瘫",
            "半身不遂", "说话不清", "失语", "喉头水肿", "严重过敏", "过敏休克", "中毒", "触电", "溺水",
            "心跳骤停", "心脏骤停", "剧烈腹痛", "自杀", "自残", "车祸", "坠落"
    );

    @Override
    public Optional<GuardResult> check(String input) {
        if (input == null) {
            return Optional.empty();
        }
        boolean hit = KEYWORDS.stream().anyMatch(input::contains);
        return hit ? Optional.of(new GuardResult("RED_FLAG", REPLY)) : Optional.empty();
    }
}
