package cn.com.wind.mcp.registry.util;

import cn.com.wind.mcp.registry.entity.Provider;
import jakarta.servlet.http.HttpSession;

/**
 * 权限控制工具类
 * 用于检查用户权限和工具所有权
 *
 * @author system
 */
public class PermissionUtil {

    /**
     * 获取当前登录用户
     */
    public static Provider getCurrentProvider(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Provider) session.getAttribute("currentProvider");
    }

    /**
     * 检查用户是否有权限操作工具
     * 用户只能操作自己创建的工具
     */
    public static boolean hasPermission(HttpSession session, Long toolProviderId) {
        Provider currentProvider = getCurrentProvider(session);
        if (currentProvider == null || toolProviderId == null) {
            return false;
        }
        return currentProvider.getId().equals(toolProviderId);
    }

    /**
     * 检查用户是否登录
     */
    public static boolean isLoggedIn(HttpSession session) {
        return getCurrentProvider(session) != null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentProviderId(HttpSession session) {
        Provider provider = getCurrentProvider(session);
        return provider != null ? provider.getId() : null;
    }
}