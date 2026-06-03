package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 课题创建DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "课题创建DTO")
public class TopicCreateDTO {

    @NotNull(message = "所属批次不能为空")
    @Schema(description = "批次ID")
    private Long batchId;

    @NotBlank(message = "题目名称不能为空")
    @Schema(description = "题目名称")
    private String title;

    @Schema(description = "题目描述")
    private String description;

    @Schema(description = "来源: preset/student_propose")
    private String source;

    @Schema(description = "可容纳学生数")
    private Integer maxCapacity;

    @Schema(description = "课题附件路径")
    private String filePath;
}
