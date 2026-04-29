package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生服务卡配置表
 */
@Data
@TableName("doctor_card_plan")
public class DoctorCardPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long doctorId;

    /** ONCE、MONTH、QUARTER、HALF_YEAR、YEAR */
    private String cardType;

    private String planName;

    private BigDecimal price;

    private Integer validDays;

    private Integer consultationTimes;

    private Integer totalMinutes;

    private Integer singleMinutes;

    private Integer giftLimitMinutes;

    private String description;

    /** 0下架，1上架 */
    private Integer status;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
