package com.gjx.gpms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.cache.CacheKeys;
import com.gjx.gpms.cache.RedisCacheService;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.system.dto.UserCreateDTO;
import com.gjx.gpms.system.dto.UserPageDTO;
import com.gjx.gpms.system.dto.UserResetPasswordDTO;
import com.gjx.gpms.system.dto.UserRoleAssignDTO;
import com.gjx.gpms.system.dto.UserStatusDTO;
import com.gjx.gpms.system.dto.UserUpdateDTO;
import com.gjx.gpms.system.entity.Role;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.entity.UserRole;
import com.gjx.gpms.system.mapper.RoleMapper;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.system.mapper.UserRoleMapper;
import com.gjx.gpms.system.service.UserService;
import com.gjx.gpms.system.vo.RoleSimpleVO;
import com.gjx.gpms.system.vo.UserRoleVO;
import com.gjx.gpms.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl  extends ServiceImpl<UserMapper, User>  implements UserService {

    private final PasswordEncoder passwordEncoder;

    private final RedisCacheService redisCacheService;

    private final UserRoleMapper userRoleMapper;

    private final RoleMapper roleMapper;

    /**
     * 新增用户
     *
     * @param dto 新增用户DTO
     */
    @Override
    public void create(UserCreateDTO dto) {

        log.info("新增用户，用户名：{}", dto.getUsername());

        // 用户名唯一校验
        Long count = this.count(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, dto.getUsername())
        );

        if (count > 0) {

            log.error("用户名已存在：{}", dto.getUsername());

            throw new BusinessException("用户名已存在");
        }

        // DTO -> Entity
        User user = new User();

        BeanUtils.copyProperties(dto, user);

        // BCrypt加密密码
        user.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        // 默认启用
        user.setStatus(1);

        // 保存用户
        this.save(user);

        log.info("新增用户成功，用户名：{}", dto.getUsername());
    }

    /**
     * 用户分页查询
     *
     * @param dto 分页参数
     * @return 分页结果
     */
    @Override
    public IPage<UserVO> page(UserPageDTO dto) {

        // 分页对象
        Page<User> page = new Page<>(
                dto.getCurrent(),
                dto.getSize()
        );

        // 查询条件
        LambdaQueryWrapper<User> wrapper =
                new LambdaQueryWrapper<>();

        // 用户名模糊查询
        wrapper.like(
                dto.getUsername() != null,
                User::getUsername,
                dto.getUsername()
        );

        // 真实姓名模糊查询
        wrapper.like(
                dto.getRealName() != null,
                User::getRealName,
                dto.getRealName()
        );

        // 状态查询
        wrapper.eq(
                dto.getStatus() != null,
                User::getStatus,
                dto.getStatus()
        );

        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        // 查询分页
        Page<User> userPage = this.page(page, wrapper);

        // Entity -> VO
        List<UserVO> voList =
                userPage.getRecords()
                        .stream()
                        .map(user -> {

                            UserVO vo = new UserVO();

                            BeanUtils.copyProperties(user, vo);

                            return vo;
                        })
                        .collect(Collectors.toList());

        // VO分页对象
        Page<UserVO> voPage = new Page<>();

        voPage.setCurrent(userPage.getCurrent());

        voPage.setSize(userPage.getSize());

        voPage.setTotal(userPage.getTotal());

        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Override
    public UserVO getUserById(Long id) {

        log.info("查询用户详情，用户ID：{}", id);

        // 查询用户
        User user = this.getById(id);

        // 用户不存在
        if (user == null) {

            log.error("用户不存在，用户ID：{}", id);

            throw new BusinessException("用户不存在");
        }

        // Entity -> VO
        UserVO userVO = new UserVO();

        BeanUtils.copyProperties(user, userVO);

        return userVO;
    }


    /**
     * 修改用户
     *
     * @param dto 修改DTO
     */
    @Override
    public void update(UserUpdateDTO dto) {

        log.info("修改用户，用户ID：{}", dto.getId());

        // 查询用户
        User user = this.getById(dto.getId());

        // 用户不存在
        if (user == null) {

            log.error("用户不存在，用户ID：{}", dto.getId());

            throw new BusinessException("用户不存在");
        }

        // 手机号唯一校验
        Long phoneCount = this.count(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, dto.getPhone())
                        .ne(User::getId, dto.getId())
        );

        if (phoneCount > 0) {

            throw new BusinessException("手机号已存在");
        }

        // 邮箱唯一校验
        Long emailCount = this.count(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, dto.getEmail())
                        .ne(User::getId, dto.getId())
        );

        if (emailCount > 0) {

            throw new BusinessException("邮箱已存在");
        }

        // DTO -> Entity
        BeanUtils.copyProperties(dto, user);

        // 更新用户
        this.updateById(user);

        log.info("修改用户成功，用户ID：{}", dto.getId());
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    @Override
    public void deleteById(Long id) {

        log.info("删除用户，用户ID：{}", id);

        // 查询用户
        User user = this.getById(id);

        // 用户不存在
        if (user == null) {

            log.error("用户不存在，用户ID：{}", id);

            throw new BusinessException("用户不存在");
        }

        // 超级管理员禁止删除
        if ("admin".equals(user.getUsername())) {

            log.error("超级管理员禁止删除");

            throw new BusinessException("超级管理员禁止删除");
        }

        // 逻辑删除
        this.removeById(id);

        log.info("删除用户成功，用户ID：{}", id);
    }

    /**
     * 修改用户状态
     *
     * @param dto 状态DTO
     */
    @Override
    public void updateStatus(UserStatusDTO dto) {

        log.info("修改用户状态，用户ID：{}，状态：{}",
                dto.getId(),
                dto.getStatus());

        // 查询用户
        User user = this.getById(dto.getId());

        // 用户不存在
        if (user == null) {

            log.error("用户不存在，用户ID：{}", dto.getId());

            throw new BusinessException("用户不存在");
        }

        // 超级管理员禁止禁用
        if ("admin".equals(user.getUsername())
                && dto.getStatus() == 0) {

            log.error("超级管理员禁止禁用");

            throw new BusinessException("超级管理员禁止禁用");
        }

        // 修改状态
        user.setStatus(dto.getStatus());

        // 更新
        this.updateById(user);

        log.info("修改用户状态成功，用户ID：{}", dto.getId());
    }

    /**
     * 重置密码
     *
     * @param dto 重置密码DTO
     */
    @Override
    public void resetPassword(UserResetPasswordDTO dto) {

        log.info("重置密码，用户ID：{}", dto.getId());

        // 查询用户
        User user = this.getById(dto.getId());

        // 用户不存在
        if (user == null) {

            log.error("用户不存在，用户ID：{}", dto.getId());

            throw new BusinessException("用户不存在");
        }

        // BCrypt加密新密码
        String encodedPassword =
                passwordEncoder.encode(dto.getNewPassword());

        // 更新密码
        user.setPassword(encodedPassword);

        this.updateById(user);

        // 清除Redis登录态，强制重新登录
        try {
            Object token = redisCacheService.get(CacheKeys.loginTokenByUserId(dto.getId()));
            if (token != null) {
                redisCacheService.delete(CacheKeys.loginToken(token.toString()));
            }
            redisCacheService.delete(CacheKeys.loginTokenByUserId(dto.getId()));
            redisCacheService.delete(CacheKeys.permission(dto.getId()));
            redisCacheService.delete(CacheKeys.userMenu(dto.getId()));
        } catch (Exception ignored) {}

        log.info("重置密码成功，用户ID：{}", dto.getId());
    }

    /**
     * 获取用户角色
     */
    @Override
    public UserRoleVO getUserRoles(Long userId) {

        // 查询用户角色关联
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
        );

        // 查询角色信息
        List<RoleSimpleVO> roles = userRoles.stream()
                .map(ur -> {
                    Role role = roleMapper.selectById(ur.getRoleId());
                    RoleSimpleVO vo = new RoleSimpleVO();
                    if (role != null) {
                        vo.setId(role.getId());
                        vo.setRoleName(role.getRoleName());
                        vo.setRoleCode(role.getRoleCode());
                    }
                    return vo;
                }).collect(Collectors.toList());

        UserRoleVO vo = new UserRoleVO();
        vo.setUserId(userId);
        vo.setRoles(roles);

        return vo;
    }

    /**
     * 分配用户角色
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void assignRoles(UserRoleAssignDTO dto) {

        log.info("分配用户角色，用户ID：{}，角色：{}", dto.getUserId(), dto.getRoleIds());

        // 校验用户存在
        User user = this.getById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 删除旧角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, dto.getUserId())
        );

        // 保存新角色关联
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            List<UserRole> userRoles = dto.getRoleIds().stream()
                    .map(roleId -> {
                        UserRole ur = new UserRole();
                        ur.setUserId(dto.getUserId());
                        ur.setRoleId(roleId);
                        return ur;
                    }).collect(Collectors.toList());

            userRoleMapper.insert(userRoles);
        }

        // 清除Redis登录态，强制重新登录以刷新权限
        try {
            Object token = redisCacheService.get(CacheKeys.loginTokenByUserId(dto.getUserId()));
            if (token != null) {
                redisCacheService.delete(CacheKeys.loginToken(token.toString()));
            }
            redisCacheService.delete(CacheKeys.loginTokenByUserId(dto.getUserId()));
            redisCacheService.delete(CacheKeys.permission(dto.getUserId()));
            redisCacheService.delete(CacheKeys.userMenu(dto.getUserId()));
        } catch (Exception ignored) {}

        log.info("分配用户角色成功，用户ID：{}", dto.getUserId());
    }


}
