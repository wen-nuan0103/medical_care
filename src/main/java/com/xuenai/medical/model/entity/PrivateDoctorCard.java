package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 患者私人医生卡表
 */
@Data
@TableName("private_doctor_card")
public class PrivateDoctorCard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;

    private Long doctorId;

    private Long planId;

    private Long orderId;

    private String cardType;

    private LocalDateTime startTime;

    private LocalDateTime expireTime;

    private Integer totalTimes;

    private Integer remainingTimes;

    private Integer totalMinutes;

    private Integer remainingMinutes;

    private Integer giftedMinutes;

    /** ACTIVE、EXPIRED、USED_UP、REFUNDED */
    private String status;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
