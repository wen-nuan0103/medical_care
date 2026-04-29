package com.xuenai.medical.controller;

import com.xuenai.medical.auth.CurrentUser;
import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.CreateConsultationDTO;
import com.xuenai.medical.model.dto.GiftTimeDTO;
import com.xuenai.medical.model.entity.ConsultationSession;
import com.xuenai.medical.model.vo.ConsultationSessionVO;
import com.xuenai.medical.service.ConsultationSessionService;
import com.xuenai.medical.service.ProfileResolveService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ConsultationController {

    private final ConsultationSessionService consultationSessionService;
    private final ProfileResolveService profileResolveService;

    public ConsultationController(ConsultationSessionService consultationSessionService,
                                  ProfileResolveService profileResolveService) {
        this.consultationSessionService = consultationSessionService;
        this.profileResolveService = profileResolveService;
    }

    @PostMapping("/consultations")
    @RequireRole("PATIENT")
    public BaseResponse<ConsultationSession> createSession(@Valid @RequestBody CreateConsultationDTO dto) {
        Long patientProfileId = profileResolveService.getPatientProfileId(UserContext.getUserId());
        return ResultUtils.success(consultationSessionService.createSession(patientProfileId, dto));
    }

    @GetMapping("/consultations")
    public BaseResponse<List<ConsultationSessionVO>> listSessions() {
        CurrentUser user = UserContext.get();
        boolean isDoctor = user.hasAnyRole(new String[]{"DOCTOR"});
        Long profileId;
        String role;
        if (isDoctor) {
            profileId = profileResolveService.getDoctorProfileId(user.id());
            role = "DOCTOR";
        } else {
            profileId = profileResolveService.getPatientProfileId(user.id());
            role = "PATIENT";
        }
        return ResultUtils.success(consultationSessionService.listSessions(profileId, role));
    }

    @GetMapping("/consultations/{id}")
    public BaseResponse<ConsultationSessionVO> getSessionDetail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        return ResultUtils.success(consultationSessionService.getSessionDetail(id, userId));
    }

    @PutMapping("/consultations/{id}/end")
    public BaseResponse<Boolean> endSession(@PathVariable Long id) {
        // endSession 需要同时支持患者和医生，传入 userId，service 层做转换判断
        Long userId = UserContext.getUserId();
        consultationSessionService.endSession(id, userId);
        return ResultUtils.success(true);
    }

    @PostMapping("/consultations/{id}/gift-time")
    @RequireRole("DOCTOR")
    public BaseResponse<Boolean> giftTime(@PathVariable Long id, @Valid @RequestBody GiftTimeDTO dto) {
        Long doctorProfileId = profileResolveService.getDoctorProfileId(UserContext.getUserId());
        consultationSessionService.giftTime(id, doctorProfileId, dto);
        return ResultUtils.success(true);
    }
}
