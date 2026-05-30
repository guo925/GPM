package com.gjx.gpms.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录用户信息
 *
 * @author gpms
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    /**
     * 密码 —— 不序列化到JSON
     */
    @JsonIgnore
    private String password;

    /**
     * 权限字符串列表（用于Redis序列化）
     */
    @JsonProperty("permissions")
    private List<String> permissionCodes;

    /**
     * 角色编码列表（用于角色兜底授权）
     */
    @JsonProperty("roles")
    private List<String> roleCodes;

    /**
     * 权限对象 —— 不序列化，从 permissionCodes 构建
     */
    @JsonIgnore
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            List<SimpleGrantedAuthority> permissionAuthorities = permissionCodes == null
                    ? List.of()
                    : permissionCodes.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
            List<SimpleGrantedAuthority> roleAuthorities = roleCodes == null
                    ? List.of()
                    : roleCodes.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());
            authorities = java.util.stream.Stream.concat(permissionAuthorities.stream(), roleAuthorities.stream())
                    .collect(Collectors.toList());
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
