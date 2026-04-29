package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseCardDTO {

    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @NotNull(message = "服务卡配置ID不能为空")
    private Long planId;
}
