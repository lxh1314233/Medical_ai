package com.xianhuicode.medical_ai.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史消息项。
 */
@Data
public class MessageVO {

    private Long id;

    /** user / assistant */
    private String role;

    private String content;

    private List<RefVO> refs;

    private LocalDateTime createTime;
}
