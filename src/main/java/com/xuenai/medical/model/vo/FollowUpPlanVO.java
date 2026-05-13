package com.xuenai.medical.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUpPlanVO {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long sessionId;
    private LocalDateTime planTime;
    private String content;
    private String status;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private String remark;
}
