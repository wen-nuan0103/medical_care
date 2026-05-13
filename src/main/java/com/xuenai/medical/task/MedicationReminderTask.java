package com.xuenai.medical.task;

import com.xuenai.medical.service.MedicationReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicationReminderTask {

    private final MedicationReminderService medicationReminderService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshReminderStatus() {
        int changed = medicationReminderService.refreshReminderStatus();
        if (changed > 0) {
            log.info("Medication reminder status refreshed, changed={}", changed);
        }
    }
}
