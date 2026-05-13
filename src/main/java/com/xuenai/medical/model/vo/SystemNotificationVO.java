package com.xuenai.medical.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemNotificationVO {

    private Long id;
    private Long receiverId;
    private Long senderId;
    private String title;
    private String content;
    private String notificationType;
    private Long businessId;
    private Integer readStatus;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
