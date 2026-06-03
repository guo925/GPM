package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.cache.CacheKeys;
import com.gjx.gpms.cache.RedisCacheService;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.TopicCreateDTO;
import com.gjx.gpms.dto.TopicReviewDTO;
import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.entity.Topic;
import com.gjx.gpms.mapper.BatchMapper;
import com.gjx.gpms.mapper.TopicMapper;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.security.model.LoginUser;
import com.gjx.gpms.service.TopicService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.TopicVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课题服务实现
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    private final BatchMapper batchMapper;
    private final UserMapper userMapper;
    private final RedisCacheService redisCacheService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 分页查询相关逻辑。
     */
    @Override
    public IPage<TopicVO> page(long current, long size, Long batchId, String status) {
        Page<Topic> page = new Page<>(current, size);

        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(batchId != null, Topic::getBatchId, batchId);
        wrapper.eq(status != null, Topic::getStatus, status);
        applyStudentScope(wrapper);
        wrapper.orderByDesc(Topic::getCreatedAt);

        Page<Topic> topicPage = this.page(page, wrapper);

        Map<Long, String> batchMap = batchMapper.selectList(null).stream()
                .collect(Collectors.toMap(Batch::getId, Batch::getName));
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));

        Page<TopicVO> voPage = new Page<>();
        voPage.setCurrent(topicPage.getCurrent());
        voPage.setSize(topicPage.getSize());
        voPage.setTotal(topicPage.getTotal());
        voPage.setRecords(topicPage.getRecords().stream()
                .map(t -> toVO(t, batchMap, userMap))
                .collect(Collectors.toList()));

        return voPage;
    }

    private void applyStudentScope(LambdaQueryWrapper<Topic> wrapper) {
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser == null
                || loginUser.getRoleCodes() == null) {
            return;
        }

        if (loginUser.getRoleCodes().contains("STUDENT")) {
            applyStudentFilter(wrapper, loginUser);
        } else if (loginUser.getRoleCodes().contains("TEACHER")) {
            wrapper.eq(Topic::getCreatorId, loginUser.getUserId());
        }
    }

    private void applyStudentFilter(LambdaQueryWrapper<Topic> wrapper, LoginUser loginUser) {
        User student = userMapper.selectById(loginUser.getUserId());
        if (student == null || student.getCollegeId() == null || student.getMajorId() == null) {
            wrapper.apply("1 = 0");
            return;
        }

        List<Long> batchIds = batchMapper.selectList(
                        new LambdaQueryWrapper<Batch>()
                                .eq(Batch::getCollegeId, student.getCollegeId())
                                .eq(Batch::getMajorId, student.getMajorId())
                                .eq(Batch::getStatus, (byte) 1)
                )
                .stream()
                .map(Batch::getId)
                .toList();
        if (batchIds.isEmpty()) {
            wrapper.apply("1 = 0");
            return;
        }

        wrapper.in(Topic::getBatchId, batchIds)
                .eq(Topic::getStatus, "approved");
    }

    /**
     * 获取Detail。
     */
    @Override
    public TopicVO getDetail(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("课题ID不合法");
        }
        return redisCacheService.getOrLoad(
                CacheKeys.topicDetail(id),
                TopicVO.class,
                Duration.ofMinutes(10),
                120,
                () -> {
                    Topic topic = this.getById(id);
                    if (topic == null) {
                        return null;
                    }
                    Map<Long, String> batchMap = batchMapper.selectList(null).stream()
                            .collect(Collectors.toMap(Batch::getId, Batch::getName));
                    Map<Long, String> userMap = userMapper.selectList(null).stream()
                            .collect(Collectors.toMap(User::getId, User::getRealName));
                    return toVO(topic, batchMap, userMap);
                }
        );
    }

    /**
     * 获取HotTopics。
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<TopicVO> getHotTopics() {
        Object cached = redisCacheService.get(CacheKeys.TOPIC_HOT_LIST);
        if (cached instanceof List<?>) {
            return (List<TopicVO>) cached;
        }
        List<Topic> topics = this.list(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getStatus, "approved")
                        .orderByDesc(Topic::getCurrentCount)
                        .orderByDesc(Topic::getCreatedAt)
                        .last("LIMIT 10")
        );
        Map<Long, String> batchMap = batchMapper.selectList(null).stream()
                .collect(Collectors.toMap(Batch::getId, Batch::getName));
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));
        List<TopicVO> result = topics.stream()
                .map(t -> toVO(t, batchMap, userMap))
                .collect(Collectors.toList());
        redisCacheService.setWithRandomTtl(CacheKeys.TOPIC_HOT_LIST, result, Duration.ofMinutes(5), 60);
        return result;
    }

    /**
     * 创建相关逻辑。
     */
    @Override
    public void create(TopicCreateDTO dto) {
        log.info("新增课题：{}", dto.getTitle());

        Batch batch = batchMapper.selectById(dto.getBatchId());
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        Topic topic = new Topic();
        BeanUtils.copyProperties(dto, topic);
        topic.setCreatorId(UserContext.getUserId());
        topic.setCurrentCount(0);

        if ("preset".equals(dto.getSource())) {
            topic.setStatus("approved");
        } else {
            topic.setStatus("pending");
        }

        this.save(topic);
        syncTopicCurrent(topic);
        redisCacheService.delete(CacheKeys.TOPIC_HOT_LIST);
        log.info("新增课题成功：{}", dto.getTitle());
    }

    /**
     * 更新相关逻辑。
     */
    @Override
    public void update(Long id, TopicCreateDTO dto) {
        Topic topic = this.getById(id);
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }
        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        topic.setMaxCapacity(dto.getMaxCapacity());
        this.updateById(topic);
        syncTopicCurrent(topic);
        redisCacheService.delete(CacheKeys.topicDetail(id));
        redisCacheService.delete(CacheKeys.TOPIC_HOT_LIST);
        log.info("修改课题成功：{}", id);
    }

    /**
     * 删除by id相关逻辑。
     */
    @Override
    public void deleteById(Long id) {
        Topic topic = this.getById(id);
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }
        this.removeById(id);
        deleteTopicCurrent(id);
        redisCacheService.delete(CacheKeys.topicDetail(id));
        redisCacheService.delete(CacheKeys.TOPIC_HOT_LIST);
        log.info("删除课题成功：{}", id);
    }

    /**
     * 审核相关逻辑。
     */
    @Override
    public void review(TopicReviewDTO dto) {
        Topic topic = this.getById(dto.getId());
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }
        topic.setStatus(dto.getStatus());
        topic.setReviewComment(dto.getReviewComment());
        this.updateById(topic);
        syncTopicCurrent(topic);
        redisCacheService.delete(CacheKeys.topicDetail(dto.getId()));
        redisCacheService.delete(CacheKeys.TOPIC_HOT_LIST);
        log.info("审核课题[{}]：{}", dto.getId(), dto.getStatus());
    }

    /**
     * 转换vo相关逻辑。
     */
    private TopicVO toVO(Topic topic, Map<Long, String> batchMap, Map<Long, String> userMap) {
        TopicVO vo = new TopicVO();
        BeanUtils.copyProperties(topic, vo);
        vo.setBatchName(batchMap.getOrDefault(topic.getBatchId(), ""));
        vo.setCreatorName(userMap.getOrDefault(topic.getCreatorId(), ""));
        return vo;
    }

    /**
     * 同步topic current相关逻辑。
     */
    private void syncTopicCurrent(Topic topic) {
        try {
            jdbcTemplate.update("""
                    REPLACE INTO topic_current
                    (id,batch_id,title,description,source,creator_id,max_capacity,current_count,status,review_comment,created_at,updated_at)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                    """,
                    topic.getId(),
                    topic.getBatchId(),
                    topic.getTitle(),
                    topic.getDescription(),
                    topic.getSource(),
                    topic.getCreatorId(),
                    topic.getMaxCapacity(),
                    topic.getCurrentCount(),
                    topic.getStatus(),
                    topic.getReviewComment(),
                    topic.getCreatedAt(),
                    topic.getUpdatedAt()
            );
        } catch (Exception e) {
            log.debug("topic_current 未就绪，跳过课题热表同步：{}", e.getMessage());
        }
    }

    /**
     * 删除topic current相关逻辑。
     */
    private void deleteTopicCurrent(Long id) {
        try {
            jdbcTemplate.update("DELETE FROM topic_current WHERE id=?", id);
        } catch (Exception e) {
            log.debug("topic_current 未就绪，跳过课题热表删除：{}", e.getMessage());
        }
    }
}
