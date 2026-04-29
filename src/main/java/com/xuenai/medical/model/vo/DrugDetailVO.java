package com.xuenai.medical.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品详情 VO
 */
public record DrugDetailVO(
        Long id,
        String drugCode,
        String drugName,
        String genericName,
        Long categoryId,
        String categoryName,
        String specification,
        String dosageForm,
        String manufacturer,
        String approvalNo,
        BigDecimal price,
        Integer stockQuantity,
        Integer warningThreshold,
        Integer prescriptionRequired,
        Integer insuranceCovered,
        String usageInstruction,
        String contraindication,
        String adverseReaction,
        Integer status,
        LocalDateTime createTime
) implements Serializable {
}
