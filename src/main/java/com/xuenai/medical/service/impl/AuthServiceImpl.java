package com.xuenai.medical.service.impl;

import com.xuenai.medical.auth.CurrentUser;
import com.xuenai.medical.auth.JwtService;
import com.xuenai.medical.auth.PasswordService;
import com.xuenai.medical.auth.UserContext;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.UserMapper;
import com.xuenai.medical.model.dto.LoginDTO;
import com.xuenai.medical.model.vo.LoginUserVO;
import com.xuenai.medical.model.vo.LoginVO;
import com.xuenai.medical.model.vo.MenuItemVO;
import com.xuenai.medical.service.AuthService;
import com.xuenai.medical.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证 Service 实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final MenuService menuService;

    @Override
    public LoginVO login(LoginDTO request) {
        LoginUserVO loginUser = userMapper.selectLoginUserByUsername(request.username());
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        if (loginUser.status() == null || loginUser.status() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
        }
        if (!passwordService.matches(request.password(), loginUser.passwordHash())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        CurrentUser currentUser = new CurrentUser(loginUser.id(), loginUser.username(), loginUser.realName(), loginUser.roles());
        String token = jwtService.createToken(currentUser);

        // 更新最后登录时间
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.xuenai.medical.model.entity.SysUser> wrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        wrapper.eq(com.xuenai.medical.model.entity.SysUser::getId, loginUser.id())
               .set(com.xuenai.medical.model.entity.SysUser::getLastLoginTime, java.time.LocalDateTime.now());
        userMapper.update(null, wrapper);

        return new LoginVO(token, currentUser, menuService.buildMenus(currentUser.roles()));
    }

    @Override
    public CurrentUser currentUser() {
        CurrentUser currentUser = UserContext.get();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public List<MenuItemVO> currentMenus() {
        return menuService.buildMenus(currentUser().roles());
    }
}
