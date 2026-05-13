package com.xuenai.medical.service;

import com.xuenai.medical.model.dto.HealthTrackSaveDTO;
import com.xuenai.medical.model.vo.HealthTrackRecordVO;
import com.xuenai.medical.model.vo.PatientCareVO;

import java.util.List;

public interface HealthTrackService {

    HealthTrackRecordVO create(Long patientProfileId, HealthTrackSaveDTO dto);

    List<HealthTrackRecordVO> listForPatient(Long patientProfileId);

    List<HealthTrackRecordVO> listForDoctor(Long doctorProfileId, Long patientProfileId);

    List<PatientCareVO> listDoctorPatients(Long doctorProfileId);
}
