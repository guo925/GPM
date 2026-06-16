package com.gjx.gpms.security.filter;

import com.gjx.gpms.security.model.LoginUser;
import com.gjx.gpms.security.util.JwtUtil;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.PermissionMapper;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.system.mapper.UserRoleMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器（优先Redis，不可用时降级JWT本地校验）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;

    /**
     * 处理doFilterInternal相关逻辑。
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            User user = userMapper.selectById(userId);
            if (user == null || user.getStatus() == null || user.getStatus() == 0) {
                filterChain.doFilter(request, response);
                return;
            }
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(user.getId());
            loginUser.setUsername(user.getUsername());
            List<String> roles = userRoleMapper.selectRoleCodesByUserId(userId);
            List<String> permissions = permissionMapper.selectPermsByUserId(userId);
            loginUser.setRoleCodes(roles);
            loginUser.setPermissionCodes(permissions);
            loginUser.setAuthorities(java.util.stream.Stream.concat(
                            permissions.stream().map(SimpleGrantedAuthority::new),
                            roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    )
                    .collect(Collectors.toList()));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            loginUser,
                            null,
                            loginUser.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.error("JWT认证失败：{}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
