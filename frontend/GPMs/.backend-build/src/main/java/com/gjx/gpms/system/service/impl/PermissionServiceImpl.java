package com.gjx.gpms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.system.dto.PermissionCreateDTO;
import com.gjx.gpms.system.dto.PermissionUpdateDTO;
import com.gjx.gpms.system.entity.Permission;
import com.gjx.gpms.system.entity.RolePermission;
import com.gjx.gpms.system.mapper.PermissionMapper;
import com.gjx.gpms.system.mapper.RolePermissionMapper;
import com.gjx.gpms.system.service.PermissionService;
import com.gjx.gpms.system.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 创建相关逻辑。
     */
    @Override
    @Transactional
    public void create(PermissionCreateDTO dto) {

        log.info("新增权限：{}", dto.getPermissionCode());

        Long count = this.count(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getPermissionCode, dto.getPermissionCode())
        );

        if (count > 0) {
            throw new BusinessException("权限编码已存在");
        }

        Permission permission = new Permission();
        permission.setPermissionName(dto.getPermissionName());
        permission.setPermissionCode(dto.getPermissionCode());
        permission.setGroupName(dto.getGroupName());

        this.save(permission);

        log.info("新增权限成功：{}", dto.getPermissionCode());
    }

    /**
     * 更新相关逻辑。
     */
    @Override
    @Transactional
    public void update(PermissionUpdateDTO dto) {

        log.info("修改权限：{}", dto.getId());

        Permission permission = this.getById(dto.getId());

        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        Long count = this.count(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getPermissionCode, dto.getPermissionCode())
                        .ne(Permission::getId, dto.getId())
        );

        if (count > 0) {
            throw new BusinessException("权限编码已存在");
        }

        permission.setPermissionName(dto.getPermissionName());
        permission.setPermissionCode(dto.getPermissionCode());
        permission.setGroupName(dto.getGroupName());

        this.updateById(permission);

        log.info("修改权限成功：{}", dto.getId());
    }

    /**
     * 删除by id相关逻辑。
     */
    @Override
    @Transactional
    public void deleteById(Long id) {

        log.info("删除权限：{}", id);

        Permission permission = this.getById(id);

        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        // 删除角色权限关联
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getPermissionId, id)
        );

        this.removeById(id);

        log.info("删除权限成功：{}", id);
    }

    /**
     * 查询列表all相关逻辑。
     */
    @Override
    public List<PermissionVO> listAll() {

        List<Permission> permissions = this.list(
                new LambdaQueryWrapper<Permission>()
                        .orderByAsc(Permission::getGroupName)
                        .orderByAsc(Permission::getPermissionCode)
        );

        return permissions.stream().map(perm -> {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(perm, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 处理tree相关逻辑。
     */
    @Override
    public Map<String, List<PermissionVO>> tree() {

        List<PermissionVO> all = listAll();

        return all.stream().collect(Collectors.groupingBy(
                vo -> vo.getGroupName() != null ? vo.getGroupName() : "未分组",
                Collectors.toList()
        ));
    }
}
