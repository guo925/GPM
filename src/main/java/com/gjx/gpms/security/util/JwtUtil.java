package com.gjx.gpms.security.util;

import com.gjx.gpms.security.model.LoginUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT工具类（认证核心）
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * 密钥
     */
    private static final String SECRET = Base64.getEncoder()
            .encodeToString("gpms-secret-key-for-hs256-algorithm-2026".getBytes(StandardCharsets.UTF_8));

    /**
     * 过期时间：24小时
     */
    private static final long EXPIRATION = 1000 * 60 * 60 * 24;

    /**
     * 生成Token（包含角色和权限）
     */
    public String generateToken(Long userId, String username, List<String> roles, List<String> permissions) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET)))
                .compact();
    }

    /**
     * 从Token中构建LoginUser（Redis不可用时的降级方案）
     */
    public LoginUser buildLoginUserFromToken(String token) {
        Claims claims = parseToken(token);
        LoginUser user = new LoginUser();
        user.setUserId(Long.valueOf(claims.get("userId").toString()));
        user.setUsername(claims.get("username").toString());
        @SuppressWarnings("unchecked")
        List<String> perms = (List<String>) claims.get("permissions");
        user.setPermissionCodes(perms != null ? perms : List.of());
        return user;
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取用户ID
     */
    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).get("userId").toString());
    }

    /**
     * 是否过期
     */
    public boolean isExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }
}