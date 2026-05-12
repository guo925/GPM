package com.gjx.gpms.controller;

import com.gjx.gpms.service.IUserService;
import com.gjx.gpms.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService iUserService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(String username, String password) {
        try {
            String token =iUserService.login(username, password);
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            data.put("username", username);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }
}
