package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.MedicationReminderActionDTO;
import com.xuenai.medical.model.vo.MedicationPlanVO;
import com.xuenai.medical.model.vo.MedicationReminderVO;
import com.xuenai.medical.service.MedicationReminderService;
import com.xuenai.medical.service.ProfileResolveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequireRole("PATIENT")
@RequiredArgsConstructor
public class MedicationReminderController {

    private final MedicationReminderService medicationReminderService;
    private final ProfileResolveService profileResolveService;

    @GetMapping("/medication-plans")
    public BaseResponse<List<MedicationPlanVO>> listPlans(@RequestParam(required = false) String status) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicationReminderService.listPlans(patientProfileId, status));
    }

    @GetMapping("/medication-reminders")
    public BaseResponse<List<MedicationReminderVO>> listReminders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicationReminderService.listReminders(patientProfileId, status, from, to));
    }

    @PostMapping("/medication-reminders/{id}/taken")
    public BaseResponse<MedicationReminderVO> markTaken(@PathVariable Long id,
                                                        @Valid @RequestBody(required = false) MedicationReminderActionDTO dto) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicationReminderService.markTaken(patientProfileId, id, dto));
    }

    @PostMapping("/medication-reminders/{id}/missed")
    public BaseResponse<MedicationReminderVO> markMissed(@PathVariable Long id,
                                                         @Valid @RequestBody(required = false) MedicationReminderActionDTO dto) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicationReminderService.markMissed(patientProfileId, id, dto));
    }

    @PostMapping("/medication-reminders/{id}/snooze")
    public BaseResponse<MedicationReminderVO> snooze(@PathVariable Long id,
                                                     @Valid @RequestBody(required = false) MedicationReminderActionDTO dto) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(medicationReminderService.snooze(patientProfileId, id, dto));
    }
}
