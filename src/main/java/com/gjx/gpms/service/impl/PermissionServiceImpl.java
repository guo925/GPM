package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.Permission;
import com.gjx.gpms.mapper.PermissionMapper;
import com.gjx.gpms.service.IPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 权限点表 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

}
