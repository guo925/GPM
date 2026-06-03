package com.gjx.gpms.security.context;

import com.gjx.gpms.security.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户上下文工具类
 */
public class UserContext {

    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof LoginUser loginUser) {
            return loginUser;
        }

        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {

        LoginUser loginUser = getLoginUser();

        if (loginUser == null) {
            return null;
        }

        return loginUser.getUserId();
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {

        LoginUser loginUser = getLoginUser();

        if (loginUser == null) {
            return null;
        }

        return loginUser.getUsername();
    }
}