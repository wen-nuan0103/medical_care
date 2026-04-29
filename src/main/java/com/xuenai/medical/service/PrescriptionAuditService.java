package com.xuenai.medical.service;

import com.xuenai.medical.model.dto.PrescriptionAuditDTO;
import com.xuenai.medical.model.vo.PrescriptionAuditCheckVO;
import com.xuenai.medical.model.vo.PrescriptionVO;

public interface PrescriptionAuditService {

    PrescriptionAuditCheckVO check(Long prescriptionId);

    PrescriptionVO audit(Long pharmacistProfileId, Long prescriptionId, PrescriptionAuditDTO dto);
}
