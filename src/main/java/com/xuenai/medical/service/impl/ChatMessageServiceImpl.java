package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.ChatAttachmentMapper;
import com.xuenai.medical.mapper.ChatMessageMapper;
import com.xuenai.medical.mapper.ConsultationSessionMapper;
import com.xuenai.medical.mapper.UserMapper;
import com.xuenai.medical.model.entity.ChatAttachment;
import com.xuenai.medical.model.entity.ChatMessage;
import com.xuenai.medical.model.entity.ConsultationSession;
import com.xuenai.medical.model.entity.SysUser;
import com.xuenai.medical.model.vo.ChatMessageVO;
import com.xuenai.medical.service.ChatMessageService;
import com.xuenai.medical.service.ProfileResolveService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final ConsultationSessionMapper consultationSessionMapper;
    private final UserMapper userMapper;
    private final ChatAttachmentMapper chatAttachmentMapper;
    private final ProfileResolveService profileResolveService;

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        ConsultationSession session = consultationSessionMapper.selectById(message.getSessionId());
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "问诊会话不存在");
        }
        if (!"IN_PROGRESS".equals(session.getStatus()) && !"WAITING".equals(session.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前会话已结束，无法发送消息");
        }

        // 如果是等待中，且医生发消息了，状态改为进行中
        // senderId 是 sys_user.id, session.doctorId 是 doctor_profile.id, 需要转换
        if ("WAITING".equals(session.getStatus())) {
            Long doctorUserId = profileResolveService.getUserIdByDoctorProfileId(session.getDoctorId());
            if (message.getSenderId().equals(doctorUserId)) {
                session.setStatus("IN_PROGRESS");
                session.setStartTime(LocalDateTime.now());
                consultationSessionMapper.updateById(session);
            }
        }

        message.setSendTime(LocalDateTime.now());
        message.setReadStatus(0);
        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<ChatMessageVO> getHistoryMessages(Long sessionId, Long userId) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "问诊会话不存在");
        }

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .orderByAsc(ChatMessage::getSendTime);

        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);

        return messages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public ChatMessageVO convertToVO(ChatMessage msg) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(msg, vo);

        SysUser sender = userMapper.selectById(msg.getSenderId());
        if (sender != null) {
            vo.setSenderName(sender.getRealName());
            vo.setSenderAvatar(sender.getAvatarUrl());
        }

        if (msg.getAttachmentId() != null) {
            ChatAttachment attachment = chatAttachmentMapper.selectById(msg.getAttachmentId());
            if (attachment != null) {
                vo.setAttachmentUrl(attachment.getFileUrl());
            }
        }
        return vo;
    }
}
