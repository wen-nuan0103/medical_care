package com.xuenai.medical.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuenai.medical.mapper.ConsultationSessionMapper;
import com.xuenai.medical.model.entity.ConsultationSession;
import com.xuenai.medical.model.entity.PrivateDoctorCard;
import com.xuenai.medical.service.PrivateDoctorCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：计算并扣除进行中的问诊时间
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsultationTimerTask {

    private final ConsultationSessionMapper consultationSessionMapper;
    private final PrivateDoctorCardService privateDoctorCardService;

    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    @Transactional(rollbackFor = Exception.class)
    public void deductTime() {
        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationSession::getStatus, "IN_PROGRESS");
        List<ConsultationSession> activeSessions = consultationSessionMapper.selectList(wrapper);

        for (ConsultationSession session : activeSessions) {
            try {
                // 1. 累加会话使用的时长
                session.setUsedMinutes(session.getUsedMinutes() + 1);

                // 2. 扣除患者该服务卡的剩余时长
                PrivateDoctorCard card = privateDoctorCardService.getActiveCard(session.getPatientId(), session.getDoctorId());
                if (card != null && card.getRemainingMinutes() > 0) {
                    card.setRemainingMinutes(card.getRemainingMinutes() - 1);
                    privateDoctorCardService.updateCard(card);
                }

                // 3. 检查是否到达限制时长，若到达则自动结束问诊
                if (session.getUsedMinutes() >= session.getAllowedMinutes()) {
                    session.setStatus("ENDED");
                    session.setEndTime(LocalDateTime.now());
                    log.info("Session {} ended automatically due to time out", session.getId());
                }

                consultationSessionMapper.updateById(session);
            } catch (Exception e) {
                log.error("Error processing timer for session {}", session.getId(), e);
            }
        }
    }
}
