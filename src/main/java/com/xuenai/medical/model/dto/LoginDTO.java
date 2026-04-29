package com.xuenai.medical.model.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 登录请求 DTO
 */
public record LoginDTO(
        @NotBlank(message = "账号不能为空") String username,
        @NotBlank(message = "密码不能为空") String password
) implements Serializable {
}
