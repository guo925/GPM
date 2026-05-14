package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 专业创建DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "专业创建DTO")
public class MajorCreateDTO {

    @NotNull(message = "所属学院不能为空")
    @Schema(description = "所属学院ID")
    private Long collegeId;

    @NotBlank(message = "专业名称不能为空")
    @Schema(description = "专业名称")
    private String name;

    @NotBlank(message = "专业代码不能为空")
    @Schema(description = "专业代码")
    private String code;

    @Schema(description = "排序")
    private Integer sortOrder;
}
