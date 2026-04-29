package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateConsultationDTO {

    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    private String chiefComplaint;

    private String diseaseDesc;
}
