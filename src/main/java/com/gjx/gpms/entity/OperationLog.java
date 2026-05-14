package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 通用操作日志
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("operation_log")

public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID，NULL表示系统
     */
    
    private Long userId;

    /**
     * 操作类型: CREATE/UPDATE/DELETE/AUDIT等
     */
    
    private String action;

    /**
     * 操作实体: batch,topic,defense等
     */
    
    private String targetType;

    /**
     * 实体ID
     */
    
    private String targetId;

    /**
     * 变更前数据
     */
    
    private String oldValue;

    /**
     * 变更后数据
     */
    
    private String newValue;

    private String ipAddress;

    private String userAgent;

    private String remark;

    private LocalDateTime createdAt;
}
