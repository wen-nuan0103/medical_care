package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrescriptionAuditDTO {

    @NotBlank(message = "审核结果不能为空")
    private String auditResult;

    private String riskLevel;

    private String dosageResult;

    private String advice;
}
