package com.gjx.gpms.service;


import com.gjx.gpms.dto.LoginDTO;
import com.gjx.gpms.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);
}