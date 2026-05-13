package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUpPlanSaveDTO {

    @NotNull(message = "patient id is required")
    private Long patientId;

    private Long sessionId;

    @NotNull(message = "plan time is required")
    @Future(message = "plan time must be in the future")
    private LocalDateTime planTime;

    @NotBlank(message = "follow-up content is required")
    private String content;

    private String remark;
}
