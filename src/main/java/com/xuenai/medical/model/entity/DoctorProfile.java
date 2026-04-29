package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生档案实体，对应 doctor_profile 表
 */
@Data
@TableName("doctor_profile")
public class DoctorProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String hospital;

    private String department;

    private String title;

    private String licenseNo;

    private String specialty;

    private String introduction;

    private Integer consultationCount;

    private BigDecimal score;

    /** 接诊状态：0=下线 1=在线 */
    private Integer serviceStatus;

    /** 审核状态：PENDING / APPROVED / REJECTED */
    private String auditStatus;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
