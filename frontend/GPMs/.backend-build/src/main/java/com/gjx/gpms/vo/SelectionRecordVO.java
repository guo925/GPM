package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选题记录VO
 *
 * @author gpms
 */
@Data
@Schema(description = "选题记录VO")
public class SelectionRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "题目ID")
    private Long topicId;

    @Schema(description = "题目名称")
    private String topicTitle;

    @Schema(description = "指导教师ID")
    private Long advisorId;

    @Schema(description = "指导教师姓名")
    private String advisorName;

    @Schema(description = "志愿序号")
    private Integer priority;

    @Schema(description = "教师操作")
    private String teacherAction;

    @Schema(description = "教师意见")
    private String teacherComment;

    @Schema(description = "是否最终选中")
    private Integer isSelected;

    @Schema(description = "提交时间")
    private LocalDateTime createdAt;
}
