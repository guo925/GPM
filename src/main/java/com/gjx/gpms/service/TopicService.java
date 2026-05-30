package com.gjx.gpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.dto.TopicCreateDTO;
import com.gjx.gpms.dto.TopicReviewDTO;
import com.gjx.gpms.entity.Topic;
import com.gjx.gpms.vo.TopicVO;

import java.util.List;

/**
 * 课题服务接口
 *
 * @author gpms
 */
public interface TopicService extends IService<Topic> {

    IPage<TopicVO> page(long current, long size, Long batchId, String status);

    TopicVO getDetail(Long id);

    List<TopicVO> getHotTopics();

    void create(TopicCreateDTO dto);

    void update(Long id, TopicCreateDTO dto);

    void deleteById(Long id);

    void review(TopicReviewDTO dto);
}
