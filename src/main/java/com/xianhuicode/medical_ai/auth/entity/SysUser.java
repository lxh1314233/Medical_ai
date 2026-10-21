package com.xianhuicode.medical_ai.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 sys_user 表。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String nickname;

    /** BCrypt 密文 */
    private String password;

    /** 1 正常 0 禁用 */
    private Integer status;

    /** PASSWORD / SMS / WECHAT */
    private String registerType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
