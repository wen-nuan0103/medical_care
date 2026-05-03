package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMedicineOrderDTO {

    @NotNull(message = "prescriptionId is required")
    private Long prescriptionId;

    @NotNull(message = "insuranceCardId is required")
    private Long insuranceCardId;
}
