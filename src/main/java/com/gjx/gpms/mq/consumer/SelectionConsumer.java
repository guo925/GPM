package com.gjx.gpms.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.cache.CacheKeys;
import com.gjx.gpms.cache.RedisCacheService;
import com.gjx.gpms.config.RabbitMQConfig;
import com.gjx.gpms.entity.SelectionRecord;
import com.gjx.gpms.entity.Topic;
import com.gjx.gpms.mapper.SelectionRecordMapper;
import com.gjx.gpms.mapper.TopicMapper;
import com.gjx.gpms.mq.dto.SelectionSubmitMessage;
import com.gjx.gpms.mq.producer.OperationLogProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 选题消息消费者，异步写入选题记录并保证重复消息幂等。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SelectionConsumer {

    private final SelectionRecordMapper selectionRecordMapper;
    private final TopicMapper topicMapper;
    private final RedisCacheService redisCacheService;
    private final OperationLogProducer operationLogProducer;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 消费相关逻辑。
     */
    @RabbitListener(queues = RabbitMQConfig.SELECTION_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void consume(SelectionSubmitMessage message) {
        String idempotentKey = "mq:selection:done:" + message.getMessageId();
        if (!Boolean.TRUE.equals(redisCacheService.setIfAbsent(idempotentKey, "1", Duration.ofDays(1)))) {
            log.info("选题消息已处理，跳过重复消费，messageId={}", message.getMessageId());
            return;
        }

        try {
            Long existCount = selectionRecordMapper.selectCount(
                    new LambdaQueryWrapper<SelectionRecord>()
                            .eq(SelectionRecord::getBatchId, message.getBatchId())
                            .eq(SelectionRecord::getStudentId, message.getStudentId())
            );
            if (existCount > 0) {
                log.info("学生已存在选题记录，幂等跳过，batchId={}, studentId={}", message.getBatchId(), message.getStudentId());
                return;
            }

            for (int i = 0; i < message.getTopicIds().size(); i++) {
                Long topicId = message.getTopicIds().get(i);
                Topic topic = topicMapper.selectById(topicId);
                if (topic == null || !"approved".equals(topic.getStatus())) {
                    throw new IllegalStateException("课题不存在或未审核通过：" + topicId);
                }

                SelectionRecord record = new SelectionRecord();
                record.setBatchId(message.getBatchId());
                record.setStudentId(message.getStudentId());
                record.setTopicId(topicId);
                record.setPriority((byte) (i + 1));
                record.setIsSelected((byte) 0);
                record.setCreatedAt(LocalDateTime.now());
                selectionRecordMapper.insert(record);
                syncSelectionCurrent(record);
            }

            operationLogProducer.send(
                    message.getStudentId(),
                    "CREATE",
                    "selection_record",
                    String.valueOf(message.getBatchId()),
                    "学生提交选题志愿"
            );
            log.info("选题异步落库成功，messageId={}", message.getMessageId());
        } catch (Exception e) {
            redisCacheService.delete(idempotentKey);
            rollbackQuota(message);
            log.error("选题异步落库失败，messageId={}", message.getMessageId(), e);
            throw e;
        }
    }

    /**
     * 处理rollbackQuota相关逻辑。
     */
    private void rollbackQuota(SelectionSubmitMessage message) {
        if (message.getReservedTopicId() != null) {
            redisCacheService.increment(CacheKeys.selectionQuota(message.getReservedTopicId()));
            redisCacheService.delete(CacheKeys.selectionStudent(message.getBatchId(), message.getStudentId()));
        }
    }

    /**
     * 同步selection current相关逻辑。
     */
    private void syncSelectionCurrent(SelectionRecord record) {
        try {
            jdbcTemplate.update("""
                    REPLACE INTO selection_current
                    (id,batch_id,student_id,topic_id,priority,teacher_action,teacher_comment,is_selected,created_at,updated_at)
                    VALUES (?,?,?,?,?,?,?,?,?,?)
                    """,
                    record.getId(),
                    record.getBatchId(),
                    record.getStudentId(),
                    record.getTopicId(),
                    record.getPriority(),
                    record.getTeacherAction(),
                    record.getTeacherComment(),
                    record.getIsSelected(),
                    record.getCreatedAt(),
                    record.getUpdatedAt()
            );
        } catch (Exception e) {
            log.debug("selection_current 未就绪，跳过选题热表同步：{}", e.getMessage());
        }
    }
}
