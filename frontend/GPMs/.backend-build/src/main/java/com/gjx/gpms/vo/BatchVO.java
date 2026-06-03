package com.gjx.gpms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 批次VO
 *
 * @author gpms
 */
@Data
@Schema(description = "批次VO")
public class BatchVO {

    @Schema(description = "批次ID")
    private Long id;

    @Schema(description = "批次名称")
    private String name;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "学院ID")
    private Long collegeId;

    @Schema(description = "学院名称")
    private String collegeName;

    @Schema(description = "专业ID")
    private Long majorId;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "当前阶段")
    private String currentStage;

    @Schema(description = "时间节点配置JSON")
    private String config;

    @Schema(description = "每导师最多带学生数")
    private Integer maxStudentPerTeacher;

    @Schema(description = "双选模式")
    private String selectionMode;

    @Schema(description = "学生可选志愿数")
    private Integer studentMaxChoices;

    @Schema(description = "是否允许导师拒绝")
    private Integer allowTeacherReject;

    @Schema(description = "被拒后策略")
    private String rejectStrategy;

    @Schema(description = "状态 1进行中 0已结束")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
