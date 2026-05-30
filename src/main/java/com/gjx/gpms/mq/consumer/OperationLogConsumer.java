package com.gjx.gpms.mq.consumer;

import com.gjx.gpms.cache.RedisCacheService;
import com.gjx.gpms.config.RabbitMQConfig;
import com.gjx.gpms.entity.OperationLog;
import com.gjx.gpms.mapper.OperationLogMapper;
import com.gjx.gpms.mq.dto.OperationLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 操作日志消费者，避免日志写入阻塞主业务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogConsumer {

    private final OperationLogMapper operationLogMapper;
    private final RedisCacheService redisCacheService;

    @RabbitListener(queues = RabbitMQConfig.LOG_QUEUE)
    public void consume(OperationLogMessage message) {
        String idempotentKey = "mq:log:done:" + message.getMessageId();
        if (!Boolean.TRUE.equals(redisCacheService.setIfAbsent(idempotentKey, "1", Duration.ofDays(1)))) {
            log.info("操作日志消息已处理，跳过重复消费，messageId={}", message.getMessageId());
            return;
        }
        try {
            OperationLog log = new OperationLog();
            log.setUserId(message.getUserId());
            log.setAction(message.getAction());
            log.setTargetType(message.getTargetType());
            log.setTargetId(message.getTargetId());
            log.setOldValue(message.getOldValue());
            log.setNewValue(message.getNewValue());
            log.setIpAddress(message.getIpAddress());
            log.setUserAgent(message.getUserAgent());
            log.setRemark(message.getRemark());
            log.setCreatedAt(LocalDateTime.now());
            operationLogMapper.insert(log);
        } catch (Exception e) {
            redisCacheService.delete(idempotentKey);
            log.error("操作日志消息消费失败，messageId={}", message.getMessageId(), e);
            throw e;
        }
    }
}
