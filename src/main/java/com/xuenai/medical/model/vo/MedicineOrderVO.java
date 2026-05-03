package com.xuenai.medical.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicineOrderVO {

    private Long id;
    private String orderNo;
    private Long patientId;
    private Long prescriptionId;
    private String prescriptionNo;
    private String diagnosis;
    private Long insuranceCardId;
    private String insuranceCardNo;
    private BigDecimal totalAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal selfAmount;
    private String status;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private List<MedicineOrderItemVO> items;
    private InsurancePaymentRecordVO paymentRecord;
}
