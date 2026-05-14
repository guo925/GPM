package com.gjx.gpms.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.system.dto.UserCreateDTO;
import com.gjx.gpms.system.dto.UserPageDTO;
import com.gjx.gpms.system.dto.UserResetPasswordDTO;
import com.gjx.gpms.system.dto.UserRoleAssignDTO;
import com.gjx.gpms.system.dto.UserStatusDTO;
import com.gjx.gpms.system.dto.UserUpdateDTO;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.vo.UserRoleVO;
import com.gjx.gpms.system.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 新增用户
     */
    void create(UserCreateDTO dto);

    /**
     * 用户分页查询
     */
    IPage<UserVO> page(UserPageDTO dto);

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getUserById(Long id);

    /**
     * 修改用户
     *
     * @param dto 修改DTO
     */
    void update(UserUpdateDTO dto);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteById(Long id);

    /**
     * 修改用户状态
     *
     * @param dto 状态DTO
     */
    void updateStatus(UserStatusDTO dto);

    /**
     * 重置密码
     *
     * @param dto 重置密码DTO
     */
    void resetPassword(UserResetPasswordDTO dto);

    /**
     * 获取用户角色
     */
    UserRoleVO getUserRoles(Long userId);

    /**
     * 分配用户角色
     */
    void assignRoles(UserRoleAssignDTO dto);
}