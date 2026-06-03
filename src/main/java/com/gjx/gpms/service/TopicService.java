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

    /**
     * 分页查询相关逻辑。
     */
    IPage<TopicVO> page(long current, long size, Long batchId, String grade, String status);

    /**
     * 获取Detail。
     */
    TopicVO getDetail(Long id);

    /**
     * 获取HotTopics。
     */
    List<TopicVO> getHotTopics();

    /**
     * 创建相关逻辑。
     */
    void create(TopicCreateDTO dto);

    /**
     * 更新相关逻辑。
     */
    void update(Long id, TopicCreateDTO dto);

    /**
     * 删除by id相关逻辑。
     */
    void deleteById(Long id);

    /**
     * 审核相关逻辑。
     */
    void review(TopicReviewDTO dto);
}
