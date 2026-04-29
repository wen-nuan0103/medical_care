package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 药品库存更新请求 DTO
 */
public record StockUpdateDTO(
        @NotNull(message = "库存不能为空") @Min(value = 0, message = "库存不能小于0") Integer stockQuantity,
        String reason
) implements Serializable {
}
