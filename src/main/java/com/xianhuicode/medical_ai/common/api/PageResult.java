package com.xianhuicode.medical_ai.common.api;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果。
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {

    private long total;
    private long pageNum;
    private long pageSize;
    private List<T> list;

    public static <T> PageResult<T> of(long total, long pageNum, long pageSize, List<T> list) {
        PageResult<T> p = new PageResult<>();
        p.total = total;
        p.pageNum = pageNum;
        p.pageSize = pageSize;
        p.list = list;
        return p;
    }
}
