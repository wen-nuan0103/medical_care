package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayMedicineOrderDTO {

    @NotNull(message = "insuranceCardId is required")
    private Long insuranceCardId;
}
