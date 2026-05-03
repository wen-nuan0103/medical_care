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
@TableName("insurance_payment_record")
public class InsurancePaymentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String paymentNo;
    private Long orderId;
    private Long patientId;
    private Long insuranceCardId;
    private BigDecimal beforeBalance;
    private BigDecimal paidAmount;
    private BigDecimal afterBalance;
    private String status;
    private String failReason;
    private LocalDateTime payTime;
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
