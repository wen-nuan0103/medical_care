package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.auth.CurrentUser;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.ConsultationSessionMapper;
import com.xuenai.medical.mapper.DoctorMapper;
import com.xuenai.medical.mapper.DrugMapper;
import com.xuenai.medical.mapper.PatientProfileMapper;
import com.xuenai.medical.mapper.PharmacistProfileMapper;
import com.xuenai.medical.mapper.PrescriptionAuditMapper;
import com.xuenai.medical.mapper.PrescriptionItemMapper;
import com.xuenai.medical.mapper.PrescriptionMapper;
import com.xuenai.medical.mapper.UserMapper;
import com.xuenai.medical.model.dto.PrescriptionItemSaveDTO;
import com.xuenai.medical.model.dto.PrescriptionSaveDTO;
import com.xuenai.medical.model.entity.ConsultationSession;
import com.xuenai.medical.model.entity.Drug;
import com.xuenai.medical.model.entity.PatientProfile;
import com.xuenai.medical.model.entity.PharmacistProfile;
import com.xuenai.medical.model.entity.Prescription;
import com.xuenai.medical.model.entity.PrescriptionAudit;
import com.xuenai.medical.model.entity.PrescriptionItem;
import com.xuenai.medical.model.entity.SysUser;
import com.xuenai.medical.model.vo.DoctorDetailVO;
import com.xuenai.medical.model.vo.PrescriptionAuditVO;
import com.xuenai.medical.model.vo.PrescriptionItemVO;
import com.xuenai.medical.model.vo.PrescriptionVO;
import com.xuenai.medical.service.PrescriptionService;
import com.xuenai.medical.service.ProfileResolveService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING_AUDIT = "PENDING_AUDIT";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_NEED_MODIFY = "NEED_MODIFY";

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final PrescriptionAuditMapper prescriptionAuditMapper;
    private final ConsultationSessionMapper consultationSessionMapper;
    private final DrugMapper drugMapper;
    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;
    private final PatientProfileMapper patientProfileMapper;
    private final PharmacistProfileMapper pharmacistProfileMapper;
    private final ProfileResolveService profileResolveService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionVO create(Long doctorProfileId, PrescriptionSaveDTO dto) {
        ConsultationSession session = getSessionForDoctor(dto.getSessionId(), doctorProfileId);
        if ("CANCELED".equals(session.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已取消的问诊不能开具处方");
        }

        Prescription prescription = new Prescription();
        prescription.setPrescriptionNo(generatePrescriptionNo());
        prescription.setSessionId(session.getId());
        prescription.setPatientId(session.getPatientId());
        prescription.setDoctorId(session.getDoctorId());
        prescription.setDiagnosis(dto.getDiagnosis());
        prescription.setStatus(STATUS_DRAFT);
        prescription.setDoctorNote(dto.getDoctorNote());
        prescription.setPatientInstruction(dto.getPatientInstruction());
        prescriptionMapper.insert(prescription);

        saveItems(prescription.getId(), dto.getItems());
        if (Boolean.TRUE.equals(dto.getSubmit())) {
            doSubmit(prescription);
        }
        return buildVO(prescription.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionVO update(Long doctorProfileId, Long prescriptionId, PrescriptionSaveDTO dto) {
        Prescription prescription = getOwnedPrescriptionForDoctor(prescriptionId, doctorProfileId);
        if (!isEditable(prescription.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前处方状态不可修改");
        }
        ConsultationSession session = getSessionForDoctor(dto.getSessionId(), doctorProfileId);
        if (!session.getPatientId().equals(prescription.getPatientId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处方患者与问诊患者不一致");
        }

        prescription.setSessionId(session.getId());
        prescription.setDiagnosis(dto.getDiagnosis());
        prescription.setDoctorNote(dto.getDoctorNote());
        prescription.setPatientInstruction(dto.getPatientInstruction());
        prescription.setStatus(STATUS_DRAFT);
        prescriptionMapper.updateById(prescription);

        LambdaQueryWrapper<PrescriptionItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PrescriptionItem::getPrescriptionId, prescriptionId);
        prescriptionItemMapper.delete(deleteWrapper);
        saveItems(prescriptionId, dto.getItems());

        if (Boolean.TRUE.equals(dto.getSubmit())) {
            doSubmit(prescription);
        }
        return buildVO(prescriptionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionVO submit(Long doctorProfileId, Long prescriptionId) {
        Prescription prescription = getOwnedPrescriptionForDoctor(prescriptionId, doctorProfileId);
        if (!isEditable(prescription.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前处方状态不可提交审核");
        }
        doSubmit(prescription);
        return buildVO(prescriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionVO> listForPatient(Long patientProfileId, String status) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getPatientId, patientProfileId);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Prescription::getStatus, status);
        }
        wrapper.orderByDesc(Prescription::getCreateTime);
        return prescriptionMapper.selectList(wrapper).stream()
                .map(prescription -> buildVO(prescription.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionVO> listForDoctor(Long doctorProfileId, String status) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getDoctorId, doctorProfileId);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Prescription::getStatus, status);
        }
        wrapper.orderByDesc(Prescription::getCreateTime);
        return prescriptionMapper.selectList(wrapper).stream()
                .map(prescription -> buildVO(prescription.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionVO> listPendingAudit() {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getStatus, STATUS_PENDING_AUDIT)
                .orderByAsc(Prescription::getSubmitTime);
        return prescriptionMapper.selectList(wrapper).stream()
                .map(prescription -> buildVO(prescription.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionVO detail(Long prescriptionId, CurrentUser user) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "处方不存在");
        }
        assertReadable(prescription, user);
        return buildVO(prescriptionId);
    }

    private void doSubmit(Prescription prescription) {
        long itemCount = prescriptionItemMapper.selectCount(new LambdaQueryWrapper<PrescriptionItem>()
                .eq(PrescriptionItem::getPrescriptionId, prescription.getId()));
        if (itemCount == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处方明细不能为空");
        }
        prescription.setStatus(STATUS_PENDING_AUDIT);
        prescription.setSubmitTime(LocalDateTime.now());
        prescription.setApproveTime(null);
        prescription.setValidUntil(LocalDateTime.now().plusDays(3));
        prescriptionMapper.updateById(prescription);
    }

    private void saveItems(Long prescriptionId, List<PrescriptionItemSaveDTO> itemDtos) {
        Map<Long, Drug> drugMap = loadDrugMap(itemDtos.stream()
                .map(PrescriptionItemSaveDTO::getDrugId)
                .distinct()
                .toList());

        for (PrescriptionItemSaveDTO itemDto : itemDtos) {
            Drug drug = drugMap.get(itemDto.getDrugId());
            if (drug == null || Integer.valueOf(0).equals(drug.getStatus())) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "药品不存在或已下架");
            }
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescriptionId);
            item.setDrugId(drug.getId());
            item.setDrugName(drug.getDrugName());
            item.setSpecification(drug.getSpecification());
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(drug.getPrice());
            item.setDosage(itemDto.getDosage());
            item.setFrequency(itemDto.getFrequency());
            item.setDurationDays(itemDto.getDurationDays());
            item.setUsageMethod(itemDto.getUsageMethod());
            item.setMedicationTime(itemDto.getMedicationTime());
            item.setRemark(itemDto.getRemark());
            prescriptionItemMapper.insert(item);
        }
    }

    private Map<Long, Drug> loadDrugMap(Collection<Long> drugIds) {
        if (drugIds == null || drugIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处方药品不能为空");
        }
        List<Drug> drugs = drugMapper.selectBatchIds(drugIds);
        Map<Long, Drug> drugMap = drugs.stream().collect(Collectors.toMap(Drug::getId, Function.identity()));
        if (drugMap.size() != drugIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "处方包含不存在的药品");
        }
        return drugMap;
    }

    private ConsultationSession getSessionForDoctor(Long sessionId, Long doctorProfileId) {
        ConsultationSession session = consultationSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "问诊会话不存在");
        }
        if (!doctorProfileId.equals(session.getDoctorId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权为该问诊开具处方");
        }
        return session;
    }

    private Prescription getOwnedPrescriptionForDoctor(Long prescriptionId, Long doctorProfileId) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "处方不存在");
        }
        if (!doctorProfileId.equals(prescription.getDoctorId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该处方");
        }
        return prescription;
    }

    private boolean isEditable(String status) {
        return STATUS_DRAFT.equals(status) || STATUS_REJECTED.equals(status) || STATUS_NEED_MODIFY.equals(status);
    }

    private void assertReadable(Prescription prescription, CurrentUser user) {
        if (user.hasAnyRole(new String[]{"ADMIN", "PHARMACIST"})) {
            return;
        }
        if (user.hasAnyRole(new String[]{"DOCTOR"})) {
            Long doctorProfileId = profileResolveService.getDoctorProfileId(user.id());
            if (doctorProfileId.equals(prescription.getDoctorId())) {
                return;
            }
        }
        if (user.hasAnyRole(new String[]{"PATIENT"})) {
            Long patientProfileId = profileResolveService.getPatientProfileId(user.id());
            if (patientProfileId.equals(prescription.getPatientId())) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看该处方");
    }

    private PrescriptionVO buildVO(Long prescriptionId) {
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "处方不存在");
        }

        PrescriptionVO vo = new PrescriptionVO();
        BeanUtils.copyProperties(prescription, vo);
        vo.setPatientName(resolvePatientName(prescription.getPatientId()));
        vo.setDoctorName(resolveDoctorName(prescription.getDoctorId()));

        List<PrescriptionItemVO> items = buildItems(prescriptionId);
        vo.setItems(items);
        vo.setTotalAmount(items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setLatestAudit(buildLatestAudit(prescriptionId));
        return vo;
    }

    private List<PrescriptionItemVO> buildItems(Long prescriptionId) {
        LambdaQueryWrapper<PrescriptionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrescriptionItem::getPrescriptionId, prescriptionId)
                .orderByAsc(PrescriptionItem::getId);
        List<PrescriptionItem> items = prescriptionItemMapper.selectList(wrapper);
        Map<Long, Drug> drugMap = items.isEmpty()
                ? Map.of()
                : drugMapper.selectBatchIds(items.stream().map(PrescriptionItem::getDrugId).distinct().toList())
                        .stream()
                        .collect(Collectors.toMap(Drug::getId, Function.identity()));

        return items.stream().map(item -> {
            PrescriptionItemVO vo = new PrescriptionItemVO();
            BeanUtils.copyProperties(item, vo);
            Drug drug = drugMap.get(item.getDrugId());
            if (drug != null) {
                vo.setCurrentStock(drug.getStockQuantity());
                vo.setWarningThreshold(drug.getWarningThreshold());
                vo.setPrescriptionRequired(drug.getPrescriptionRequired());
                vo.setInsuranceCovered(drug.getInsuranceCovered());
                vo.setStockStatus(resolveStockStatus(drug, item.getQuantity()));
            }
            return vo;
        }).toList();
    }

    private PrescriptionAuditVO buildLatestAudit(Long prescriptionId) {
        LambdaQueryWrapper<PrescriptionAudit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrescriptionAudit::getPrescriptionId, prescriptionId)
                .orderByDesc(PrescriptionAudit::getAuditTime)
                .last("LIMIT 1");
        PrescriptionAudit audit = prescriptionAuditMapper.selectOne(wrapper);
        if (audit == null) {
            return null;
        }
        PrescriptionAuditVO vo = new PrescriptionAuditVO();
        BeanUtils.copyProperties(audit, vo);
        vo.setPharmacistName(resolvePharmacistName(audit.getPharmacistId()));
        return vo;
    }

    private String resolveStockStatus(Drug drug, Integer quantity) {
        if (drug.getStockQuantity() == null || drug.getStockQuantity() < quantity) {
            return "INSUFFICIENT";
        }
        if (drug.getWarningThreshold() != null && drug.getStockQuantity() <= drug.getWarningThreshold()) {
            return "LOW";
        }
        return "ENOUGH";
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

    private String resolvePharmacistName(Long pharmacistProfileId) {
        PharmacistProfile profile = pharmacistProfileMapper.selectById(pharmacistProfileId);
        if (profile == null) {
            return null;
        }
        SysUser user = userMapper.selectById(profile.getUserId());
        return user == null ? null : user.getRealName();
    }

    private String generatePrescriptionNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "RX" + date + suffix;
    }
}
