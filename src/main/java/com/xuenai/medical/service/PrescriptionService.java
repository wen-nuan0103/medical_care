package com.xuenai.medical.service;

import com.xuenai.medical.auth.CurrentUser;
import com.xuenai.medical.model.dto.PrescriptionSaveDTO;
import com.xuenai.medical.model.vo.PrescriptionVO;

import java.util.List;

public interface PrescriptionService {

    PrescriptionVO create(Long doctorProfileId, PrescriptionSaveDTO dto);

    PrescriptionVO update(Long doctorProfileId, Long prescriptionId, PrescriptionSaveDTO dto);

    PrescriptionVO submit(Long doctorProfileId, Long prescriptionId);

    List<PrescriptionVO> listForPatient(Long patientProfileId, String status);

    List<PrescriptionVO> listForDoctor(Long doctorProfileId, String status);

    List<PrescriptionVO> listPendingAudit();

    PrescriptionVO detail(Long prescriptionId, CurrentUser user);
}
