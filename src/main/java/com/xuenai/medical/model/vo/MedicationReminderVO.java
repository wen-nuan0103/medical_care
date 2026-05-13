package com.xuenai.medical.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicationReminderVO {

    private Long id;
    private Long planId;
    private Long patientId;
    private Long drugId;
    private String drugName;
    private String dosage;
    private String usageMethod;
    private String aiReminderText;
    private LocalDateTime remindTime;
    private String status;
    private LocalDateTime confirmTime;
    private LocalDateTime snoozeTime;
    private String feedback;
    private LocalDateTime createTime;
}
