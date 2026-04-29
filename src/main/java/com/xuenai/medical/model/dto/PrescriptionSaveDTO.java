package com.xuenai.medical.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PrescriptionSaveDTO {

    @NotNull(message = "问诊会话ID不能为空")
    private Long sessionId;

    @NotBlank(message = "诊断不能为空")
    private String diagnosis;

    private String doctorNote;

    private String patientInstruction;

    private Boolean submit;

    @Valid
    @NotEmpty(message = "处方明细不能为空")
    private List<PrescriptionItemSaveDTO> items;
}
