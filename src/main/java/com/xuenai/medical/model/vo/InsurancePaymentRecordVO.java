package com.xuenai.medical.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InsurancePaymentRecordVO {

    private Long id;
    private String paymentNo;
    private Long orderId;
    private Long patientId;
    private Long insuranceCardId;
    private BigDecimal beforeBalance;
    private BigDecimal paidAmount;
    private BigDecimal afterBalance;
    private String status;
    private String failReason;
    private LocalDateTime payTime;
}
