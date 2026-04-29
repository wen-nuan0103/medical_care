package com.xuenai.medical.model.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单项 VO
 */
public record MenuItemVO(
        String title,
        String path,
        String icon,
        List<MenuItemVO> children
) implements Serializable {
}
