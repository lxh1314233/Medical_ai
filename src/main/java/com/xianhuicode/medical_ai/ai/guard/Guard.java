package com.xianhuicode.medical_ai.ai.guard;

import java.util.Optional;

/**
 * 安全拦截统一接口。命中返回带固定回复的 {@link GuardResult}，否则返回 empty。
 */
public interface Guard {

    Optional<GuardResult> check(String input);
}
