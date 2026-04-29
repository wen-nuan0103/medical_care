package com.xuenai.medical.model.vo;

import lombok.Data;

@Data
public class DrugInteractionRiskVO {

    private Long ruleId;
    private Long drugAId;
    private String drugAName;
    private Long drugBId;
    private String drugBName;
    private String riskLevel;
    private String description;
    private String suggestion;
}
