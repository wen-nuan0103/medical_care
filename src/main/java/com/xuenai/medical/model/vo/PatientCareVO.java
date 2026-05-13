package com.xuenai.medical.model.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientCareVO {

    private Long patientId;
    private Long userId;
    private String patientName;
    private String phone;
    private String latestSymptom;
    private LocalDate latestRecordDate;
    private Long abnormalRecordCount;
    private Long activeMedicationPlanCount;
    private Long pendingFollowUpCount;
}
