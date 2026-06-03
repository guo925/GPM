package com.gjx.gpms.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 权限创建DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "权限创建DTO")
public class PermissionCreateDTO {

    @NotBlank(message = "权限名称不能为空")
    @Schema(description = "权限名称")
    private String permissionName;

    @NotBlank(message = "权限编码不能为空")
    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "权限分组")
    private String groupName;
}
