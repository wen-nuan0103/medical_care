package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("medicine_order_item")
public class MedicineOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long drugId;
    private String drugName;
    private String specification;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private Integer insuranceCovered;
    private BigDecimal insuranceAmount;
    private Long createBy;
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
