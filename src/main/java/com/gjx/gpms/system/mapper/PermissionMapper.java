package com.gjx.gpms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.system.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限Mapper
 *
 * @author gpms
 */
@Mapper
public interface PermissionMapper
        extends BaseMapper<Permission> {

    /**
     * 根据用户ID查询权限标识
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> selectPermsByUserId(Long userId);

}