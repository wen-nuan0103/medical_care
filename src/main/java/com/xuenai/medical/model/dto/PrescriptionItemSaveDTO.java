package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionItemSaveDTO {

    @NotNull(message = "药品ID不能为空")
    private Long drugId;

    @NotNull(message = "药品数量不能为空")
    @Min(value = 1, message = "药品数量至少为1")
    private Integer quantity;

    @NotBlank(message = "单次剂量不能为空")
    private String dosage;

    @NotBlank(message = "用药频次不能为空")
    private String frequency;

    @NotNull(message = "疗程天数不能为空")
    @Min(value = 1, message = "疗程天数至少为1")
    private Integer durationDays;

    @NotBlank(message = "用法不能为空")
    private String usageMethod;

    private String medicationTime;

    private String remark;
}
