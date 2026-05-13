package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.ConsultationSessionMapper;
import com.xuenai.medical.mapper.DoctorMapper;
import com.xuenai.medical.mapper.FollowUpPlanMapper;
import com.xuenai.medical.mapper.PatientProfileMapper;
import com.xuenai.medical.mapper.PrivateDoctorCardMapper;
import com.xuenai.medical.mapper.UserMapper;
import com.xuenai.medical.model.dto.FollowUpPlanSaveDTO;
import com.xuenai.medical.model.dto.FollowUpStatusDTO;
import com.xuenai.medical.model.entity.ConsultationSession;
import com.xuenai.medical.model.entity.FollowUpPlan;
import com.xuenai.medical.model.entity.PatientProfile;
import com.xuenai.medical.model.entity.PrivateDoctorCard;
import com.xuenai.medical.model.entity.SysUser;
import com.xuenai.medical.model.vo.DoctorDetailVO;
import com.xuenai.medical.model.vo.FollowUpPlanVO;
import com.xuenai.medical.service.FollowUpPlanService;
import com.xuenai.medical.service.ProfileResolveService;
import com.xuenai.medical.service.SystemNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowUpPlanServiceImpl implements FollowUpPlanService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_DONE = "DONE";
    private static final String STATUS_CANCELED = "CANCELED";

    private final FollowUpPlanMapper followUpPlanMapper;
    private final PrivateDoctorCardMapper privateDoctorCardMapper;
    private final ConsultationSessionMapper consultationSessionMapper;
    private final PatientProfileMapper patientProfileMapper;
    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;
    private final ProfileResolveService profileResolveService;
    private final SystemNotificationService systemNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FollowUpPlanVO create(Long doctorProfileId, FollowUpPlanSaveDTO dto) {
        if (!hasDoctorPatientRelation(doctorProfileId, dto.getPatientId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission for this patient");
        }
        validateSession(dto.getSessionId(), doctorProfileId, dto.getPatientId());

        FollowUpPlan plan = new FollowUpPlan();
        plan.setDoctorId(doctorProfileId);
        plan.setPatientId(dto.getPatientId());
        plan.setSessionId(dto.getSessionId());
        plan.setPlanTime(dto.getPlanTime());
        plan.setContent(dto.getContent());
        plan.setStatus(STATUS_PENDING);
        plan.setRemark(dto.getRemark());
        plan.setCreateBy(UserContext.getUserId());
        plan.setUpdateBy(UserContext.getUserId());
        followUpPlanMapper.insert(plan);

        systemNotificationService.create(
                profileResolveService.getUserIdByPatientProfileId(dto.getPatientId()),
                UserContext.getUserId(),
                "New follow-up plan",
                "Your doctor created a follow-up plan. Please check the time and content.",
                "FOLLOW_UP",
                plan.getId()
        );
        return toVO(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpPlanVO> listForDoctor(Long doctorProfileId, Long patientProfileId, String status) {
        LambdaQueryWrapper<FollowUpPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUpPlan::getDoctorId, doctorProfileId);
        if (patientProfileId != null) {
            wrapper.eq(FollowUpPlan::getPatientId, patientProfileId);
        }
        if (hasText(status)) {
            wrapper.eq(FollowUpPlan::getStatus, status);
        }
        wrapper.orderByDesc(FollowUpPlan::getPlanTime);
        return followUpPlanMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpPlanVO> listForPatient(Long patientProfileId, String status) {
        LambdaQueryWrapper<FollowUpPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUpPlan::getPatientId, patientProfileId);
        if (hasText(status)) {
            wrapper.eq(FollowUpPlan::getStatus, status);
        }
        wrapper.orderByDesc(FollowUpPlan::getPlanTime);
        return followUpPlanMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FollowUpPlanVO updateStatus(Long doctorProfileId, Long followUpId, FollowUpStatusDTO dto) {
        FollowUpPlan plan = followUpPlanMapper.selectById(followUpId);
        if (plan == null || !doctorProfileId.equals(plan.getDoctorId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "follow-up plan not found");
        }
        String status = normalizeStatus(dto.getStatus());
        plan.setStatus(status);
        plan.setUpdateBy(UserContext.getUserId());
        if (STATUS_DONE.equals(status)) {
            plan.setFinishTime(LocalDateTime.now());
        }
        if (hasText(dto.getRemark())) {
            plan.setRemark(dto.getRemark());
        }
        followUpPlanMapper.updateById(plan);

        systemNotificationService.create(
                profileResolveService.getUserIdByPatientProfileId(plan.getPatientId()),
                UserContext.getUserId(),
                "Follow-up plan updated",
                "Your doctor updated a follow-up plan status to " + status + ".",
                "FOLLOW_UP_STATUS",
                plan.getId()
        );
        return toVO(plan);
    }

    private void validateSession(Long sessionId, Long doctorProfileId, Long patientProfileId) {
        if (sessionId == null) {
            return;
        }
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null || !doctorProfileId.equals(session.getDoctorId())
                || !patientProfileId.equals(session.getPatientId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "consultation session does not match patient");
        }
    }

    private boolean hasDoctorPatientRelation(Long doctorProfileId, Long patientProfileId) {
        LambdaQueryWrapper<PrivateDoctorCard> cardWrapper = new LambdaQueryWrapper<>();
        cardWrapper.eq(PrivateDoctorCard::getDoctorId, doctorProfileId)
                .eq(PrivateDoctorCard::getPatientId, patientProfileId)
                .last("LIMIT 1");
        if (privateDoctorCardMapper.selectOne(cardWrapper) != null) {
            return true;
        }
        LambdaQueryWrapper<ConsultationSession> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(ConsultationSession::getDoctorId, doctorProfileId)
                .eq(ConsultationSession::getPatientId, patientProfileId)
                .last("LIMIT 1");
        return consultationSessionMapper.selectOne(sessionWrapper) != null;
    }

    private FollowUpPlanVO toVO(FollowUpPlan plan) {
        FollowUpPlanVO vo = new FollowUpPlanVO();
        BeanUtils.copyProperties(plan, vo);
        vo.setPatientName(resolvePatientName(plan.getPatientId()));
        vo.setDoctorName(resolveDoctorName(plan.getDoctorId()));
        return vo;
    }

    private String normalizeStatus(String status) {
        if (STATUS_PENDING.equals(status) || STATUS_DONE.equals(status) || STATUS_CANCELED.equals(status)) {
            return status;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "follow-up status must be PENDING, DONE or CANCELED");
    }

    private String resolvePatientName(Long patientProfileId) {
        PatientProfile profile = patientProfileMapper.selectById(patientProfileId);
        if (profile == null) {
            return null;
        }
        SysUser user = userMapper.selectById(profile.getUserId());
        return user == null ? null : user.getRealName();
    }

    private String resolveDoctorName(Long doctorProfileId) {
        DoctorDetailVO doctor = doctorMapper.selectDoctorDetailById(doctorProfileId);
        return doctor == null ? null : doctor.realName();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
