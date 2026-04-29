package com.xuenai.medical.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天附件表
 */
@Data
@TableName("chat_attachment")
public class ChatAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long uploaderId;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
