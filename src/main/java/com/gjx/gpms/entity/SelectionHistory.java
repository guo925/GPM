package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 选题记录历史表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("selection_history")
public class SelectionHistory extends SelectionRecord {

    private LocalDateTime archiveTime;

    private Long archiveBatchId;

    private Long archiveOperator;
}
