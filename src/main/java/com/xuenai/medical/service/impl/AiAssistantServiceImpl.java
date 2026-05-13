package com.xuenai.medical.service.impl;

import com.xuenai.medical.mapper.AiSuggestionLogMapper;
import com.xuenai.medical.model.entity.AiSuggestionLog;
import com.xuenai.medical.model.vo.AiSuggestionLogVO;
import com.xuenai.medical.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final String SYSTEM_PROMPT = """
            You are an online healthcare assistant embedded in a medical demo system.
            Give concise, cautious, patient-friendly Chinese advice.
            Never diagnose independently. Always state that the content is only an auxiliary reference
            and final medical decisions must be confirmed by doctors or pharmacists.
            """;

    private final AiSuggestionLogMapper aiSuggestionLogMapper;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Value("${medical-care.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}")
    private String modelName;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSuggestionLog createSuggestion(Long userId,
                                            String roleCode,
                                            String scenario,
                                            Long targetId,
                                            String promptSummary,
                                            String fallbackOutput) {
        AiSuggestionLog log = new AiSuggestionLog();
        log.setUserId(userId == null ? 1L : userId);
        log.setRoleCode(hasText(roleCode) ? roleCode : "SYSTEM");
        log.setScenario(scenario);
        log.setTargetId(targetId);
        log.setModelName(modelName);
        log.setPromptSummary(shorten(promptSummary, 2000));
        log.setConfirmStatus("PENDING");
        log.setCreateBy(log.getUserId());
        log.setUpdateBy(log.getUserId());

        String output = null;
        try {
            output = callAi(promptSummary);
        } catch (Exception e) {
            log.setErrorMessage(shorten(e.getMessage(), 1000));
        }
        if (!hasText(output)) {
            output = fallbackOutput;
        }
        log.setAiOutput(shorten(output, 8000));
        aiSuggestionLogMapper.insert(log);
        return log;
    }

    @Override
    public AiSuggestionLogVO toVO(AiSuggestionLog log) {
        if (log == null) {
            return null;
        }
        AiSuggestionLogVO vo = new AiSuggestionLogVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
    }

    private String callAi(String promptSummary) {
        if (!aiEnabled) {
            return null;
        }
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            return null;
        }
        return builder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(promptSummary)
                .call()
                .content();
    }

    private String shorten(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
