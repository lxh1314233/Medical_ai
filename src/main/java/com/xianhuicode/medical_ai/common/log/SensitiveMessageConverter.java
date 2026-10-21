package com.xianhuicode.medical_ai.common.log;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * 日志脱敏转换器：覆盖默认 {@code %msg}，对手机号、身份证号等敏感信息脱敏，
 * 保证健康信息不出现在明文日志（NFR-S04）。
 */
public class SensitiveMessageConverter extends MessageConverter {

    /** 手机号：138****5678 */
    private static final Pattern PHONE = Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})");

    /** 身份证号：保留前 6 后 4 */
    private static final Pattern ID_CARD = Pattern.compile("(\\d{6})\\d{8}(\\d{4})");

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        if (message == null || message.isEmpty()) {
            return message;
        }
        message = PHONE.matcher(message).replaceAll("$1****$2");
        message = ID_CARD.matcher(message).replaceAll("$1********$2");
        return message;
    }
}
