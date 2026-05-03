package com.xuenai.medical.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MedicineOrderItemVO {

    private Long id;
    private Long orderId;
    private Long drugId;
    private String drugName;
    private String specification;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private Integer insuranceCovered;
    private BigDecimal insuranceAmount;
}
