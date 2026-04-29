package com.xuenai.medical.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionVO {

    private Long id;
    private String prescriptionNo;
    private Long sessionId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String diagnosis;
    private String status;
    private LocalDateTime validUntil;
    private String doctorNote;
    private String patientInstruction;
    private LocalDateTime submitTime;
    private LocalDateTime approveTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private BigDecimal totalAmount;
    private List<PrescriptionItemVO> items;
    private PrescriptionAuditVO latestAudit;
}
