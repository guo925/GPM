package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 教师审核志愿DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "教师审核志愿DTO")
public class TeacherReviewDTO {

    @NotNull(message = "志愿记录ID不能为空")
    @Schema(description = "志愿记录ID")
    private Long id;

    @NotBlank(message = "操作不能为空")
    @Schema(description = "approve/reject")
    private String action;

    @Schema(description = "审核意见")
    private String comment;
}
