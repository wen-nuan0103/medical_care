package com.xuenai.medical.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品列表展示 VO
 */
public record DrugVO(
        Long id,
        String drugCode,
        String drugName,
        String genericName,
        Long categoryId,
        String categoryName,
        String specification,
        String dosageForm,
        String manufacturer,
        BigDecimal price,
        Integer stockQuantity,
        Integer warningThreshold,
        Integer prescriptionRequired,
        Integer insuranceCovered,
        Integer status,
        LocalDateTime createTime
) implements Serializable {
}
