package com.xuenai.medical.service;

import com.xuenai.medical.model.vo.SystemNotificationVO;

import java.util.List;

public interface SystemNotificationService {

    void create(Long receiverUserId,
                Long senderUserId,
                String title,
                String content,
                String notificationType,
                Long businessId);

    List<SystemNotificationVO> list(Long receiverUserId, Integer readStatus);

    SystemNotificationVO markRead(Long receiverUserId, Long notificationId);

    Long unreadCount(Long receiverUserId);
}
