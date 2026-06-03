package com.gjx.gpms.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 角色创建DTO
 *
 * @author gpms
 */
@Data
@Schema(description = "角色创建DTO")
public class RoleCreateDTO {

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;
}
