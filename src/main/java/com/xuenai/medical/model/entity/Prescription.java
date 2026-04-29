package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prescription")
public class Prescription {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String prescriptionNo;
    private Long sessionId;
    private Long patientId;
    private Long doctorId;
    private String diagnosis;
    private String status;
    private LocalDateTime validUntil;
    private String doctorNote;
    private String patientInstruction;
    private Long aiExplainId;
    private LocalDateTime submitTime;
    private LocalDateTime approveTime;
    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
