package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生选题结果VO
 *
 * @author gpms
 */
@Data
@Schema(description = "学生选题结果VO")
public class StudentTopicVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "批次名称")
    private String batchName;

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

    @Schema(description = "状态")
    private String status;

    @Schema(description = "分配时间")
    private LocalDateTime allocationTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
