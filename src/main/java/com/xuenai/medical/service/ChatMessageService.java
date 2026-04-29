package com.xuenai.medical.service;

import com.xuenai.medical.model.entity.ChatMessage;
import com.xuenai.medical.model.vo.ChatMessageVO;

import java.util.List;

public interface ChatMessageService {

    /**
     * 保存聊天消息
     */
    ChatMessage saveMessage(ChatMessage message);

    /**
     * 获取会话的历史消息
     */
    List<ChatMessageVO> getHistoryMessages(Long sessionId, Long userId);

    /**
     * 将实体转换为VO
     */
    ChatMessageVO convertToVO(ChatMessage message);
}
