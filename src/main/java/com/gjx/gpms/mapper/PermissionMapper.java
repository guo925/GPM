package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 权限点表 Mapper 接口
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

}
