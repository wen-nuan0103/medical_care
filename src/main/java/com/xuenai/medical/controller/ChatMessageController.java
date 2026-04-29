package com.xuenai.medical.controller;

import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.vo.ChatMessageVO;
import com.xuenai.medical.service.ChatMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping("/consultations/{id}/messages")
    public BaseResponse<List<ChatMessageVO>> getHistoryMessages(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        return ResultUtils.success(chatMessageService.getHistoryMessages(id, userId));
    }
}
