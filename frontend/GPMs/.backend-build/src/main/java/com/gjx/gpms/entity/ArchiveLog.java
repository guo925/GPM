package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 冷热数据归档日志。
 */
@Data
@TableName("archive_log")
public class ArchiveLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long batchId;

    private Long operatorId;

    private Integer topicCount;

    private Integer selectionCount;

    private Integer scoreCount;

    private Integer weeklyLogCount;

    private String status;

    private String message;

    private LocalDateTime createdAt;
}
