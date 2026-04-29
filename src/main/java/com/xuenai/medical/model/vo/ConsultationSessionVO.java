package com.xuenai.medical.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConsultationSessionVO {
    private Long id;
    private String sessionNo;
    private Long patientId;
    private String patientName;
    private String patientAvatar;
    private Long doctorId;
    private String doctorName;
    private String doctorAvatar;
    private String chiefComplaint;
    private String diseaseDesc;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer allowedMinutes;
    private Integer usedMinutes;
    private String summary;
    private LocalDateTime createTime;
}
