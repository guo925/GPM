package com.gjx.gpms.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.system.dto.PermissionCreateDTO;
import com.gjx.gpms.system.dto.PermissionUpdateDTO;
import com.gjx.gpms.system.entity.Permission;
import com.gjx.gpms.system.vo.PermissionVO;

import java.util.List;
import java.util.Map;

/**
 * 权限服务接口
 *
 * @author gpms
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 新增权限
     */
    void create(PermissionCreateDTO dto);

    /**
     * 修改权限
     */
    void update(PermissionUpdateDTO dto);

    /**
     * 删除权限
     */
    void deleteById(Long id);

    /**
     * 权限列表
     */
    List<PermissionVO> listAll();

    /**
     * 权限树（按 groupName 分组）
     */
    Map<String, List<PermissionVO>> tree();
}
