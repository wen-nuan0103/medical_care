package com.xuenai.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuenai.medical.model.entity.SysUser;
import com.xuenai.medical.model.vo.LoginUserVO;
import com.xuenai.medical.model.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询登录信息（含角色列表）
     */
    LoginUserVO selectLoginUserByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户详情（含角色列表）
     */
    UserVO selectUserVOById(@Param("userId") Long userId);

    /**
     * 分页查询用户列表
     */
    List<UserVO> selectUserVOPage(@Param("offset") long offset,
                                  @Param("limit") int limit,
                                  @Param("roleCode") String roleCode,
                                  @Param("status") Integer status,
                                  @Param("keyword") String keyword);

    /**
     * 统计用户数量
     */
    long countUsers(@Param("roleCode") String roleCode,
                    @Param("status") Integer status,
                    @Param("keyword") String keyword);

    /**
     * 查询用户的角色 code 列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
