package com.baomidou.service.impl;

import com.baomidou.entity.Batch;
import com.baomidou.mapper.BatchMapper;
import com.baomidou.service.IBatchService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 毕设批次及各阶段时间开关 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Service
public class BatchServiceImpl extends ServiceImpl<BatchMapper, Batch> implements IBatchService {

}
