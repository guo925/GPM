package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 周记历史表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("weekly_log_history")
public class WeeklyLogHistory extends GuidanceRecord {

    private LocalDateTime archiveTime;

    private Long archiveBatchId;

    private Long archiveOperator;
}
