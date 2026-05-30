package com.gjx.gpms.mq.producer;

import com.gjx.gpms.mq.dto.OperationLogMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 操作日志业务生产者。
 */
@Component
@RequiredArgsConstructor
public class OperationLogProducer {

    private final MqProducer mqProducer;

    public void send(Long userId, String action, String targetType, String targetId, String remark) {
        OperationLogMessage message = new OperationLogMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setUserId(userId);
        message.setAction(action);
        message.setTargetType(targetType);
        message.setTargetId(targetId);
        message.setRemark(remark);
        mqProducer.sendOperationLog(message);
    }
}
