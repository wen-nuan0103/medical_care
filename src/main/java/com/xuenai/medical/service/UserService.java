package com.xuenai.medical.service;

import com.xuenai.medical.common.PageResult;
import com.xuenai.medical.model.vo.UserVO;

/**
 * 用户 Service 接口
 */
public interface UserService {

    /**
     * 分页查询用户列表
     */
    PageResult<UserVO> pageUsers(int current, int pageSize, String roleCode, Integer status, String keyword);

    /**
     * 更新用户状态
     */
    boolean updateStatus(Long userId, Integer status);
}
