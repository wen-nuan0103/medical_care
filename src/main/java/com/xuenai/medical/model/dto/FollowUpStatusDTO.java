package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FollowUpStatusDTO {

    @NotBlank(message = "status is required")
    private String status;

    private String remark;
}
