package com.xuenai.medical.controller;

import com.xuenai.medical.auth.RequireRole;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.PageResult;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.StatusUpdateDTO;
import com.xuenai.medical.model.vo.UserVO;
import com.xuenai.medical.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequireRole("ADMIN")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public BaseResponse<PageResult<UserVO>> pageUsers(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword
    ) {
        return ResultUtils.success(userService.pageUsers(current, pageSize, roleCode, status, keyword));
    }

    @PutMapping("/{id}/status")
    public BaseResponse<Boolean> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO request) {
        return ResultUtils.success(userService.updateStatus(id, request.status()));
    }
}
