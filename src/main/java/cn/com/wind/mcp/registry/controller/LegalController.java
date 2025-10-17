package cn.com.wind.mcp.registry.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 法律文档控制器
 * 处理服务协议、隐私政策等法律相关页面的访问
 *
 * @author Wind MCP Registry
 * @since 2025-01-17
 */
@Controller
@RequestMapping("/legal")
public class LegalController {

    /**
     * 显示服务协议页面
     *
     * @return 服务协议页面视图
     */
    @GetMapping("/terms")
    public String terms() {
        return "legal/terms";
    }

    /**
     * 显示隐私政策页面
     *
     * @return 隐私政策页面视图
     */
    @GetMapping("/privacy")
    public String privacy() {
        return "legal/privacy";
    }
}
