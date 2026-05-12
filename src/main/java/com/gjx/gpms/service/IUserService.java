package com.gjx.gpms.service;

import com.gjx.gpms.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.mapper.UserMapper;
import com.gjx.gpms.util.JwtUtil;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */

public interface IUserService extends IService<User> {
    String login(String username, String password);
}
