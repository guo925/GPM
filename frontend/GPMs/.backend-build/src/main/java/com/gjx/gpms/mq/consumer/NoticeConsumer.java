package com.gjx.gpms.mq.consumer;

import com.gjx.gpms.cache.RedisCacheService;
import com.gjx.gpms.config.RabbitMQConfig;
import com.gjx.gpms.entity.Notification;
import com.gjx.gpms.mapper.NotificationMapper;
import com.gjx.gpms.mq.dto.NoticeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 通知消息消费者，负责异步写入通知表。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeConsumer {

    private final NotificationMapper notificationMapper;
    private final RedisCacheService redisCacheService;

    /**
     * 消费相关逻辑。
     */
    @RabbitListener(queues = RabbitMQConfig.NOTICE_QUEUE)
    public void consume(NoticeMessage message) {
        String idempotentKey = "mq:notice:done:" + message.getMessageId();
        if (!Boolean.TRUE.equals(redisCacheService.setIfAbsent(idempotentKey, "1", Duration.ofDays(1)))) {
            log.info("通知消息已处理，跳过重复消费，messageId={}", message.getMessageId());
            return;
        }
        try {
            Notification notification = new Notification();
            notification.setRecipientId(message.getRecipientId());
            notification.setTitle(message.getTitle());
            notification.setContent(message.getContent());
            notification.setType(message.getType());
            notification.setIsRead((byte) 0);
            notification.setCreatedAt(LocalDateTime.now());
            notificationMapper.insert(notification);
            log.info("通知消息消费成功，messageId={}", message.getMessageId());
        } catch (Exception e) {
            redisCacheService.delete(idempotentKey);
            log.error("通知消息消费失败，messageId={}", message.getMessageId(), e);
            throw e;
        }
    }
}
