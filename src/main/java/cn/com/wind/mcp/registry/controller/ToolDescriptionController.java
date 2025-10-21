package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.dto.common.Result;
import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.McpClientService;
import cn.com.wind.mcp.registry.service.McpTestSuccessRecordService;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ToolDescriptionController {
    private final McpClientService mcpClientService;
    private final McpTestSuccessRecordService mcpTestSuccessRecordService;

    @PostMapping("/tools/{id}/test")
    public Result<Map<String, Object>> testTool(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {

        try {
            log.info("测试工具请求体: {}", requestBody);
            if (true) {
                // 读取 mock.json 文件并返回
                ClassPathResource resource = new ClassPathResource("config/mock.json");
                try (InputStream inputStream = resource.getInputStream()) {
                    String jsonContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                    Map<String, Object> mockData = JSONUtil.toBean(jsonContent, Map.class);
                    log.info("返回 Mock 数据: {}", mockData);
                    return Result.success(mockData);
                } catch (Exception e) {
                    log.error("读取 mock.json 文件失败", e);
                    return Result.fail("读取 Mock 数据失败: " + e.getMessage());
                }
            }

            // 多种方式获取 sessionId
            String sessionId = null;

            // 1. 从请求体获取
            if (requestBody.containsKey("sessionId")) {
                sessionId = (String) requestBody.remove("sessionId");
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

    /**
     * 保存测试成功记录
     * 当用户确认测试成功后,将完整的测试信息保存到数据库
     *
     * @param id          工具ID
     * @param requestBody 请求体,包含testParameters和testResult
     * @param session     HTTP会话
     * @return 保存结果
     */
    @PostMapping("/tools/{id}/test/save")
    public Result<String> saveTestRecord(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {

        try {
            log.info("保存测试成功记录: toolId={}, requestBody={}", id, requestBody);

            // 获取当前登录用户
            Provider currentProvider = (Provider) session.getAttribute("currentProvider");
            if (currentProvider == null) {
                return Result.fail("用户未登录");
            }

            // 获取测试参数和结果
            String testParameters = JSONUtil.toJsonStr(requestBody.get("testParameters"));
            String testResult = JSONUtil.toJsonStr(requestBody.get("testResult"));

            // 保存测试记录
            boolean success = mcpTestSuccessRecordService.saveTestRecord(
                    id,
                    testParameters,
                    testResult,
                    currentProvider.getId(),
                    currentProvider.getUsername()
            );

            if (success) {
                log.info("测试记录保存成功: toolId={}, operatorId={}", id, currentProvider.getId());
                return Result.success("测试记录保存成功");
            } else {
                log.error("测试记录保存失败: toolId={}", id);
                return Result.fail("测试记录保存失败");
            }
        } catch (Exception e) {
            log.error("保存测试记录时出错: toolId=" + id, e);
            return Result.fail("保存测试记录失败: " + e.getMessage());
        }
    }

}