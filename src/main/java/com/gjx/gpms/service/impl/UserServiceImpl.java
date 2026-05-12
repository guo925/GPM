package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.entity.User;
import com.gjx.gpms.mapper.UserMapper;
import com.gjx.gpms.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(String username, String password) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        };
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        return jwtUtil.generateToken(user.getId());
    }
}
