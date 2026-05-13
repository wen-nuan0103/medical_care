package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class MedicationReminderActionDTO {

    private String feedback;

    @Min(value = 5, message = "snooze minutes must be at least 5")
    @Max(value = 240, message = "snooze minutes must be no more than 240")
    private Integer snoozeMinutes;
}
