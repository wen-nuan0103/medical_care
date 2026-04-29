package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 私人医生卡购买订单表
 */
@Data
@TableName("doctor_card_order")
public class DoctorCardOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long patientId;

    private Long doctorId;

    private Long planId;

    private String cardType;

    private String planName;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private String payType;

    /** UNPAID、PAID、CANCELED、REFUNDED */
    private String status;

    private LocalDateTime payTime;

    private LocalDateTime cancelTime;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
