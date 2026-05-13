package com.xuenai.medical.controller;

import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.vo.SystemNotificationVO;
import com.xuenai.medical.service.SystemNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class SystemNotificationController {

    private final SystemNotificationService systemNotificationService;

    @GetMapping
    public BaseResponse<List<SystemNotificationVO>> list(@RequestParam(required = false) Integer readStatus) {
        return ResultUtils.success(systemNotificationService.list(UserContext.getUserId(), readStatus));
    }

    @GetMapping("/unread-count")
    public BaseResponse<Long> unreadCount() {
        return ResultUtils.success(systemNotificationService.unreadCount(UserContext.getUserId()));
    }

    @PostMapping("/{id}/read")
    public BaseResponse<SystemNotificationVO> markRead(@PathVariable Long id) {
        return ResultUtils.success(systemNotificationService.markRead(UserContext.getUserId(), id));
    }
}
