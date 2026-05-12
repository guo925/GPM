package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.OperationLog;
import com.gjx.gpms.mapper.OperationLogMapper;
import com.gjx.gpms.service.IOperationLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用操作日志 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements IOperationLogService {

}
