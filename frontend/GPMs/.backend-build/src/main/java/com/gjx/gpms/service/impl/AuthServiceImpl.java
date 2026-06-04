package com.gjx.gpms.service.impl;

import com.gjx.gpms.dto.LoginDTO;
import com.gjx.gpms.security.model.LoginUser;
import com.gjx.gpms.security.util.JwtUtil;
import com.gjx.gpms.service.AuthService;
import com.gjx.gpms.system.mapper.UserRoleMapper;
import com.gjx.gpms.vo.LoginVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.gjx.gpms.cache.CacheKeys;
import com.gjx.gpms.cache.RedisCacheService;

import java.time.Duration;
import java.util.List;
/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * Spring Security认证管理器
     */
    private final AuthenticationManager authenticationManager;

    /**
     * JWT工具类
     */
    private final JwtUtil jwtUtil;

    private final RedisCacheService redisCacheService;

    private final UserRoleMapper userRoleMapper;

    /**
     * 登录
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {

        log.info("用户开始登录：{}", loginDTO.getUsername());

        // 构建认证Token
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                );

        Authentication authentication =
                authenticationManager.authenticate(authenticationToken);

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        // 查询用户真实角色
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(loginUser.getUserId());
        List<String> permissions = loginUser.getPermissionCodes();
        loginUser.setRoleCodes(roles);

        // 生成JWT（含权限，兼容无Redis场景）
        String token = jwtUtil.generateToken(
                loginUser.getUserId(),
                loginUser.getUsername(),
                roles,
                permissions
        );

        log.info("用户登录成功：{}，角色：{}", loginUser.getUsername(), roles);

        // 尝试存入Redis（非必须）
        try {
            redisCacheService.set(CacheKeys.loginToken(token), loginUser, Duration.ofHours(24));
            redisCacheService.set(CacheKeys.loginTokenByUserId(loginUser.getUserId()), token, Duration.ofHours(24));
            log.info("用户登录信息已存入Redis");
        } catch (Exception e) {
            log.warn("Redis不可用，跳过缓存：{}", e.getMessage());
        }

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(loginUser.getUserId());
        vo.setUsername(loginUser.getUsername());
        vo.setRealName(loginUser.getRealName());
        vo.setStudentNo(loginUser.getStudentNo());
        vo.setGrade(loginUser.getGrade());
        vo.setCollegeId(loginUser.getCollegeId());
        vo.setCollegeName(loginUser.getCollegeName());
        vo.setMajorId(loginUser.getMajorId());
        vo.setMajorName(loginUser.getMajorName());
        vo.setRoles(roles);
        vo.setPermissions(permissions);
        return vo;
    }
}
