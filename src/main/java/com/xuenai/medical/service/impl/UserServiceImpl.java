package com.xuenai.medical.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuenai.medical.common.PageResult;
import com.xuenai.medical.exception.BusinessException;
import com.xuenai.medical.exception.ErrorCode;
import com.xuenai.medical.mapper.UserMapper;
import com.xuenai.medical.model.entity.SysUser;
import com.xuenai.medical.model.vo.UserVO;
import com.xuenai.medical.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户 Service 实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public PageResult<UserVO> pageUsers(int current, int pageSize, String roleCode, Integer status, String keyword) {
        int safePageSize = Math.min(100, Math.max(1, pageSize));
        int safeCurrent = Math.max(1, current);
        long offset = (long) (safeCurrent - 1) * safePageSize;

        long total = userMapper.countUsers(roleCode, status, keyword);
        List<UserVO> records = userMapper.selectUserVOPage(offset, safePageSize, roleCode, status, keyword);
        return PageResult.of(records, total, safeCurrent, safePageSize);
    }

    @Override
    public boolean updateStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态只能是 0 或 1");
        }
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, userId)
               .eq(SysUser::getDeleted, 0)
               .set(SysUser::getStatus, status);
        int updated = userMapper.update(null, wrapper);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return true;
    }
}
