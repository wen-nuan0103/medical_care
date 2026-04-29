package com.xuenai.medical.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生详情 VO
 */
public record DoctorDetailVO(
        Long id,
        Long userId,
        String realName,
        String phone,
        String avatarUrl,
        String hospital,
        String department,
        String title,
        String licenseNo,
        String specialty,
        String introduction,
        Integer consultationCount,
        BigDecimal score,
        Integer serviceStatus,
        String auditStatus,
        LocalDateTime createTime
) implements Serializable {
}
