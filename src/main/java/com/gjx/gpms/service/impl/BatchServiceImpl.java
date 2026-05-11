package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.mapper.BatchMapper;
import com.gjx.gpms.service.IBatchService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 毕设批次及各阶段时间开关 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Service
public class BatchServiceImpl extends ServiceImpl<BatchMapper, Batch> implements IBatchService {

}
