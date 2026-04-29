package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问诊时长赠送记录表
 */
@Data
@TableName("chat_time_gift")
public class ChatTimeGift {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long cardId;

    private Long doctorId;

    private Long patientId;

    private Integer minutes;

    private String reason;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
