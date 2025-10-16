package cn.com.wind.mcp.registry.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 简易 Cookie 工具类
 */
public class CookieUtils {

    /**
     * 从 HttpServletRequest 中获取指定名称的 Cookie 对象
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        if (request == null || name == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }
}