package com.xuenai.medical.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PrivateDoctorCardVO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private String doctorAvatar;
    private String cardType;
    private String planName;
    private LocalDateTime startTime;
    private LocalDateTime expireTime;
    private Integer totalTimes;
    private Integer remainingTimes;
    private Integer totalMinutes;
    private Integer remainingMinutes;
    private Integer giftedMinutes;
    private String status;
}
