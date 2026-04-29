package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 药品新增/修改请求 DTO
 */
public record DrugSaveDTO(
        @NotBlank(message = "药品编码不能为空") String drugCode,
        @NotBlank(message = "药品名称不能为空") String drugName,
        String genericName,
        Long categoryId,
        @NotBlank(message = "规格不能为空") String specification,
        String dosageForm,
        String manufacturer,
        String approvalNo,
        @NotNull(message = "价格不能为空") @DecimalMin(value = "0.00", message = "价格不能小于0") BigDecimal price,
        @NotNull(message = "库存不能为空") @Min(value = 0, message = "库存不能小于0") Integer stockQuantity,
        @NotNull(message = "预警阈值不能为空") @Min(value = 0, message = "预警阈值不能小于0") Integer warningThreshold,
        @NotNull(message = "处方药标识不能为空") Integer prescriptionRequired,
        @NotNull(message = "医保标识不能为空") Integer insuranceCovered,
        String usageInstruction,
        String contraindication,
        String adverseReaction,
        Integer status
) implements Serializable {
}
