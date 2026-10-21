package com.xianhuicode.medical_ai.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户禁用/解禁请求。
 */
@Data
public class UserStatusReq {

    /** 1 正常 0 禁用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
