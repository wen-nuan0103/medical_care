package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.AiSuggestionLogMapper;
import com.xuenai.medical.mapper.MedicationPlanMapper;
import com.xuenai.medical.mapper.MedicationReminderMapper;
import com.xuenai.medical.mapper.PrescriptionItemMapper;
import com.xuenai.medical.model.dto.MedicationReminderActionDTO;
import com.xuenai.medical.model.entity.AiSuggestionLog;
import com.xuenai.medical.model.entity.MedicationPlan;
import com.xuenai.medical.model.entity.MedicationReminder;
import com.xuenai.medical.model.entity.MedicineOrder;
import com.xuenai.medical.model.entity.PrescriptionItem;
import com.xuenai.medical.model.vo.MedicationPlanVO;
import com.xuenai.medical.model.vo.MedicationReminderVO;
import com.xuenai.medical.service.AiAssistantService;
import com.xuenai.medical.service.MedicationReminderService;
import com.xuenai.medical.service.ProfileResolveService;
import com.xuenai.medical.service.SystemNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationReminderServiceImpl implements MedicationReminderService {

    private static final String PLAN_ACTIVE = "ACTIVE";
    private static final String REMINDER_WAITING = "WAITING";
    private static final String REMINDER_TAKEN = "TAKEN";
    private static final String REMINDER_MISSED = "MISSED";
    private static final String REMINDER_SNOOZED = "SNOOZED";

    private final MedicationPlanMapper medicationPlanMapper;
    private final MedicationReminderMapper medicationReminderMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final AiSuggestionLogMapper aiSuggestionLogMapper;
    private final AiAssistantService aiAssistantService;
    private final ProfileResolveService profileResolveService;
    private final SystemNotificationService systemNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateFromPaidOrder(MedicineOrder order) {
        List<PrescriptionItem> items = listPrescriptionItems(order.getPrescriptionId());
        Long patientUserId = profileResolveService.getUserIdByPatientProfileId(order.getPatientId());
        List<Long> newPlanIds = new ArrayList<>();

        for (PrescriptionItem item : items) {
            if (hasPlan(item.getId())) {
                continue;
            }
            MedicationPlan plan = buildPlan(order, item);
            medicationPlanMapper.insert(plan);
            newPlanIds.add(plan.getId());
            generateAiReminderText(plan, patientUserId);
            generateReminderRows(plan);
        }

        if (!newPlanIds.isEmpty()) {
            systemNotificationService.create(
                    patientUserId,
                    UserContext.getUserId(),
                    "Medication reminders generated",
                    "Your prescription purchase has generated medication plans and reminders.",
                    "MEDICATION_PLAN",
                    order.getId()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationPlanVO> listPlans(Long patientProfileId, String status) {
        LambdaQueryWrapper<MedicationPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationPlan::getPatientId, patientProfileId);
        if (hasText(status)) {
            wrapper.eq(MedicationPlan::getStatus, status);
        }
        wrapper.orderByDesc(MedicationPlan::getCreateTime);
        return medicationPlanMapper.selectList(wrapper).stream()
                .map(this::toPlanVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationReminderVO> listReminders(Long patientProfileId,
                                                    String status,
                                                    LocalDateTime from,
                                                    LocalDateTime to) {
        LambdaQueryWrapper<MedicationReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationReminder::getPatientId, patientProfileId);
        if (hasText(status)) {
            wrapper.eq(MedicationReminder::getStatus, status);
        }
        if (from != null) {
            wrapper.ge(MedicationReminder::getRemindTime, from);
        }
        if (to != null) {
            wrapper.le(MedicationReminder::getRemindTime, to);
        }
        wrapper.orderByAsc(MedicationReminder::getRemindTime);
        return medicationReminderMapper.selectList(wrapper).stream()
                .map(this::toReminderVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedicationReminderVO markTaken(Long patientProfileId, Long reminderId, MedicationReminderActionDTO dto) {
        MedicationReminder reminder = getPatientReminder(patientProfileId, reminderId);
        reminder.setStatus(REMINDER_TAKEN);
        reminder.setConfirmTime(LocalDateTime.now());
        reminder.setFeedback(dto == null ? null : dto.getFeedback());
        reminder.setUpdateBy(UserContext.getUserId());
        medicationReminderMapper.updateById(reminder);
        return toReminderVO(reminder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedicationReminderVO markMissed(Long patientProfileId, Long reminderId, MedicationReminderActionDTO dto) {
        MedicationReminder reminder = getPatientReminder(patientProfileId, reminderId);
        reminder.setStatus(REMINDER_MISSED);
        reminder.setConfirmTime(LocalDateTime.now());
        reminder.setFeedback(dto == null ? null : dto.getFeedback());
        reminder.setUpdateBy(UserContext.getUserId());
        medicationReminderMapper.updateById(reminder);
        return toReminderVO(reminder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedicationReminderVO snooze(Long patientProfileId, Long reminderId, MedicationReminderActionDTO dto) {
        MedicationReminder reminder = getPatientReminder(patientProfileId, reminderId);
        int minutes = dto == null || dto.getSnoozeMinutes() == null ? 15 : dto.getSnoozeMinutes();
        LocalDateTime snoozeTime = LocalDateTime.now().plusMinutes(minutes);
        reminder.setStatus(REMINDER_SNOOZED);
        reminder.setSnoozeTime(snoozeTime);
        reminder.setRemindTime(snoozeTime);
        reminder.setFeedback(dto == null ? null : dto.getFeedback());
        reminder.setUpdateBy(UserContext.getUserId());
        medicationReminderMapper.updateById(reminder);
        return toReminderVO(reminder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refreshReminderStatus() {
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<MedicationReminder> snoozeWrapper = new LambdaUpdateWrapper<>();
        snoozeWrapper.eq(MedicationReminder::getStatus, REMINDER_SNOOZED)
                .le(MedicationReminder::getSnoozeTime, now)
                .set(MedicationReminder::getStatus, REMINDER_WAITING);
        int resumed = medicationReminderMapper.update(null, snoozeWrapper);

        LambdaUpdateWrapper<MedicationReminder> missedWrapper = new LambdaUpdateWrapper<>();
        missedWrapper.eq(MedicationReminder::getStatus, REMINDER_WAITING)
                .lt(MedicationReminder::getRemindTime, now.minusHours(2))
                .set(MedicationReminder::getStatus, REMINDER_MISSED);
        int missed = medicationReminderMapper.update(null, missedWrapper);
        return resumed + missed;
    }

    private MedicationPlan buildPlan(MedicineOrder order, PrescriptionItem item) {
        int durationDays = item.getDurationDays() == null || item.getDurationDays() <= 0 ? 1 : item.getDurationDays();
        int timesPerDay = resolveTimesPerDay(item.getFrequency());
        LocalDate startDate = LocalDate.now();
        MedicationPlan plan = new MedicationPlan();
        plan.setPatientId(order.getPatientId());
        plan.setPrescriptionId(order.getPrescriptionId());
        plan.setPrescriptionItemId(item.getId());
        plan.setDrugId(item.getDrugId());
        plan.setDrugName(item.getDrugName());
        plan.setStartDate(startDate);
        plan.setEndDate(startDate.plusDays(durationDays - 1L));
        plan.setTimesPerDay(timesPerDay);
        plan.setReminderTimes(defaultReminderTimes(timesPerDay));
        plan.setDosage(item.getDosage());
        plan.setUsageMethod(item.getUsageMethod());
        plan.setStatus(PLAN_ACTIVE);
        plan.setCreateBy(UserContext.getUserId());
        plan.setUpdateBy(UserContext.getUserId());
        plan.setRemark(item.getMedicationTime());
        return plan;
    }

    private void generateAiReminderText(MedicationPlan plan, Long patientUserId) {
        String fallback = "请按医嘱服用 " + plan.getDrugName() + "，单次剂量 " + nullToDash(plan.getDosage())
                + "，用法 " + nullToDash(plan.getUsageMethod()) + "。本提醒仅供辅助参考。";
        String prompt = "请为患者生成一条简短用药提醒文案，药品：" + plan.getDrugName()
                + "；剂量：" + nullToDash(plan.getDosage())
                + "；用法：" + nullToDash(plan.getUsageMethod())
                + "；每日次数：" + plan.getTimesPerDay()
                + "；提醒时间：" + plan.getReminderTimes();
        AiSuggestionLog log = aiAssistantService.createSuggestion(
                patientUserId,
                "PATIENT",
                "MEDICATION_REMINDER_TEXT",
                plan.getId(),
                prompt,
                fallback
        );
        plan.setAiReminderText(log.getAiOutput());
        medicationPlanMapper.updateById(plan);
    }

    private void generateReminderRows(MedicationPlan plan) {
        List<LocalTime> times = parseReminderTimes(plan.getReminderTimes());
        LocalDate date = plan.getStartDate();
        LocalDate endDate = plan.getEndDate();
        int days = 0;
        while (!date.isAfter(endDate) && days < 60) {
            for (LocalTime time : times) {
                LocalDateTime remindTime = LocalDateTime.of(date, time);
                if (hasReminder(plan.getId(), remindTime)) {
                    continue;
                }
                MedicationReminder reminder = new MedicationReminder();
                reminder.setPlanId(plan.getId());
                reminder.setPatientId(plan.getPatientId());
                reminder.setDrugId(plan.getDrugId());
                reminder.setRemindTime(remindTime);
                reminder.setStatus(REMINDER_WAITING);
                reminder.setCreateBy(UserContext.getUserId());
                reminder.setUpdateBy(UserContext.getUserId());
                medicationReminderMapper.insert(reminder);
            }
            date = date.plusDays(1);
            days++;
        }
    }

    private List<PrescriptionItem> listPrescriptionItems(Long prescriptionId) {
        LambdaQueryWrapper<PrescriptionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrescriptionItem::getPrescriptionId, prescriptionId)
                .orderByAsc(PrescriptionItem::getId);
        return prescriptionItemMapper.selectList(wrapper);
    }

    private boolean hasPlan(Long prescriptionItemId) {
        LambdaQueryWrapper<MedicationPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationPlan::getPrescriptionItemId, prescriptionItemId)
                .last("LIMIT 1");
        return medicationPlanMapper.selectOne(wrapper) != null;
    }

    private boolean hasReminder(Long planId, LocalDateTime remindTime) {
        LambdaQueryWrapper<MedicationReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationReminder::getPlanId, planId)
                .eq(MedicationReminder::getRemindTime, remindTime)
                .last("LIMIT 1");
        return medicationReminderMapper.selectOne(wrapper) != null;
    }

    private MedicationReminder getPatientReminder(Long patientProfileId, Long reminderId) {
        MedicationReminder reminder = medicationReminderMapper.selectById(reminderId);
        if (reminder == null || !patientProfileId.equals(reminder.getPatientId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "medication reminder not found");
        }
        return reminder;
    }

    private MedicationPlanVO toPlanVO(MedicationPlan plan) {
        MedicationPlanVO vo = new MedicationPlanVO();
        BeanUtils.copyProperties(plan, vo);
        LambdaQueryWrapper<MedicationReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicationReminder::getPlanId, plan.getId())
                .orderByAsc(MedicationReminder::getRemindTime);
        vo.setReminders(medicationReminderMapper.selectList(wrapper).stream()
                .sorted(Comparator.comparing(MedicationReminder::getRemindTime))
                .map(this::toReminderVO)
                .toList());
        return vo;
    }

    private MedicationReminderVO toReminderVO(MedicationReminder reminder) {
        MedicationReminderVO vo = new MedicationReminderVO();
        BeanUtils.copyProperties(reminder, vo);
        MedicationPlan plan = medicationPlanMapper.selectById(reminder.getPlanId());
        if (plan != null) {
            vo.setDrugName(plan.getDrugName());
            vo.setDosage(plan.getDosage());
            vo.setUsageMethod(plan.getUsageMethod());
            vo.setAiReminderText(plan.getAiReminderText());
        }
        return vo;
    }

    private int resolveTimesPerDay(String frequency) {
        if (!hasText(frequency)) {
            return 1;
        }
        String text = frequency.toLowerCase();
        if (text.contains("4") || text.contains("四")) {
            return 4;
        }
        if (text.contains("3") || text.contains("三")) {
            return 3;
        }
        if (text.contains("2") || text.contains("两") || text.contains("二")) {
            return 2;
        }
        return 1;
    }

    private String defaultReminderTimes(int timesPerDay) {
        return switch (timesPerDay) {
            case 4 -> "08:00,12:00,18:00,22:00";
            case 3 -> "08:00,13:00,20:00";
            case 2 -> "08:00,20:00";
            default -> "08:00";
        };
    }

    private List<LocalTime> parseReminderTimes(String reminderTimes) {
        if (!hasText(reminderTimes)) {
            return List.of(LocalTime.of(8, 0));
        }
        List<LocalTime> times = new ArrayList<>();
        for (String item : reminderTimes.split(",")) {
            if (hasText(item)) {
                times.add(LocalTime.parse(item.trim()));
            }
        }
        return times.isEmpty() ? List.of(LocalTime.of(8, 0)) : times;
    }

    private String nullToDash(String value) {
        return hasText(value) ? value : "-";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
