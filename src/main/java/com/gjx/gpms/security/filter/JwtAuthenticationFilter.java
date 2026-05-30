package com.gjx.gpms.security.filter;

import com.gjx.gpms.security.model.LoginUser;
import com.gjx.gpms.security.util.JwtUtil;
import com.gjx.gpms.cache.CacheKeys;
import com.gjx.gpms.cache.RedisCacheService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器（优先Redis，不可用时降级JWT本地校验）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisCacheService redisCacheService;

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
            LoginUser loginUser = null;

            // 优先从Redis获取
            try {
                loginUser = redisCacheService.get(CacheKeys.loginToken(token), LoginUser.class);
            } catch (Exception e) {
                log.debug("Redis读取失败，降级JWT：{}", e.getMessage());
            }

            // Redis不可用时从JWT构建
            if (loginUser == null) {
                loginUser = jwtUtil.buildLoginUserFromToken(token);
            } else if (loginUser.getRoleCodes() == null || loginUser.getRoleCodes().isEmpty()) {
                loginUser.setRoleCodes(jwtUtil.buildLoginUserFromToken(token).getRoleCodes());
            }

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
