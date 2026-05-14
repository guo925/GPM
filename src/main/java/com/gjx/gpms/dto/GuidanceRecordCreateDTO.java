package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 指导记录创建DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "指导记录创建DTO")
public class GuidanceRecordCreateDTO {

    @NotNull(message = "学生选题ID不能为空")
    @Schema(description = "学生选题ID")
    private Long studentTopicId;

    @NotNull(message = "周次不能为空")
    @Schema(description = "第几周")
    private Integer weekNumber;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "周记内容")
    private String content;
}
