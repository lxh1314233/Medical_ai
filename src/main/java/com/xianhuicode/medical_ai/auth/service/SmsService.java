package com.xianhuicode.medical_ai.auth.service;

/**
 * 短信验证码服务。
 */
public interface SmsService {

    /**
     * 发送验证码。
     *
     * @param phone 手机号
     * @param scene 场景：REGISTER / LOGIN / RESET_PASSWORD
     */
    void sendCode(String phone, String scene);

    /**
     * 校验验证码，失败抛出 {@link com.xianhuicode.medical_ai.common.exception.BizException}。
     */
    void verifyOrThrow(String phone, String scene, String code);
}
