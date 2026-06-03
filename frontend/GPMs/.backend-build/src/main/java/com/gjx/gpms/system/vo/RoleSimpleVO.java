package com.gjx.gpms.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色简要VO
 *
 * @author gpms
 */
@Data
@Schema(description = "角色简要VO")
public class RoleSimpleVO {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;
}
