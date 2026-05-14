package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 学生志愿提交DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "学生志愿提交DTO")
public class SelectionSubmitDTO {

    @NotNull(message = "批次ID不能为空")
    @Schema(description = "批次ID")
    private Long batchId;

    @NotNull(message = "志愿列表不能为空")
    @Schema(description = "题目ID列表，按优先级排序")
    private List<Long> topicIds;
}
