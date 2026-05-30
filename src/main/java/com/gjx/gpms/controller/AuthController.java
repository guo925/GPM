package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.cache.CacheKeys;
import com.gjx.gpms.cache.RedisCacheService;
import com.gjx.gpms.dto.LoginDTO;
import com.gjx.gpms.security.model.LoginUser;
import com.gjx.gpms.security.util.JwtUtil;
import com.gjx.gpms.service.AuthService;
import com.gjx.gpms.system.mapper.UserRoleMapper;
import com.gjx.gpms.vo.LoginVO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final RedisCacheService redisCacheService;
    private final UserRoleMapper userRoleMapper;

    @PostMapping("/login")
    public Result<LoginVO> login(
            @RequestBody @Valid LoginDTO loginDTO
    ) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return Result.error("Token不存在");
        }
        String token = header.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            try {
                redisCacheService.delete(CacheKeys.loginToken(token));
                redisCacheService.delete(CacheKeys.loginTokenByUserId(userId));
            } catch (Exception ignored) {}
        } catch (Exception e) {
            return Result.error("Token无效");
        }
        return Result.success();
    }

    @GetMapping("/me")
    public Result<LoginVO> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            return Result.error("未登录");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(loginUser.getUserId());

        LoginVO vo = new LoginVO();
        vo.setUserId(loginUser.getUserId());
        vo.setUsername(loginUser.getUsername());
        vo.setRoles(roles);
        vo.setPermissions(loginUser.getPermissionCodes());
        return Result.success(vo);
    }
}
