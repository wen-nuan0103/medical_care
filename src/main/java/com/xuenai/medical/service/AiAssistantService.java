package com.xuenai.medical.service;

import com.xuenai.medical.model.entity.AiSuggestionLog;
import com.xuenai.medical.model.vo.AiSuggestionLogVO;

public interface AiAssistantService {

    AiSuggestionLog createSuggestion(Long userId,
                                     String roleCode,
                                     String scenario,
                                     Long targetId,
                                     String promptSummary,
                                     String fallbackOutput);

    AiSuggestionLogVO toVO(AiSuggestionLog log);
}
