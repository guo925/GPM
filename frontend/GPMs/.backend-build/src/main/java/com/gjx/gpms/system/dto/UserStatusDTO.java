package com.gjx.gpms.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "用户状态DTO")
public class UserStatusDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Long id;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态错误")
    @Max(value = 1, message = "状态错误")
    @Schema(description = "状态 0-禁用 1-启用")
    private Integer status;
}