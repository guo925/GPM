package com.gjx.gpms.system.dto;

import lombok.Data;

/**
 * 用户分页查询DTO
 */
@Data
public class UserPageDTO {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 状态
     */
    private Integer status;
}