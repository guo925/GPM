package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.ProcessInstance;
import com.gjx.gpms.mapper.ProcessInstanceMapper;
import com.gjx.gpms.service.IProcessInstanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 毕设流程实例表(状态机) 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Service
public class ProcessInstanceServiceImpl extends ServiceImpl<ProcessInstanceMapper, ProcessInstance> implements IProcessInstanceService {

}
