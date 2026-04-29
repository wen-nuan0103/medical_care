package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.PrescriptionAuditDTO;
import com.xuenai.medical.model.vo.PrescriptionAuditCheckVO;
import com.xuenai.medical.model.vo.PrescriptionVO;
import com.xuenai.medical.service.PrescriptionAuditService;
import com.xuenai.medical.service.PrescriptionService;
import com.xuenai.medical.service.ProfileResolveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacist/audits")
@RequireRole("PHARMACIST")
@RequiredArgsConstructor
public class PharmacistAuditController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionAuditService prescriptionAuditService;
    private final ProfileResolveService profileResolveService;

    @GetMapping("/pending")
    public BaseResponse<List<PrescriptionVO>> pending() {
        return ResultUtils.success(prescriptionService.listPendingAudit());
    }

    @GetMapping("/{prescriptionId}")
    public BaseResponse<PrescriptionVO> detail(@PathVariable Long prescriptionId) {
        return ResultUtils.success(prescriptionService.detail(prescriptionId, UserContext.get()));
    }

    @GetMapping("/{prescriptionId}/check")
    public BaseResponse<PrescriptionAuditCheckVO> check(@PathVariable Long prescriptionId) {
        return ResultUtils.success(prescriptionAuditService.check(prescriptionId));
    }

    @PostMapping("/{prescriptionId}")
    public BaseResponse<PrescriptionVO> audit(@PathVariable Long prescriptionId,
                                              @Valid @RequestBody PrescriptionAuditDTO dto) {
        Long pharmacistProfileId = profileResolveService.getPharmacistProfileId(UserContext.getUserId());
        return ResultUtils.success(prescriptionAuditService.audit(pharmacistProfileId, prescriptionId, dto));
    }
}
