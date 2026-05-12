package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 角色权限关联表 Mapper 接口
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

}
