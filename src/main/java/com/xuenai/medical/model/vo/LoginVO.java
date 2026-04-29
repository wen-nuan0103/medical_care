package com.xuenai.medical.model.vo;

import com.xuenai.medical.auth.CurrentUser;

import java.io.Serializable;
import java.util.List;

/**
 * 登录响应 VO
 */
public record LoginVO(
        String token,
        CurrentUser user,
        List<MenuItemVO> menus
) implements Serializable {
}
