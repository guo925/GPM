package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 通用流程事项DTO
 */
@Data
@Schema(description = "通用流程事项DTO")
public class WorkflowItemDTO {
    private Long id;

    private Long batchId;
    private String grade;
    private String workflowType;

    private String studentName;

    private String studentNo;

    private String advisorName;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String extra;
    private String remark;
    private String status;
    private BigDecimal score;
    private String comment;
}
