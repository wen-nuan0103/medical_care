package com.xuenai.medical.service;

import com.xuenai.medical.model.dto.MedicationReminderActionDTO;
import com.xuenai.medical.model.entity.MedicineOrder;
import com.xuenai.medical.model.vo.MedicationPlanVO;
import com.xuenai.medical.model.vo.MedicationReminderVO;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicationReminderService {

    void generateFromPaidOrder(MedicineOrder order);

    List<MedicationPlanVO> listPlans(Long patientProfileId, String status);

    List<MedicationReminderVO> listReminders(Long patientProfileId,
                                             String status,
                                             LocalDateTime from,
                                             LocalDateTime to);

    MedicationReminderVO markTaken(Long patientProfileId, Long reminderId, MedicationReminderActionDTO dto);

    MedicationReminderVO markMissed(Long patientProfileId, Long reminderId, MedicationReminderActionDTO dto);

    MedicationReminderVO snooze(Long patientProfileId, Long reminderId, MedicationReminderActionDTO dto);

    int refreshReminderStatus();
}
