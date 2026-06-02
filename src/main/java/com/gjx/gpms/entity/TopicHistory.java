package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课题历史表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("topic_history")
public class TopicHistory extends Topic {

    private LocalDateTime archiveTime;

    private Long archiveBatchId;

    private Long archiveOperator;
}
