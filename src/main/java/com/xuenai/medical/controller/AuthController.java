package com.xuenai.medical.controller;

import com.xuenai.medical.auth.CurrentUser;
import com.xuenai.medical.auth.PublicApi;
import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import com.xuenai.medical.model.dto.LoginDTO;
import com.xuenai.medical.model.vo.LoginVO;
import com.xuenai.medical.model.vo.MenuItemVO;
import com.xuenai.medical.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PublicApi
    @PostMapping("/login")
    public BaseResponse<LoginVO> login(@Valid @RequestBody LoginDTO request) {
        return ResultUtils.success(authService.login(request));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> logout() {
        return ResultUtils.success(true);
    }

    @GetMapping("/me")
    public BaseResponse<CurrentUser> me() {
        return ResultUtils.success(authService.currentUser());
    }

    @GetMapping("/menus")
    public BaseResponse<List<MenuItemVO>> menus() {
        return ResultUtils.success(authService.currentMenus());
    }
}
