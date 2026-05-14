package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批次修改DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "批次修改DTO")
public class BatchUpdateDTO {

    @NotNull(message = "批次ID不能为空")
    @Schema(description = "批次ID")
    private Long id;

    @NotBlank(message = "批次名称不能为空")
    @Schema(description = "批次名称")
    private String name;

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
}
