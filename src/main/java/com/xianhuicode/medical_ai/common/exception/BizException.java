package com.xianhuicode.medical_ai.common.exception;

import com.xianhuicode.medical_ai.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常，由全局异常处理器统一转为 {@link com.xianhuicode.medical_ai.common.api.Result}。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
