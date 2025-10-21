package cn.com.wind.mcp.registry.interceptor;

import cn.com.wind.mcp.registry.entity.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 登录拦截器
 * 用于控制未登录用户只能访问登录和注册页面
 *
 * @author system
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestURI = request.getRequestURI();

        // 允许访问的公开路径
        String[] allowedPaths = {
                "/api/tools/",     // API工具接口
                "/provider/login",     // 登录页面
                "/provider/register",  // 注册页面
                "/legal/",            // 法律文档页面（服务协议、隐私政策）
                "/css/",              // CSS静态资源
                "/js/",               // JS静态资源
                "/vendor/",           // 第三方库资源
                "/webfonts/",         // 字体资源
                "/images/",           // 图片资源
                "/favicon.ico"        // 网站图标
        };

        // 检查是否为允许访问的路径
        for (String path : allowedPaths) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }

        // 检查用户是否已登录
        HttpSession session = request.getSession(false);
        if (session != null) {
            Provider provider = (Provider) session.getAttribute("currentProvider");
            if (provider != null) {
                log.debug("用户已登录: {}, 访问路径: {}", provider.getUsername(), requestURI);
                return true;
            }
        }

        // 未登录用户重定向到登录页面
        log.info("未登录用户尝试访问: {}, 重定向到登录页面", requestURI);

        // 如果是Ajax请求，返回JSON响应
        if (isAjaxRequest(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"请先登录\",\"redirect\":\"/provider/login\"}");
            return false;
        }

        // 普通请求重定向到登录页面
        response.sendRedirect("/provider/login");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // 为所有视图添加currentPath属性,用于导航栏高亮
        if (modelAndView != null && !modelAndView.hasView()) {
            return;
        }

        if (modelAndView != null && modelAndView.getViewName() != null
                && !modelAndView.getViewName().startsWith("redirect:")) {
            String currentPath = request.getRequestURI();
            modelAndView.addObject("currentPath", currentPath);
            log.debug("添加currentPath到ModelAndView: {}", currentPath);
        }
    }

    /**
     * 判断是否为Ajax请求
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        String contentType = request.getHeader("Content-Type");
        String accept = request.getHeader("Accept");

        return "XMLHttpRequest".equals(xRequestedWith)
                || (contentType != null && contentType.contains("application/json"))
                || (accept != null && accept.contains("application/json"));
    }
}