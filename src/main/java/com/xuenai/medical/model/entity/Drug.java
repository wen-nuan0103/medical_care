package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品实体，对应 drug 表
 */
@Data
@TableName("drug")
public class Drug {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String drugCode;

    private String drugName;

    private String genericName;

    private Long categoryId;

    private String specification;

    private String dosageForm;

    private String manufacturer;

    private String approvalNo;

    private BigDecimal price;

    private Integer stockQuantity;

    private Integer warningThreshold;

    /** 是否处方药：0=否 1=是 */
    private Integer prescriptionRequired;

    /** 是否医保：0=否 1=是 */
    private Integer insuranceCovered;

    private String usageInstruction;

    private String contraindication;

    private String adverseReaction;

    /** 状态：0=下架 1=上架 */
    private Integer status;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
