package com.xuenai.medical.common;

import java.io.Serializable;
import java.util.List;

public record PageResult<T>(
        List<T> records,
        long total,
        int current,
        int pageSize
) implements Serializable {

    public static <T> PageResult<T> of(List<T> records, long total, int current, int pageSize) {
        return new PageResult<>(records, total, current, pageSize);
    }
}

