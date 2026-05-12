package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.Topic;
import com.gjx.gpms.mapper.TopicMapper;
import com.gjx.gpms.service.ITopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 题目表 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements ITopicService {

}
