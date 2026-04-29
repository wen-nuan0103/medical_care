package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("drug_interaction_rule")
public class DrugInteractionRule {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long drugAId;
    private Long drugBId;
    private String riskLevel;
    private String description;
    private String suggestion;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
