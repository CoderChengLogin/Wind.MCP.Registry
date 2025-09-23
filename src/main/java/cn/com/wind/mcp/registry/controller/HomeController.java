package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.service.OriginToolHttpService;
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

    /**
     * 首页
     */
    @GetMapping("/")
    public String index(Model model) {
        log.info("访问首页");

        // 统计数据
        long mcpToolCount = mcpToolService.count();
        long httpToolCount = originToolHttpService.count();

        model.addAttribute("mcpToolCount", mcpToolCount);
        model.addAttribute("httpToolCount", httpToolCount);
        model.addAttribute("totalToolCount", mcpToolCount + httpToolCount);

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