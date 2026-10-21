package com.xianhuicode.medical_ai.common.api;

import lombok.Getter;

/**
 * 统一错误码。
 * 0 成功；4xx 客户端错误；5xx 服务端错误；业务错误独立段。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    INTERNAL_ERROR(500, "系统繁忙，请稍后再试"),

    // 认证 10xxx
    AUTH_PHONE_EXISTS(10001, "该手机号已注册"),
    AUTH_VERIFY_CODE_ERROR(10002, "验证码错误或已过期"),
    AUTH_PASSWORD_ERROR(10003, "用户名或密码错误"),
    AUTH_ACCOUNT_DISABLED(10004, "账号已被禁用"),
    AUTH_ACCOUNT_NOT_FOUND(10005, "账号不存在"),
    AUTH_TOKEN_INVALID(10006, "token 无效或已过期"),

    // 通用业务 20xxx
    BIZ_PARAM_ERROR(20001, "参数校验失败"),
    BIZ_DATA_NOT_FOUND(20002, "数据不存在"),

    // 智能问答 30xxx
    CHAT_SESSION_NOT_FOUND(30001, "会话不存在"),
    CHAT_SESSION_NOT_OWNED(30002, "无权访问该会话"),

    // 知识库/文档上传 40xxx
    KNOWLEDGE_FILE_EMPTY(40001, "上传文件为空"),
    KNOWLEDGE_FILE_TYPE_UNSUPPORTED(40002, "仅支持 txt/md/pdf/docx 格式"),
    KNOWLEDGE_FILE_TOO_LARGE(40003, "文件过大，最大 10MB"),
    KNOWLEDGE_PARSE_FAILED(40004, "文件解析失败"),
    KNOWLEDGE_INDEX_FAILED(40005, "向量化入库失败"),
    KNOWLEDGE_DOC_NOT_FOUND(40006, "文档不存在"),
    KNOWLEDGE_DOC_NOT_OWNED(40007, "无权操作该文档"),

    // 报告解读 60xxx
    REPORT_FILE_EMPTY(60001, "上传文件为空"),
    REPORT_FILE_TYPE_UNSUPPORTED(60002, "仅支持 jpg/jpeg/png/pdf 格式"),
    REPORT_FILE_TOO_LARGE(60003, "文件过大，最大 10MB"),
    REPORT_OCR_FAILED(60004, "报告识别失败"),
    REPORT_NOT_FOUND(60005, "报告不存在"),
    REPORT_NOT_OWNED(60006, "无权操作该报告"),

    // 反馈评价 70xxx
    FEEDBACK_MESSAGE_NOT_FOUND(70001, "回答不存在"),
    FEEDBACK_MESSAGE_NOT_OWNED(70002, "无权评价该回答"),

    // 后台管理 80xxx
    ADMIN_ACCOUNT_NOT_FOUND(80001, "管理员账号不存在"),
    ADMIN_PASSWORD_ERROR(80002, "用户名或密码错误"),
    ADMIN_ACCOUNT_DISABLED(80003, "管理员账号已禁用"),
    ADMIN_ROLE_INVALID(80004, "管理员角色不合法"),
    ADMIN_USERNAME_EXISTS(80005, "管理员用户名已存在"),
    ADMIN_KNOWLEDGE_NOT_FOUND(80006, "知识条目不存在"),
    ADMIN_USER_NOT_FOUND(80007, "用户不存在"),
    ADMIN_FEEDBACK_NOT_FOUND(80008, "反馈不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
