package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorCardPlanSaveDTO {
    
    @NotBlank(message = "卡型不能为空")
    private String cardType;

    @NotBlank(message = "卡名称不能为空")
    private String planName;

    @NotNull(message = "价格不能为空")
    @Min(value = 0, message = "价格不能小于0")
    private BigDecimal price;

    @NotNull(message = "有效天数不能为空")
    @Min(value = 1, message = "有效天数不能小于1")
    private Integer validDays;

    @NotNull(message = "问诊次数不能为空")
    @Min(value = 1, message = "问诊次数不能小于1")
    private Integer consultationTimes;

    @NotNull(message = "总聊天时长不能为空")
    @Min(value = 1, message = "总聊天时长不能小于1")
    private Integer totalMinutes;

    @NotNull(message = "单次聊天时长上限不能为空")
    @Min(value = 1, message = "单次聊天时长上限不能小于1")
    private Integer singleMinutes;

    private Integer giftLimitMinutes;

    private String description;

    private Integer status;
}
