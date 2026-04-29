package com.xuenai.medical.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageVO {
    private Long id;
    private Long sessionId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;
    private String messageType;
    private String content;
    private Long attachmentId;
    private String attachmentUrl;
    private Integer readStatus;
    private LocalDateTime sendTime;
}
