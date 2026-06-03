package com.gjx.gpms.dto;


import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginDTO {

    /**
     * 用户名 / 学号 / 工号
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}