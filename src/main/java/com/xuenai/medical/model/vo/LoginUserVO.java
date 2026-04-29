package com.xuenai.medical.model.vo;

import java.util.List;

/**
 * 登录用户内部查询结果（包含密码哈希，仅供认证内部使用，不对外暴露）
 */
public record LoginUserVO(
        Long id,
        String username,
        String passwordHash,
        String realName,
        Integer status,
        List<String> roles
) {
}
