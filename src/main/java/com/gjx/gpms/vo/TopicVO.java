package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课题VO
 *
 * @author gpms
 */
@Data
@Schema(description = "课题VO")
public class TopicVO {

    @Schema(description = "课题ID")
    private Long id;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "批次名称")
    private String batchName;

    @Schema(description = "题目名称")
    private String title;

    @Schema(description = "题目描述")
    private String description;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "可容纳学生数")
    private Integer maxCapacity;

    @Schema(description = "已选人数")
    private Integer currentCount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "审核意见")
    private String reviewComment;

    @Schema(description = "课题附件路径")
    private String filePath;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
