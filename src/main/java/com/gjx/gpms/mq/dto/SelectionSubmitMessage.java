package com.gjx.gpms.mq.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生选题异步落库消息。
 */
@Data
public class SelectionSubmitMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;
    private Long batchId;
    private Long studentId;
    private List<Long> topicIds;
    private Long reservedTopicId;
    private LocalDateTime submittedAt;
}
