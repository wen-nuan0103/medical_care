package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("medication_plan")
public class MedicationPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;
    private Long prescriptionId;
    private Long prescriptionItemId;
    private Long drugId;
    private String drugName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer timesPerDay;
    private String reminderTimes;
    private String dosage;
    private String usageMethod;
    private String aiReminderText;
    private String status;
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
