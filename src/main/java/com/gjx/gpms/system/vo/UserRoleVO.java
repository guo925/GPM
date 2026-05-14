package com.gjx.gpms.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户角色VO
 *
 * @author gpms
 */
@Data
@Schema(description = "用户角色VO")
public class UserRoleVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色列表")
    private List<RoleSimpleVO> roles;
}
