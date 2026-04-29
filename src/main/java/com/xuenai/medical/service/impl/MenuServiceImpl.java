package com.xuenai.medical.service.impl;

import com.xuenai.medical.model.vo.MenuItemVO;
import com.xuenai.medical.service.MenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单 Service 实现
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Override
    public List<MenuItemVO> buildMenus(List<String> roles) {
        List<MenuItemVO> menus = new ArrayList<>();
        if (roles.contains("PATIENT")) {
            menus.add(new MenuItemVO("患者首页", "/patient/home", "Home", List.of()));
            menus.add(new MenuItemVO("找医生", "/patient/doctors", "UserSearch", List.of()));
            menus.add(new MenuItemVO("药品商城", "/patient/drugs", "Pill", List.of()));
            menus.add(new MenuItemVO("我的处方", "/patient/prescriptions", "FileText", List.of()));
        }
        if (roles.contains("DOCTOR")) {
            menus.add(new MenuItemVO("医生工作台", "/doctor/workbench", "BriefcaseMedical", List.of()));
            menus.add(new MenuItemVO("问诊列表", "/doctor/sessions", "MessagesSquare", List.of()));
            menus.add(new MenuItemVO("我的患者", "/doctor/patients", "Users", List.of()));
        }
        if (roles.contains("PHARMACIST")) {
            menus.add(new MenuItemVO("药师工作台", "/pharmacist/workbench", "ClipboardCheck", List.of()));
            menus.add(new MenuItemVO("处方审核", "/pharmacist/audits", "FileCheck", List.of()));
            menus.add(new MenuItemVO("药品管理", "/pharmacist/drugs", "Pill", List.of()));
        }
        if (roles.contains("ADMIN")) {
            menus.add(new MenuItemVO("管理看板", "/admin/dashboard", "LayoutDashboard", List.of()));
            menus.add(new MenuItemVO("用户管理", "/admin/users", "Users", List.of()));
            menus.add(new MenuItemVO("医生管理", "/admin/doctors", "Stethoscope", List.of()));
            menus.add(new MenuItemVO("药品管理", "/admin/drugs", "Pill", List.of()));
        }
        return menus;
    }
}
