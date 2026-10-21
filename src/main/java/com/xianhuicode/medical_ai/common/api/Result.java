package com.xianhuicode.medical_ai.common.api;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体。
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return build(ResultCode.SUCCESS, null);
    }

    public static <T> Result<T> ok(T data) {
        return build(ResultCode.SUCCESS, data);
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return build(resultCode, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    private static <T> Result<T> build(ResultCode rc, T data) {
        Result<T> r = new Result<>();
        r.code = rc.getCode();
        r.message = rc.getMessage();
        r.data = data;
        return r;
    }
}
