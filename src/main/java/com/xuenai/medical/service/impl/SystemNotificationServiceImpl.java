package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.SystemNotificationMapper;
import com.xuenai.medical.model.entity.SystemNotification;
import com.xuenai.medical.model.vo.SystemNotificationVO;
import com.xuenai.medical.service.SystemNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemNotificationServiceImpl implements SystemNotificationService {

    private final SystemNotificationMapper systemNotificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long receiverUserId,
                       Long senderUserId,
                       String title,
                       String content,
                       String notificationType,
                       Long businessId) {
        if (receiverUserId == null) {
            return;
        }
        SystemNotification notification = new SystemNotification();
        notification.setReceiverId(receiverUserId);
        notification.setSenderId(senderUserId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(notificationType);
        notification.setBusinessId(businessId);
        notification.setReadStatus(0);
        notification.setCreateBy(senderUserId);
        notification.setUpdateBy(senderUserId);
        systemNotificationMapper.insert(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemNotificationVO> list(Long receiverUserId, Integer readStatus) {
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getReceiverId, receiverUserId);
        if (readStatus != null) {
            wrapper.eq(SystemNotification::getReadStatus, readStatus);
        }
        wrapper.orderByDesc(SystemNotification::getCreateTime);
        return systemNotificationMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemNotificationVO markRead(Long receiverUserId, Long notificationId) {
        SystemNotification notification = systemNotificationMapper.selectById(notificationId);
        if (notification == null || !receiverUserId.equals(notification.getReceiverId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "notification not found");
        }
        notification.setReadStatus(1);
        notification.setReadTime(LocalDateTime.now());
        notification.setUpdateBy(UserContext.getUserId());
        systemNotificationMapper.updateById(notification);
        return toVO(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Long unreadCount(Long receiverUserId) {
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getReceiverId, receiverUserId)
                .eq(SystemNotification::getReadStatus, 0);
        return systemNotificationMapper.selectCount(wrapper);
    }

    private SystemNotificationVO toVO(SystemNotification notification) {
        SystemNotificationVO vo = new SystemNotificationVO();
        BeanUtils.copyProperties(notification, vo);
        return vo;
    }
}
