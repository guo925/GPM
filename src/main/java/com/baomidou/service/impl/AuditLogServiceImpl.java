package com.baomidou.service.impl;

import com.baomidou.entity.AuditLog;
import com.baomidou.mapper.AuditLogMapper;
import com.baomidou.service.IAuditLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 关键操作审计日志 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements IAuditLogService {

}
