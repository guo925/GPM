package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 通用流程事项
 */
@Data
@TableName("workflow_item")
public class WorkflowItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long batchId;
    private String workflowType;
    private String studentName;
    private String studentNo;
    private String advisorName;
    private String title;
    private String extra;
    private String remark;
    private String status;
    private BigDecimal score;
    private String comment;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
