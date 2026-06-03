package com.gjx.gpms.mq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 异步通知消息。
 */
@Data
public class NoticeMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private Long recipientId;
    private String title;
    private String content;
    private String type;
}
