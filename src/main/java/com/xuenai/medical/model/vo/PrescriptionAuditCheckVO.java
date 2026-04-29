package com.xuenai.medical.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class PrescriptionAuditCheckVO {

    private Long prescriptionId;
    private String riskLevel;
    private String stockResult;
    private String interactionResult;
    private String dosageResult;
    private String summary;
    private List<PrescriptionStockWarningVO> stockWarnings;
    private List<DrugInteractionRiskVO> interactionRisks;
}
