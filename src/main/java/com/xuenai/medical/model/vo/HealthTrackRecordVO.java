package com.xuenai.medical.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HealthTrackRecordVO {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
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
    private String aiAnalysisText;
    private LocalDateTime createTime;
    private String remark;
}
