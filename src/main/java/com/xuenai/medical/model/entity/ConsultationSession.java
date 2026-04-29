package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问诊会话表
 */
@Data
@TableName("consultation_session")
public class ConsultationSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionNo;

    private Long patientId;

    private Long doctorId;

    private Long cardId;

    private String chiefComplaint;

    private String diseaseDesc;

    /** WAITING、IN_PROGRESS、ENDED、CANCELED */
    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer allowedMinutes;

    private Integer usedMinutes;

    private String summary;

    private Long aiSummaryId;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
