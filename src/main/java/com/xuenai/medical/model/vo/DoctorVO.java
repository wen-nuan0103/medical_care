package com.xuenai.medical.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生列表展示 VO
 */
public record DoctorVO(
        Long id,
        Long userId,
        String realName,
        String phone,
        String avatarUrl,
        String hospital,
        String department,
        String title,
        String specialty,
        Integer consultationCount,
        BigDecimal score,
        Integer serviceStatus,
        String auditStatus,
        LocalDateTime createTime
) implements Serializable {
}
