package com.xuenai.medical.model.vo;

import lombok.Data;

@Data
public class PrescriptionStockWarningVO {

    private Long drugId;
    private String drugName;
    private Integer requestedQuantity;
    private Integer currentStock;
    private Integer warningThreshold;
    private Boolean stockEnough;
    private String warningMessage;
}
