package cn.com.wind.mcp.registry.config;

import cn.com.wind.mcp.registry.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 用于注册拦截器和其他Web相关配置
 *
 * @author system
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
            // 拦截所有请求
            .addPathPatterns("/**")
            // 排除静态资源和公开接口
            .excludePathPatterns(
                "/provider/login",       // 登录页面GET
                "/provider/register",    // 注册页面GET
                "/css/**",              // CSS静态资源
                "/js/**",               // JS静态资源
                "/webfonts/**",         // 字体资源
                "/images/**",           // 图片资源
                "/favicon.ico",         // 网站图标
                "/error"                // 错误页面
            );
    }
}