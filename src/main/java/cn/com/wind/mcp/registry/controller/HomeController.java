package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.service.OriginToolExpoService;
import cn.com.wind.mcp.registry.service.OriginToolHttpService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <p>
 * 首页Controller
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Slf4j
@Controller
public class HomeController {

    @Autowired
    private McpToolService mcpToolService;

    @Autowired
    private OriginToolHttpService originToolHttpService;

    @Autowired
    private OriginToolExpoService originToolExpoService;

    /**
     * 首页
     */
    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        log.info("访问首页");

        // 检查用户是否登录
        Provider currentProvider = (Provider)session.getAttribute("currentProvider");
        if (currentProvider == null) {
            // 未登录，重定向到登录页
            log.info("用户未登录，重定向到登录页");
            return "redirect:/provider/login";
        }

        // 已登录：只统计当前用户创建的数据
        Long providerId = currentProvider.getId();
        String username = currentProvider.getUsername();
        log.info("用户 {} (ID: {}) 访问首页", username, providerId);

        long mcpToolCount = mcpToolService.countByProviderId(providerId);
        long httpToolCount = originToolHttpService.countByProviderId(providerId);
        long expoToolCount = originToolExpoService.countByProviderId(providerId);

        model.addAttribute("mcpToolCount", mcpToolCount);
        model.addAttribute("httpToolCount", httpToolCount);
        model.addAttribute("expoToolCount", expoToolCount);
        model.addAttribute("totalToolCount", mcpToolCount + httpToolCount + expoToolCount);

        return "index";
    }

    /**
     * 关于页面
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}