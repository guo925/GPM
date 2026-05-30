package com.gjx.gpms.mq.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 异步操作日志消息。
 */
@Data
public class OperationLogMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private Long userId;
    private String action;
    private String targetType;
    private String targetId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private String remark;
}
