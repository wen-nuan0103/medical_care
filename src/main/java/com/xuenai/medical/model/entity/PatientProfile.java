package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者资料实体，对应 patient_profile 表
 */
@Data
@TableName("patient_profile")
public class PatientProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String idCardNo;

    private LocalDate birthday;

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    private String allergyHistory;

    private String medicalHistory;

    private String familyHistory;

    private String emergencyContact;

    private String emergencyPhone;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
