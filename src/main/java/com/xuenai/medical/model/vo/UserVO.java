package com.xuenai.medical.model.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表展示 VO
 */
public record UserVO(
        Long id,
        String username,
        String realName,
        String phone,
        String email,
        Integer status,
        List<String> roles,
        LocalDateTime lastLoginTime,
        LocalDateTime createTime
) implements Serializable {
}
