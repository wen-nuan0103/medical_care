package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prescription_item")
public class PrescriptionItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long prescriptionId;
    private Long drugId;
    private String drugName;
    private String specification;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private String usageMethod;
    private String medicationTime;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
