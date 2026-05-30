package com.gjx.gpms.mq.producer;

import com.gjx.gpms.config.RabbitMQConfig;
import com.gjx.gpms.mq.dto.NoticeMessage;
import com.gjx.gpms.mq.dto.OperationLogMessage;
import com.gjx.gpms.mq.dto.SelectionSubmitMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 统一生产者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendSelection(SelectionSubmitMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SELECTION_EXCHANGE, RabbitMQConfig.SELECTION_ROUTING_KEY, message);
        log.info("已发送选题异步消息，messageId={}", message.getMessageId());
    }

    public void sendNotice(NoticeMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTICE_EXCHANGE, RabbitMQConfig.NOTICE_ROUTING_KEY, message);
        log.info("已发送通知异步消息，messageId={}", message.getMessageId());
    }

    public void sendOperationLog(OperationLogMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.LOG_EXCHANGE, RabbitMQConfig.LOG_ROUTING_KEY, message);
        log.info("已发送操作日志异步消息，messageId={}", message.getMessageId());
    }
}
