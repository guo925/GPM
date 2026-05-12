package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.AuditLog;
import com.gjx.gpms.mapper.AuditLogMapper;
import com.gjx.gpms.service.IAuditLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 审核日志 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements IAuditLogService {

}
