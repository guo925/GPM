package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 审核日志
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("audit_log")

public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联流程实例（如果是流程审核）
     */
    
    private Long processInstanceId;

    /**
     * topic / task_book / opening_report / thesis 等
     */
    
    private String targetType;

    private Long targetId;

    /**
     * 审核人
     */
    
    private Long auditorId;

    /**
     * approve / reject
     */
    
    private String action;

    private String comment;

    private LocalDateTime createdAt;
}
