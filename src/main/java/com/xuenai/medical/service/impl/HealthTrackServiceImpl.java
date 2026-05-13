package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.AiSuggestionLogMapper;
import com.xuenai.medical.mapper.ConsultationSessionMapper;
import com.xuenai.medical.mapper.DoctorMapper;
import com.xuenai.medical.mapper.FollowUpPlanMapper;
import com.xuenai.medical.mapper.HealthTrackRecordMapper;
import com.xuenai.medical.mapper.MedicationPlanMapper;
import com.xuenai.medical.mapper.PatientProfileMapper;
import com.xuenai.medical.mapper.PrivateDoctorCardMapper;
import com.xuenai.medical.mapper.UserMapper;
import com.xuenai.medical.model.dto.HealthTrackSaveDTO;
import com.xuenai.medical.model.entity.AiSuggestionLog;
import com.xuenai.medical.model.entity.ConsultationSession;
import com.xuenai.medical.model.entity.FollowUpPlan;
import com.xuenai.medical.model.entity.HealthTrackRecord;
import com.xuenai.medical.model.entity.MedicationPlan;
import com.xuenai.medical.model.entity.PatientProfile;
import com.xuenai.medical.model.entity.PrivateDoctorCard;
import com.xuenai.medical.model.entity.SysUser;
import com.xuenai.medical.model.vo.DoctorDetailVO;
import com.xuenai.medical.model.vo.HealthTrackRecordVO;
import com.xuenai.medical.model.vo.PatientCareVO;
import com.xuenai.medical.service.AiAssistantService;
import com.xuenai.medical.service.HealthTrackService;
import com.xuenai.medical.service.ProfileResolveService;
import com.xuenai.medical.service.SystemNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HealthTrackServiceImpl implements HealthTrackService {

    private final HealthTrackRecordMapper healthTrackRecordMapper;
    private final ConsultationSessionMapper consultationSessionMapper;
    private final PrivateDoctorCardMapper privateDoctorCardMapper;
    private final PatientProfileMapper patientProfileMapper;
    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;
    private final MedicationPlanMapper medicationPlanMapper;
    private final FollowUpPlanMapper followUpPlanMapper;
    private final AiSuggestionLogMapper aiSuggestionLogMapper;
    private final AiAssistantService aiAssistantService;
    private final ProfileResolveService profileResolveService;
    private final SystemNotificationService systemNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthTrackRecordVO create(Long patientProfileId, HealthTrackSaveDTO dto) {
        Long doctorId = dto.getDoctorId() == null ? resolveLatestDoctorId(patientProfileId) : dto.getDoctorId();
        if (doctorId != null && !hasDoctorPatientRelation(doctorId, patientProfileId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "doctor is not related to this patient");
        }

        HealthTrackRecord record = new HealthTrackRecord();
        BeanUtils.copyProperties(dto, record);
        record.setPatientId(patientProfileId);
        record.setDoctorId(doctorId);
        record.setRecordDate(dto.getRecordDate() == null ? LocalDate.now() : dto.getRecordDate());
        record.setAbnormalFlag(isAbnormal(dto) ? 1 : 0);
        record.setCreateBy(UserContext.getUserId());
        record.setUpdateBy(UserContext.getUserId());
        healthTrackRecordMapper.insert(record);

        AiSuggestionLog aiLog = createAiAnalysis(record);
        record.setAiAnalysisId(aiLog.getId());
        healthTrackRecordMapper.updateById(record);

        if (Integer.valueOf(1).equals(record.getAbnormalFlag()) && doctorId != null) {
            systemNotificationService.create(
                    profileResolveService.getUserIdByDoctorProfileId(doctorId),
                    UserContext.getUserId(),
                    "Patient health record needs attention",
                    "A patient submitted an abnormal health tracking record.",
                    "HEALTH_TRACK_ABNORMAL",
                    record.getId()
            );
        }
        return toVO(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthTrackRecordVO> listForPatient(Long patientProfileId) {
        LambdaQueryWrapper<HealthTrackRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthTrackRecord::getPatientId, patientProfileId)
                .orderByDesc(HealthTrackRecord::getRecordDate)
                .orderByDesc(HealthTrackRecord::getCreateTime);
        return healthTrackRecordMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthTrackRecordVO> listForDoctor(Long doctorProfileId, Long patientProfileId) {
        if (!hasDoctorPatientRelation(doctorProfileId, patientProfileId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission for this patient");
        }
        LambdaQueryWrapper<HealthTrackRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthTrackRecord::getPatientId, patientProfileId)
                .eq(HealthTrackRecord::getDoctorId, doctorProfileId)
                .orderByDesc(HealthTrackRecord::getRecordDate)
                .orderByDesc(HealthTrackRecord::getCreateTime);
        return healthTrackRecordMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientCareVO> listDoctorPatients(Long doctorProfileId) {
        Set<Long> patientIds = new LinkedHashSet<>();
        privateDoctorCardMapper.selectList(new LambdaQueryWrapper<PrivateDoctorCard>()
                        .eq(PrivateDoctorCard::getDoctorId, doctorProfileId)
                        .orderByDesc(PrivateDoctorCard::getCreateTime))
                .forEach(card -> patientIds.add(card.getPatientId()));
        consultationSessionMapper.selectList(new LambdaQueryWrapper<ConsultationSession>()
                        .eq(ConsultationSession::getDoctorId, doctorProfileId)
                        .orderByDesc(ConsultationSession::getCreateTime))
                .forEach(session -> patientIds.add(session.getPatientId()));
        return patientIds.stream()
                .map(patientId -> buildPatientCareVO(doctorProfileId, patientId))
                .toList();
    }

    public boolean hasDoctorPatientRelation(Long doctorProfileId, Long patientProfileId) {
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

    private PatientCareVO buildPatientCareVO(Long doctorProfileId, Long patientProfileId) {
        PatientCareVO vo = new PatientCareVO();
        vo.setPatientId(patientProfileId);
        PatientProfile profile = patientProfileMapper.selectById(patientProfileId);
        if (profile != null) {
            vo.setUserId(profile.getUserId());
            SysUser user = userMapper.selectById(profile.getUserId());
            if (user != null) {
                vo.setPatientName(user.getRealName());
                vo.setPhone(user.getPhone());
            }
        }

        HealthTrackRecord latest = latestTrack(patientProfileId);
        if (latest != null) {
            vo.setLatestSymptom(latest.getSymptom());
            vo.setLatestRecordDate(latest.getRecordDate());
        }
        vo.setAbnormalRecordCount(countAbnormalRecords(doctorProfileId, patientProfileId));
        vo.setActiveMedicationPlanCount(countMedicationPlans(patientProfileId));
        vo.setPendingFollowUpCount(countPendingFollowUps(doctorProfileId, patientProfileId));
        return vo;
    }

    private AiSuggestionLog createAiAnalysis(HealthTrackRecord record) {
        String fallback = Integer.valueOf(1).equals(record.getAbnormalFlag())
                ? "健康记录存在异常指标，请持续观察并联系医生复核。本内容仅为 AI 辅助参考。"
                : "健康记录未触发明显异常阈值，请继续按医嘱用药并保持记录。本内容仅为 AI 辅助参考。";
        String prompt = "请基于患者健康跟踪记录生成简短分析，症状：" + nullToDash(record.getSymptom())
                + "；体温：" + nullToDash(record.getTemperature())
                + "；血压：" + nullToDash(record.getSystolicPressure()) + "/" + nullToDash(record.getDiastolicPressure())
                + "；心率：" + nullToDash(record.getHeartRate())
                + "；血糖：" + nullToDash(record.getBloodGlucose())
                + "；用药反馈：" + nullToDash(record.getMedicationFeedback());
        return aiAssistantService.createSuggestion(
                UserContext.getUserId(),
                "PATIENT",
                "HEALTH_TRACK_ANALYSIS",
                record.getId(),
                prompt,
                fallback
        );
    }

    private HealthTrackRecordVO toVO(HealthTrackRecord record) {
        HealthTrackRecordVO vo = new HealthTrackRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setPatientName(resolvePatientName(record.getPatientId()));
        vo.setDoctorName(resolveDoctorName(record.getDoctorId()));
        if (record.getAiAnalysisId() != null) {
            AiSuggestionLog aiLog = aiSuggestionLogMapper.selectById(record.getAiAnalysisId());
            if (aiLog != null) {
                vo.setAiAnalysisText(aiLog.getAiOutput());
            }
        }
        return vo;
    }

    private Long resolveLatestDoctorId(Long patientProfileId) {
        LambdaQueryWrapper<PrivateDoctorCard> cardWrapper = new LambdaQueryWrapper<>();
        cardWrapper.eq(PrivateDoctorCard::getPatientId, patientProfileId)
                .eq(PrivateDoctorCard::getStatus, "ACTIVE")
                .orderByDesc(PrivateDoctorCard::getCreateTime)
                .last("LIMIT 1");
        PrivateDoctorCard card = privateDoctorCardMapper.selectOne(cardWrapper);
        if (card != null) {
            return card.getDoctorId();
        }
        LambdaQueryWrapper<ConsultationSession> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(ConsultationSession::getPatientId, patientProfileId)
                .orderByDesc(ConsultationSession::getCreateTime)
                .last("LIMIT 1");
        ConsultationSession session = consultationSessionMapper.selectOne(sessionWrapper);
        return session == null ? null : session.getDoctorId();
    }

    private HealthTrackRecord latestTrack(Long patientProfileId) {
        LambdaQueryWrapper<HealthTrackRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthTrackRecord::getPatientId, patientProfileId)
                .orderByDesc(HealthTrackRecord::getRecordDate)
                .orderByDesc(HealthTrackRecord::getCreateTime)
                .last("LIMIT 1");
        return healthTrackRecordMapper.selectOne(wrapper);
    }

    private Long countAbnormalRecords(Long doctorProfileId, Long patientProfileId) {
        LambdaQueryWrapper<HealthTrackRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthTrackRecord::getPatientId, patientProfileId)
                .eq(HealthTrackRecord::getDoctorId, doctorProfileId)
                .eq(HealthTrackRecord::getAbnormalFlag, 1);
        return healthTrackRecordMapper.selectCount(wrapper);
    }

    private Long countMedicationPlans(Long patientProfileId) {
        LambdaQueryWrapper<MedicationPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationPlan::getPatientId, patientProfileId)
                .eq(MedicationPlan::getStatus, "ACTIVE");
        return medicationPlanMapper.selectCount(wrapper);
    }

    private Long countPendingFollowUps(Long doctorProfileId, Long patientProfileId) {
        LambdaQueryWrapper<FollowUpPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUpPlan::getDoctorId, doctorProfileId)
                .eq(FollowUpPlan::getPatientId, patientProfileId)
                .eq(FollowUpPlan::getStatus, "PENDING");
        return followUpPlanMapper.selectCount(wrapper);
    }

    private boolean isAbnormal(HealthTrackSaveDTO dto) {
        return greaterOrEqual(dto.getTemperature(), "37.3")
                || greaterOrEqual(dto.getSystolicPressure(), 140)
                || greaterOrEqual(dto.getDiastolicPressure(), 90)
                || greaterOrEqual(dto.getHeartRate(), 100)
                || lessOrEqual(dto.getHeartRate(), 50)
                || greaterOrEqual(dto.getBloodGlucose(), "7.80");
    }

    private boolean greaterOrEqual(BigDecimal value, String threshold) {
        return value != null && value.compareTo(new BigDecimal(threshold)) >= 0;
    }

    private boolean greaterOrEqual(Integer value, int threshold) {
        return value != null && value >= threshold;
    }

    private boolean lessOrEqual(Integer value, int threshold) {
        return value != null && value <= threshold;
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
        if (doctorProfileId == null) {
            return null;
        }
        DoctorDetailVO doctor = doctorMapper.selectDoctorDetailById(doctorProfileId);
        return doctor == null ? null : doctor.realName();
    }

    private String nullToDash(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }
}
