package com.xuenai.medical.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrescriptionItemVO {

    private Long id;
    private Long prescriptionId;
    private Long drugId;
    private String drugName;
    private String specification;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private String usageMethod;
    private String medicationTime;
    private String remark;
    private Integer currentStock;
    private Integer warningThreshold;
    private Integer prescriptionRequired;
    private Integer insuranceCovered;
    private String stockStatus;
}
