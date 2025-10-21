package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.McpToolService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * API控制器
 * 用于提供各种数据接口
 *
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private McpToolService mcpToolService;

    /**
     * 获取用户统计信息
     */
    @GetMapping("/user/statistics")
    public Map<String, Object> getUserStatistics(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Provider currentProvider = (Provider) session.getAttribute("currentProvider");
            if (currentProvider == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            // 查询用户的MCP工具数量
            long mcpToolCount = mcpToolService.count(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<McpTool>()
                            .eq("provider_id", currentProvider.getId()));

            // 查询用户的HTTP接口数量 - 需要导入OriginToolHttpService
            long httpToolCount = 0; // 暂时保持为0，后续实现

            result.put("success", true);
            result.put("mcpToolCount", mcpToolCount);
            result.put("httpToolCount", httpToolCount);
            result.put("totalCount", mcpToolCount + httpToolCount);

        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            result.put("success", false);
            result.put("message", "获取统计信息失败");
            result.put("mcpToolCount", 0);
            result.put("httpToolCount", 0);
            result.put("totalCount", 0);
        }

        return result;
    }
}