package com.gjx.gpms.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.system.dto.RoleCreateDTO;
import com.gjx.gpms.system.dto.RolePageDTO;
import com.gjx.gpms.system.dto.RoleUpdateDTO;
import com.gjx.gpms.system.entity.Role;
import com.gjx.gpms.system.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author gpms
 */
public interface RoleService extends IService<Role> {

    /**
     * 新增角色
     */
    void create(RoleCreateDTO dto);

    /**
     * 修改角色
     */
    void update(RoleUpdateDTO dto);

    /**
     * 删除角色
     */
    void deleteById(Long id);

    /**
     * 角色分页
     */
    IPage<RoleVO> page(RolePageDTO dto);

    /**
     * 角色列表
     */
    List<RoleVO> listAll();

    /**
     * 角色详情（含权限）
     */
    RoleVO getRoleById(Long id);
}
