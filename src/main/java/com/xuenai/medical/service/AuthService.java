package com.xuenai.medical.service;

import com.xuenai.medical.auth.CurrentUser;
import com.xuenai.medical.model.dto.LoginDTO;
import com.xuenai.medical.model.vo.LoginVO;
import com.xuenai.medical.model.vo.MenuItemVO;

import java.util.List;

/**
 * 认证 Service 接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO request);

    /**
     * 获取当前登录用户
     */
    CurrentUser currentUser();

    /**
     * 获取当前用户菜单
     */
    List<MenuItemVO> currentMenus();
}
