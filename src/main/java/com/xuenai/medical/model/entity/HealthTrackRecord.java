package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("health_track_record")
public class HealthTrackRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;
    private Long doctorId;
    private LocalDate recordDate;
    private String symptom;
    private BigDecimal temperature;
    private Integer systolicPressure;
    private Integer diastolicPressure;
    private Integer heartRate;
    private BigDecimal bloodGlucose;
    private String medicationFeedback;
    private Integer abnormalFlag;
    private Long aiAnalysisId;
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
