package cn.com.wind.mcp.registry.controller;

import java.util.Map;

import cn.com.wind.mcp.registry.dto.common.Result;
import cn.com.wind.mcp.registry.service.McpClientService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ToolDescriptionController {
    private final McpClientService mcpClientService;

    @PostMapping("/tools/{id}/test")
    public Result<Map<String, Object>> testTool(
        @PathVariable Long id,
        @RequestBody Map<String, Object> requestBody,
        HttpServletRequest request) {

        try {
            log.info("测试工具请求体: {}", requestBody);

            // 多种方式获取 sessionId
            String sessionId = null;

            // 1. 从请求体获取
            if (requestBody.containsKey("sessionId")) {
                sessionId = (String)requestBody.remove("sessionId");
            }

            // 2. 从请求头获取
            if (sessionId == null) {
                sessionId = request.getHeader("windsessionid");
            }
            if (sessionId == null) {
                sessionId = request.getHeader("wind.sessionid");
            }
            if (sessionId == null) {
                sessionId = request.getHeader("X-Session-Id");
            }

            log.info("获取到的 sessionId: {}", sessionId);

            if (sessionId == null) {
                return Result.fail("缺少 sessionId");
            }

            // 剩余的是参数
            Map<String, Object> arguments = requestBody;

            log.info("测试工具: id={}, sessionId={}, arguments={}", id, sessionId, arguments);

            // 使用带 sessionId 的方法调用
            Map<String, Object> result = mcpClientService.testToolWithSessionId(id, arguments, sessionId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试工具时出错", e);
            return Result.fail("测试工具失败: " + e.getMessage());
        }
    }

}