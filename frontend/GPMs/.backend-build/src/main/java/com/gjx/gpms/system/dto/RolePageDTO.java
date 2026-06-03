package com.gjx.gpms.system.dto;

import lombok.Data;

/**
 * 角色分页查询DTO
 *
 * @author gpms
 */
@Data
public class RolePageDTO {

    private Long current = 1L;
    private Long size = 10L;
    private String roleName;
}
