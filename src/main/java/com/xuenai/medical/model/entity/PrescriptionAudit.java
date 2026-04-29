package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prescription_audit")
public class PrescriptionAudit {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long prescriptionId;
    private Long pharmacistId;
    private String auditResult;
    private String riskLevel;
    private String interactionResult;
    private String stockResult;
    private String dosageResult;
    private String advice;
    private Long aiSuggestionId;
    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
