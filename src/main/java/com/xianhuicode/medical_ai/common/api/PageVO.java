package com.xianhuicode.medical_ai.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页返回。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    private List<T> records;

    private long total;

    private long page;

    private long size;
}
