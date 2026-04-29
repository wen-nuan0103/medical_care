package com.xuenai.medical.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrescriptionAuditVO {

    private Long id;
    private Long prescriptionId;
    private Long pharmacistId;
    private String pharmacistName;
    private String auditResult;
    private String riskLevel;
    private String interactionResult;
    private String stockResult;
    private String dosageResult;
    private String advice;
    private LocalDateTime auditTime;
}
