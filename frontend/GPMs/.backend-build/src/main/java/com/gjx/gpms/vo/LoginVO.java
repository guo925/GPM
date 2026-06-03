package com.gjx.gpms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /**
     * JWT令牌
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色编码列表
     */
    private List<String> roles;

    /**
     * 权限编码列表
     */
    private List<String> permissions;

    /**
     * @deprecated 使用 roles 替代，保留兼容
     */
    @Deprecated
    public String getRole() {
        return (roles != null && !roles.isEmpty()) ? roles.get(0) : null;
    }
}