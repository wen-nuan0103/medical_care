package com.xuenai.medical.service;

import com.xuenai.medical.model.dto.FollowUpPlanSaveDTO;
import com.xuenai.medical.model.dto.FollowUpStatusDTO;
import com.xuenai.medical.model.vo.FollowUpPlanVO;

import java.util.List;

public interface FollowUpPlanService {

    FollowUpPlanVO create(Long doctorProfileId, FollowUpPlanSaveDTO dto);

    List<FollowUpPlanVO> listForDoctor(Long doctorProfileId, Long patientProfileId, String status);

    List<FollowUpPlanVO> listForPatient(Long patientProfileId, String status);

    FollowUpPlanVO updateStatus(Long doctorProfileId, Long followUpId, FollowUpStatusDTO dto);
}
