package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 指导记录VO
 *
 * @author gpms
 */
@Data
@Schema(description = "指导记录VO")
public class GuidanceRecordVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "学生选题ID")
    private Long studentTopicId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "题目名称")
    private String topicTitle;

    @Schema(description = "第几周")
    private Integer weekNumber;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "导师评语")
    private String advisorComment;

    @Schema(description = "批阅时间")
    private LocalDateTime reviewedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
