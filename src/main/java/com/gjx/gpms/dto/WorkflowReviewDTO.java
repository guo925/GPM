package com.gjx.gpms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 通用流程审核DTO
 */
@Data
public class WorkflowReviewDTO {
    @NotNull(message = "事项ID不能为空")
    private Long id;

    @NotBlank(message = "状态不能为空")
    private String status;

    private BigDecimal score;
    private String comment;
}
