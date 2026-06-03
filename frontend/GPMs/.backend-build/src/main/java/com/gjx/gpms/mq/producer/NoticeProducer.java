package com.gjx.gpms.mq.producer;

import com.gjx.gpms.mq.dto.NoticeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 通知业务生产者，封装常见通知事件。
 */
@Component
@RequiredArgsConstructor
public class NoticeProducer {

    private final MqProducer mqProducer;

    /**
     * 发送相关逻辑。
     */
    public void send(Long recipientId, String title, String content, String type) {
        NoticeMessage message = new NoticeMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setRecipientId(recipientId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        mqProducer.sendNotice(message);
    }
}
