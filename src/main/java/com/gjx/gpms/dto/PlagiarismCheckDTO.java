package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * PlagiarismCheck 数据传输对象。
 */
@Data
@Schema(description = "AI查重请求")
public class PlagiarismCheckDTO {

    @NotNull(message = "流程实例ID不能为空")
    @Schema(description = "流程实例ID")
    private Long processInstanceId;
}
