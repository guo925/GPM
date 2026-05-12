package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.User;
import com.gjx.gpms.mapper.UserMapper;
import com.gjx.gpms.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

}
