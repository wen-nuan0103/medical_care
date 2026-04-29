package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.ChatTimeGiftMapper;
import com.xuenai.medical.mapper.ConsultationSessionMapper;
import com.xuenai.medical.mapper.DoctorMapper;
import com.xuenai.medical.mapper.UserMapper;
import com.xuenai.medical.model.dto.CreateConsultationDTO;
import com.xuenai.medical.model.dto.GiftTimeDTO;
import com.xuenai.medical.model.entity.ChatTimeGift;
import com.xuenai.medical.model.entity.ConsultationSession;
import com.xuenai.medical.model.entity.DoctorProfile;
import com.xuenai.medical.model.entity.PrivateDoctorCard;
import com.xuenai.medical.model.entity.SysUser;
import com.xuenai.medical.model.vo.ConsultationSessionVO;
import com.xuenai.medical.model.vo.DoctorDetailVO;
import com.xuenai.medical.service.ConsultationSessionService;
import com.xuenai.medical.service.PrivateDoctorCardService;
import com.xuenai.medical.service.ProfileResolveService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationSessionServiceImpl implements ConsultationSessionService {

    private final ConsultationSessionMapper consultationSessionMapper;
    private final PrivateDoctorCardService privateDoctorCardService;
    private final ChatTimeGiftMapper chatTimeGiftMapper;
    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;
    private final ProfileResolveService profileResolveService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsultationSession createSession(Long patientProfileId, CreateConsultationDTO dto) {
        // 1. 获取有效卡（patientProfileId 和 doctorId 都是 profile 层面的 ID）
        PrivateDoctorCard activeCard = privateDoctorCardService.getActiveCard(patientProfileId, dto.getDoctorId());
        if (activeCard == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "您没有该医生的有效服务卡，或次数已用完");
        }

        // 2. 扣减次数
        activeCard.setRemainingTimes(activeCard.getRemainingTimes() - 1);
        int allowedMinutes = activeCard.getRemainingMinutes();
        if (allowedMinutes <= 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "您的服务卡聊天时长已耗尽");
        }

        privateDoctorCardService.updateCard(activeCard);

        // 3. 创建会话
        ConsultationSession session = new ConsultationSession();
        session.setSessionNo(UUID.randomUUID().toString().replace("-", ""));
        session.setPatientId(patientProfileId);
        session.setDoctorId(dto.getDoctorId());
        session.setCardId(activeCard.getId());
        session.setChiefComplaint(dto.getChiefComplaint());
        session.setDiseaseDesc(dto.getDiseaseDesc());
        session.setStatus("WAITING");
        session.setAllowedMinutes(allowedMinutes);
        session.setUsedMinutes(0);

        consultationSessionMapper.insert(session);
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endSession(Long sessionId, Long userId) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "问诊会话不存在");
        }
        if (!"WAITING".equals(session.getStatus()) && !"IN_PROGRESS".equals(session.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前会话状态无法结束");
        }

        // 鉴权：需要将 userId 转换为 profileId 来比对
        // session.patientId 是 patient_profile.id，session.doctorId 是 doctor_profile.id
        Long doctorUserId = profileResolveService.getUserIdByDoctorProfileId(session.getDoctorId());
        Long patientUserId = profileResolveService.getUserIdByPatientProfileId(session.getPatientId());
        if (!userId.equals(doctorUserId) && !userId.equals(patientUserId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权结束此会话");
        }

        session.setStatus("ENDED");
        session.setEndTime(LocalDateTime.now());
        consultationSessionMapper.updateById(session);
    }

    @Override
    public List<ConsultationSessionVO> listSessions(Long profileId, String role) {
        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        if ("DOCTOR".equals(role)) {
            // profileId 已经是 doctor_profile.id
            wrapper.eq(ConsultationSession::getDoctorId, profileId);
        } else {
            // profileId 已经是 patient_profile.id
            wrapper.eq(ConsultationSession::getPatientId, profileId);
        }
        wrapper.orderByDesc(ConsultationSession::getCreateTime);

        return consultationSessionMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsultationSessionVO getSessionDetail(Long sessionId, Long userId) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "问诊会话不存在");
        }
        return convertToVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void giftTime(Long sessionId, Long doctorProfileId, GiftTimeDTO dto) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null || !session.getDoctorId().equals(doctorProfileId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在或无权操作");
        }

        // 增加会话允许的时长
        session.setAllowedMinutes(session.getAllowedMinutes() + dto.getMinutes());
        consultationSessionMapper.updateById(session);

        // 记录赠送
        ChatTimeGift gift = new ChatTimeGift();
        gift.setSessionId(sessionId);
        gift.setCardId(session.getCardId());
        gift.setDoctorId(session.getDoctorId());
        gift.setPatientId(session.getPatientId());
        gift.setMinutes(dto.getMinutes());
        gift.setReason(dto.getReason());
        chatTimeGiftMapper.insert(gift);
    }

    @Override
    public void updateUsedMinutes(Long sessionId, int minutesToAdd) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session != null && "IN_PROGRESS".equals(session.getStatus())) {
            session.setUsedMinutes(session.getUsedMinutes() + minutesToAdd);
            consultationSessionMapper.updateById(session);
        }
    }

    private ConsultationSessionVO convertToVO(ConsultationSession session) {
        ConsultationSessionVO vo = new ConsultationSessionVO();
        BeanUtils.copyProperties(session, vo);

        // patient_profile.id → sys_user 获取姓名和头像
        Long patientUserId = profileResolveService.getUserIdByPatientProfileId(session.getPatientId());
        SysUser patient = userMapper.selectById(patientUserId);
        if (patient != null) {
            vo.setPatientName(patient.getRealName());
            vo.setPatientAvatar(patient.getAvatarUrl());
        }

        // doctor_profile.id → 使用已有的 selectDoctorDetailById
        DoctorDetailVO doctor = doctorMapper.selectDoctorDetailById(session.getDoctorId());
        if (doctor != null) {
            vo.setDoctorName(doctor.realName());
            vo.setDoctorAvatar(doctor.avatarUrl());
        }

        return vo;
    }
}
