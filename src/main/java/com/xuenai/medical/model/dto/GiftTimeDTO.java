package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GiftTimeDTO {

    @NotNull(message = "赠送时长不能为空")
    @Min(value = 1, message = "赠送时长至少为1分钟")
    private Integer minutes;

    @NotBlank(message = "赠送原因不能为空")
    private String reason;
}
