package com.gjx.gpms.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权限VO
 *
 * @author gpms
 */
@Data
@Schema(description = "权限VO")
public class PermissionVO {

    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "权限分组")
    private String groupName;
}
