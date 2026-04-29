package com.xuenai.medical.service;

import com.xuenai.medical.model.vo.MenuItemVO;

import java.util.List;

/**
 * 菜单 Service 接口
 */
public interface MenuService {

    /**
     * 根据角色列表构建菜单
     */
    List<MenuItemVO> buildMenus(List<String> roles);
}
