package com.gjx.gpms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 学院创建DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "学院创建DTO")
public class CollegeCreateDTO {

    @NotBlank(message = "学院名称不能为空")
    @Schema(description = "学院名称")
    private String name;

    @NotBlank(message = "学院代码不能为空")
    @Schema(description = "学院代码")
    private String code;

    @Schema(description = "排序")
    private Integer sortOrder;
}
