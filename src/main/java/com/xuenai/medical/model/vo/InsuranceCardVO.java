package com.xuenai.medical.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InsuranceCardVO {

    private Long id;
    private Long patientId;
    private String cardNo;
    private String holderName;
    private String holderIdCard;
    private BigDecimal balance;
    private BigDecimal reimbursementRate;
    private String status;
    private LocalDateTime bindTime;
}
