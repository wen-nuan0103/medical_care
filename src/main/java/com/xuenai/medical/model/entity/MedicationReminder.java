package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("medication_reminder")
public class MedicationReminder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;
    private Long patientId;
    private Long drugId;
    private LocalDateTime remindTime;
    private String status;
    private LocalDateTime confirmTime;
    private LocalDateTime snoozeTime;
    private String feedback;
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
