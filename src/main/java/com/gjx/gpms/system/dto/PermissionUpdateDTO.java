package com.gjx.gpms.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 权限修改DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "权限修改DTO")
public class PermissionUpdateDTO {

    @NotNull(message = "权限ID不能为空")
    @Schema(description = "权限ID")
    private Long id;

    @NotBlank(message = "权限名称不能为空")
    @Schema(description = "权限名称")
    private String permissionName;

    @NotBlank(message = "权限编码不能为空")
    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "权限分组")
    private String groupName;
}
