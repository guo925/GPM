package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 课题审核DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "课题审核DTO")
public class TopicReviewDTO {

    @NotNull(message = "课题ID不能为空")
    @Schema(description = "课题ID")
    private Long id;

    @NotBlank(message = "审核结果不能为空")
    @Schema(description = "approved/rejected")
    private String status;

    @Schema(description = "审核意见")
    private String reviewComment;
}
