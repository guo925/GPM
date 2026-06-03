package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 流程提交DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "流程提交DTO")
public class ProcessSubmitDTO {

    @NotNull(message = "学生选题ID不能为空")
    @Schema(description = "学生选题ID")
    private Long studentTopicId;

    @NotBlank(message = "阶段不能为空")
    @Schema(description = "阶段")
    private String stage;

    @Schema(description = "文本内容")
    private String content;

    @Schema(description = "附件路径")
    private String filePath;
}
