package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 成绩单历史表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("score_history")
public class ScoreHistory extends ScoreSheet {

    private LocalDateTime archiveTime;

    private Long archiveBatchId;

    private Long archiveOperator;
}
