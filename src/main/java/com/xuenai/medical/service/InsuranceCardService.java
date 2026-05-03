package com.xuenai.medical.service;

import com.xuenai.medical.model.dto.InsuranceCardBindDTO;
import com.xuenai.medical.model.vo.InsuranceCardVO;

import java.util.List;

public interface InsuranceCardService {

    InsuranceCardVO bind(Long patientProfileId, InsuranceCardBindDTO dto);

    List<InsuranceCardVO> listByPatient(Long patientProfileId);
}
