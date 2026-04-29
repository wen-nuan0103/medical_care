package com.xuenai.medical.service;

import com.xuenai.medical.model.dto.CreateConsultationDTO;
import com.xuenai.medical.model.dto.GiftTimeDTO;
import com.xuenai.medical.model.entity.ConsultationSession;
import com.xuenai.medical.model.vo.ConsultationSessionVO;

import java.util.List;

public interface ConsultationSessionService {

    /**
     * 创建问诊会话
     */
    ConsultationSession createSession(Long patientId, CreateConsultationDTO dto);

    /**
     * 结束问诊
     */
    void endSession(Long sessionId, Long operatorId);

    /**
     * 查询会话列表
     */
    List<ConsultationSessionVO> listSessions(Long userId, String role);

    /**
     * 查询会话详情
     */
    ConsultationSessionVO getSessionDetail(Long sessionId, Long userId);

    /**
     * 医生赠送时长
     */
    void giftTime(Long sessionId, Long doctorId, GiftTimeDTO dto);

    /**
     * 更新已使用时长
     */
    void updateUsedMinutes(Long sessionId, int minutesToAdd);
}
