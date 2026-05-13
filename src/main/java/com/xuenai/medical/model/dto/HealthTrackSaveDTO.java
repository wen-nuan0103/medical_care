package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HealthTrackSaveDTO {

    private Long doctorId;

    private LocalDate recordDate;

    private String symptom;

    @DecimalMin(value = "30.0", message = "temperature is too low")
    @DecimalMax(value = "45.0", message = "temperature is too high")
    private BigDecimal temperature;

    @Min(value = 50, message = "systolic pressure is too low")
    @Max(value = 260, message = "systolic pressure is too high")
    private Integer systolicPressure;

    @Min(value = 30, message = "diastolic pressure is too low")
    @Max(value = 180, message = "diastolic pressure is too high")
    private Integer diastolicPressure;

    @Min(value = 30, message = "heart rate is too low")
    @Max(value = 220, message = "heart rate is too high")
    private Integer heartRate;

    @DecimalMin(value = "1.0", message = "blood glucose is too low")
    @DecimalMax(value = "40.0", message = "blood glucose is too high")
    private BigDecimal bloodGlucose;

    private String medicationFeedback;

    private String remark;
}
