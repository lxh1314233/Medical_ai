package com.xianhuicode.medical_ai.ai.guard;

/**
 * 拦截结果。type 取值 RED_FLAG / SCOPE / SENSITIVE。
 */
public record GuardResult(String type, String reply) {
}
