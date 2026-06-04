package com.gjx.gpms.security.service;

import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.entity.College;
import com.gjx.gpms.entity.Major;
import com.gjx.gpms.mapper.CollegeMapper;
import com.gjx.gpms.mapper.MajorMapper;
import com.gjx.gpms.security.model.LoginUser;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.PermissionMapper;
import com.gjx.gpms.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户认证服务
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    private final PermissionMapper permissionMapper;

    private final CollegeMapper collegeMapper;

    private final MajorMapper majorMapper;

    /**
     * 加载用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public UserDetails loadUserByUsername(String username) {

        log.info("开始加载用户：{}", username);

        // 查询用户
        User user = userMapper.selectByUsername(username);

        // 用户不存在
        if (user == null) {

            log.error("用户不存在：{}", username);

            throw new BusinessException("用户名或密码错误");
        }

        // 用户已禁用
        if (user.getStatus() == 0) {

            log.error("用户已被禁用：{}", username);

            throw new BusinessException("账号已被禁用");
        }

        // 登录时直接读取最新权限，避免角色授权调整后继续使用旧缓存。
        List<String> perms = permissionMapper.selectPermsByUserId(user.getId());

        // 封装权限
        List<SimpleGrantedAuthority> authorities =
                perms.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 封装LoginUser
        LoginUser loginUser = new LoginUser();

        loginUser.setUserId(user.getId());

        loginUser.setUsername(user.getUsername());

        loginUser.setRealName(user.getRealName());

        loginUser.setStudentNo(user.getStudentNo());

        loginUser.setGrade(user.getGrade());

        loginUser.setCollegeId(user.getCollegeId());

        loginUser.setMajorId(user.getMajorId());

        if (user.getCollegeId() != null) {
            College college = collegeMapper.selectById(user.getCollegeId());
            loginUser.setCollegeName(college == null ? "" : college.getName());
        }

        if (user.getMajorId() != null) {
            Major major = majorMapper.selectById(user.getMajorId());
            loginUser.setMajorName(major == null ? "" : major.getName());
        }

        loginUser.setPassword(user.getPassword());

        loginUser.setAuthorities(authorities);

        loginUser.setPermissionCodes(perms);

        return loginUser;
    }
}
