package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InsuranceCardBindDTO {

    @NotBlank(message = "cardNo is required")
    private String cardNo;

    @NotBlank(message = "holderName is required")
    private String holderName;

    private String holderIdCard;

    @NotNull(message = "balance is required")
    @DecimalMin(value = "0.00", message = "balance cannot be negative")
    private BigDecimal balance;

    @DecimalMin(value = "0.00", message = "reimbursementRate cannot be negative")
    @DecimalMax(value = "1.00", message = "reimbursementRate cannot exceed 1")
    private BigDecimal reimbursementRate;
}
