package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 账号状态更新请求 DTO
 */
public record StatusUpdateDTO(
        @NotNull(message = "状态不能为空") Integer status
) implements Serializable {
}
