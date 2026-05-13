package com.xuenai.medical.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicationPlanVO {

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
    private LocalDateTime createTime;
    private List<MedicationReminderVO> reminders;
}
