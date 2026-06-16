package com.gjx.gpms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.system.dto.RoleCreateDTO;
import com.gjx.gpms.system.dto.RolePageDTO;
import com.gjx.gpms.system.dto.RoleUpdateDTO;
import com.gjx.gpms.system.entity.Permission;
import com.gjx.gpms.system.entity.Role;
import com.gjx.gpms.system.entity.RolePermission;
import com.gjx.gpms.system.mapper.PermissionMapper;
import com.gjx.gpms.system.mapper.RoleMapper;
import com.gjx.gpms.system.mapper.RolePermissionMapper;
import com.gjx.gpms.system.service.RoleService;
import com.gjx.gpms.system.vo.PermissionVO;
import com.gjx.gpms.system.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    /**
     * 创建相关逻辑。
     */
    @Override
    @Transactional
    public void create(RoleCreateDTO dto) {

        log.info("新增角色：{}", dto.getRoleCode());

        // 角色编码唯一校验
        Long count = this.count(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, dto.getRoleCode())
        );

        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }

        // 保存角色
        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setStatus(1);

        this.save(role);

        // 保存角色权限关联
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            List<RolePermission> rpList = dto.getPermissionIds().stream()
                    .map(permId -> {
                        RolePermission rp = new RolePermission();
                        rp.setRoleId(role.getId());
                        rp.setPermissionId(permId);
                        return rp;
                    }).collect(Collectors.toList());

            rolePermissionMapper.insert(rpList);
        }

        log.info("新增角色成功：{}", dto.getRoleCode());
    }

    /**
     * 更新相关逻辑。
     */
    @Override
    @Transactional
    public void update(RoleUpdateDTO dto) {

        log.info("修改角色：{}", dto.getId());

        Role role = this.getById(dto.getId());

        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if (SUPER_ADMIN_ROLE.equals(role.getRoleCode())
                && !SUPER_ADMIN_ROLE.equals(dto.getRoleCode())) {
            throw new BusinessException("超级管理员角色编码禁止修改");
        }

        // 编码唯一校验（排除自身）
        Long count = this.count(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, dto.getRoleCode())
                        .ne(Role::getId, dto.getId())
        );

        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }

        // 更新角色
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        this.updateById(role);

        // 删除旧权限关联
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, dto.getId())
        );

        // 保存新权限关联
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            List<RolePermission> rpList = dto.getPermissionIds().stream()
                    .map(permId -> {
                        RolePermission rp = new RolePermission();
                        rp.setRoleId(role.getId());
                        rp.setPermissionId(permId);
                        return rp;
                    }).collect(Collectors.toList());

            rolePermissionMapper.insert(rpList);
        }

        log.info("修改角色成功：{}", dto.getId());
    }

    /**
     * 删除by id相关逻辑。
     */
    @Override
    @Transactional
    public void deleteById(Long id) {

        log.info("删除角色：{}", id);

        Role role = this.getById(id);

        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if (SUPER_ADMIN_ROLE.equals(role.getRoleCode())) {
            throw new BusinessException("超级管理员角色禁止删除");
        }

        // 删除角色权限关联
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, id)
        );

        // 删除角色
        this.removeById(id);

        log.info("删除角色成功：{}", id);
    }

    /**
     * 分页查询相关逻辑。
     */
    @Override
    public IPage<RoleVO> page(RolePageDTO dto) {

        Page<Role> page = new Page<>(dto.getCurrent(), dto.getSize());

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(dto.getRoleName() != null, Role::getRoleName, dto.getRoleName());
        wrapper.orderByDesc(Role::getCreateTime);

        Page<Role> rolePage = this.page(page, wrapper);

        List<RoleVO> voList = rolePage.getRecords().stream()
                .map(role -> {
                    RoleVO vo = new RoleVO();
                    BeanUtils.copyProperties(role, vo);
                    vo.setPermissions(getPermissionsByRoleId(role.getId()));
                    return vo;
                }).collect(Collectors.toList());

        Page<RoleVO> voPage = new Page<>();
        voPage.setCurrent(rolePage.getCurrent());
        voPage.setSize(rolePage.getSize());
        voPage.setTotal(rolePage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询列表all相关逻辑。
     */
    @Override
    public List<RoleVO> listAll() {

        List<Role> roles = this.list(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getStatus, 1)
                        .orderByDesc(Role::getCreateTime)
        );

        return roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            vo.setPermissions(getPermissionsByRoleId(role.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取RoleById。
     */
    @Override
    public RoleVO getRoleById(Long id) {

        Role role = super.getById(id);

        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        vo.setPermissions(getPermissionsByRoleId(id));

        return vo;
    }

    /**
     * 查询角色权限
     */
    private List<PermissionVO> getPermissionsByRoleId(Long roleId) {

        List<RolePermission> rpList = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
        );

        if (rpList.isEmpty()) {
            return List.of();
        }

        List<Long> permIds = rpList.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        List<Permission> perms = permissionMapper.selectBatchIds(permIds);

        return perms.stream().map(perm -> {
            PermissionVO pvo = new PermissionVO();
            BeanUtils.copyProperties(perm, pvo);
            return pvo;
        }).collect(Collectors.toList());
    }
}
