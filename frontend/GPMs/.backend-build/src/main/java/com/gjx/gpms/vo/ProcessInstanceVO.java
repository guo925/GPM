package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程实例VO
 *
 * @author gpms
 */
@Data
@Schema(description = "流程实例VO")
public class ProcessInstanceVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "学生选题ID")
    private Long studentTopicId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "题目名称")
    private String topicTitle;

    @Schema(description = "阶段")
    private String stage;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "提交人姓名")
    private String submitterName;

    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    @Schema(description = "文本内容")
    private String content;

    @Schema(description = "附件路径")
    private String filePath;

    @Schema(description = "审核人姓名")
    private String reviewerName;

    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;

    @Schema(description = "审核意见")
    private String reviewComment;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
