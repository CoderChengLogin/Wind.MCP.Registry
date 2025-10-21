package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.config.McpClientConfig;
import cn.com.wind.mcp.registry.dto.mcptool.McpToolDTO;
import cn.com.wind.mcp.registry.service.McpClientService;
import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.util.SessionDataUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpClientServiceImpl implements McpClientService {

    private final RestTemplate mcpRestTemplate;
    private final McpClientConfig.McpProperties mcpProperties;
    private final McpToolService mcpToolService;
    private final ObjectMapper objectMapper;

    private final AtomicLong requestId = new AtomicLong(0);
    private volatile boolean isInitialized = false;

    @Override
    public Map<String, Object> testTool(Long toolId, Map<String, Object> arguments) {
        try {
            // 获取当前请求的 sessionId
            String sessionId = getCurrentSessionId();
            if (sessionId == null) {
                return createErrorResponse("缺少 sessionId，请先登录");
            }

            log.info("使用 sessionId: {}", sessionId);

            // 确保MCP连接已初始化
            if (!isInitialized) {
                initializeConnection(sessionId);
            }

            // 修改查询逻辑：toolId实际上是toolNum
            McpToolDTO tool = null;
            try {
                // 先尝试通过toolNum查询（这是正确的方式）
                List<McpToolDTO> tools = mcpToolService.getMcpToolsByNumValid(toolId, "1");
                if (!tools.isEmpty()) {
                    tool = tools.get(0);
                    log.info("通过toolNum {}找到工具: {}", toolId, tool.getToolName());
                }
            } catch (Exception e) {
                log.warn("通过toolNum {}查询工具失败: {}", toolId, e.getMessage());

                // 如果toolNum查询失败，尝试通过id查询
                try {
                    tool = mcpToolService.getMcpToolById(toolId);
                    log.info("通过id {}找到工具: {}", toolId, tool.getToolName());
                } catch (Exception e2) {
                    log.warn("通过id {}查询工具失败: {}", toolId, e2.getMessage());
                }
            }

            if (tool == null) {
                // 如果还是找不到，列出所有可用工具供调试
                try {
                    List<McpToolDTO> allTools = mcpToolService.getAllMcpTools();
                    log.info("当前数据库中的所有工具: {}",
                            allTools.stream()
                                    .map(t -> String.format("ID:%d, ToolNum:%d, Name:%s",
                                            t.getId(), t.getToolNum(), t.getToolName()))
                                    .collect(java.util.stream.Collectors.toList()));
                } catch (Exception e) {
                    log.error("查询所有工具失败", e);
                }

                return createErrorResponse("工具不存在，toolNum: " + toolId);
            }

            log.info("找到工具: ID={}, ToolNum={}, Name={}",
                    tool.getId(), tool.getToolNum(), tool.getToolName());

            // 构造MCP调用请求
            Map<String, Object> mcpRequest = new HashMap<>();
            mcpRequest.put("jsonrpc", "2.0");
            mcpRequest.put("id", requestId.incrementAndGet());
            mcpRequest.put("method", "tools/call");

            Map<String, Object> params = new HashMap<>();
            params.put("name", tool.getToolName());
            params.put("arguments", arguments != null ? arguments : new HashMap<>());
            mcpRequest.put("params", params);

            // 使用SSE方式发送请求，传入 sessionId
            String mcpUrl = mcpProperties.getServer().getUrl();
            log.info("准备调用MCP服务器: {}, 工具: {}, 参数: {}", mcpUrl, tool.getToolName(), arguments);
            String response = sendSseRequest(mcpUrl, mcpRequest, sessionId);

            if (response != null && !response.trim().isEmpty()) {
                log.info("收到MCP响应: {}", response);
                // 解析SSE响应
                return parseSseResponse(response);
            } else {
                log.error("MCP服务器无响应或响应为空");
                return createErrorResponse("MCP服务器无响应");
            }

        } catch (Exception e) {
            log.error("调用MCP工具时出错", e);
            return createErrorResponse("MCP客户端调用失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        try {
            String sessionId = getCurrentSessionId();
            String mcpUrl = mcpProperties.getServer().getUrl();

            // 使用正确的Accept头检查连接
            HttpURLConnection connection = (HttpURLConnection) new URL(mcpUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json, text/event-stream");
            connection.setRequestProperty("Cache-Control", "no-cache");

            // 添加认证头
            if (sessionId != null) {
                connection.setRequestProperty("wind.sessionid", sessionId);
                connection.setRequestProperty("windsessionid", sessionId);
            }

            String clientName = mcpProperties.getClient().getClientName();
            if (clientName != null && !clientName.trim().isEmpty()) {
                connection.setRequestProperty("x-wind-clientname", clientName);
            } else {
                connection.setRequestProperty("x-wind-clientname", "aimarket-backend");
            }

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            log.info("连接检查响应码: {}", responseCode);
            connection.disconnect();

            return responseCode == 200;
        } catch (Exception e) {
            log.error("检查MCP连接状态时出错", e);
            return false;
        }
    }

    private String getCurrentSessionId() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                // 1. 优先从请求头获取 windsessionid
                String sessionId = request.getHeader("windsessionid");
                if (sessionId != null) {
                    log.info("从请求头获取到 windsessionid: {}", sessionId);
                    return sessionId;
                }

                // 2. 尝试从 Cookie 获取 wind.sessionid
                sessionId = SessionDataUtil.getWindSessionId(request);
                if (sessionId != null) {
                    log.info("从Cookie获取到 wind.sessionid: {}", sessionId);
                    return sessionId;
                }
                return sessionId;
            }
        } catch (Exception e) {
            log.error("获取 sessionId 失败", e);
        }
        return null;
    }

    /**
     * 初始化MCP连接，带 sessionId
     */
    private void initializeConnection(String sessionId) {
        try {
            String mcpUrl = mcpProperties.getServer().getUrl();

            // 发送初始化请求
            Map<String, Object> initRequest = new HashMap<>();
            initRequest.put("jsonrpc", "2.0");
            initRequest.put("id", 0);
            initRequest.put("method", "initialize");

            Map<String, Object> params = new HashMap<>();
            params.put("protocolVersion", "2025-03-26");

            Map<String, Object> capabilities = new HashMap<>();
            Map<String, Object> tools = new HashMap<>();
            tools.put("listChanged", true);
            capabilities.put("tools", tools);
            params.put("capabilities", capabilities);

            Map<String, Object> clientInfo = new HashMap<>();
            clientInfo.put("name", "mcp-tool-caller");
            clientInfo.put("version", "1.0.0");
            params.put("clientInfo", clientInfo);

            initRequest.put("params", params);

            String response = sendSseRequest(mcpUrl, initRequest, sessionId);

            if (response != null && response.contains("result")) {
                // 发送initialized通知
                Map<String, Object> notifyRequest = new HashMap<>();
                notifyRequest.put("jsonrpc", "2.0");
                notifyRequest.put("method", "notifications/initialized");

                sendSseRequest(mcpUrl, notifyRequest, sessionId);

                isInitialized = true;
                log.info("MCP连接初始化成功，使用 sessionId: {}", sessionId);
            }
        } catch (Exception e) {
            log.error("初始化MCP连接失败", e);
            isInitialized = false;
        }
    }

    /**
     * 发送SSE请求，带 sessionId
     */
    private String sendSseRequest(String url, Map<String, Object> requestData, String sessionId) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json, text/event-stream");
            connection.setRequestProperty("Cache-Control", "no-cache");

            // ⭐ 关键：添加 sessionId 认证头
            if (sessionId != null) {
                connection.setRequestProperty("wind.sessionid", sessionId);
                connection.setRequestProperty("windsessionid", sessionId);
                log.info("添加认证头 wind.sessionid: {}", sessionId);
            } else {
                log.warn("sessionId 为空，可能导致认证失败");
            }

            String clientName = mcpProperties.getClient().getClientName();
            if (clientName != null && !clientName.trim().isEmpty()) {
                connection.setRequestProperty("x-wind-clientname", clientName);
                log.info("添加客户端名称头 x-wind-clientname: {}", clientName);
            } else {
                // 如果配置中没有，使用默认值
                connection.setRequestProperty("x-wind-clientname", "aimarket-backend");
                log.info("使用默认客户端名称: aimarket-backend");
            }

            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            // 发送请求体
            String jsonRequest = objectMapper.writeValueAsString(requestData);
            log.info("发送MCP请求到: {}, 请求内容: {}", url, jsonRequest);

            connection.getOutputStream().write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();

            // 读取响应
            int responseCode = connection.getResponseCode();
            log.info("MCP响应状态码: {}", responseCode);

            if (responseCode == 200 || responseCode == 202) {
                StringBuilder response = new StringBuilder();

                // 检查响应的Content-Type来决定如何处理
                String contentType = connection.getContentType();
                log.info("响应Content-Type: {}", contentType);

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

                    String line;
                    if (contentType != null && contentType.contains("text/event-stream")) {
                        // SSE格式响应
                        while ((line = reader.readLine()) != null) {
                            log.debug("SSE响应行: {}", line);

                            // SSE格式：data: {json}
                            if (line.startsWith("data: ")) {
                                String jsonData = line.substring(6);
                                response.append(jsonData);

                                // 如果这是完整的JSON响应，就退出
                                if (jsonData.contains("\"result\"") || jsonData.contains("\"error\"")) {
                                    break;
                                }
                            }
                        }
                    } else {
                        // JSON格式响应
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                }

                String result = response.toString();
                log.info("MCP完整响应: {}", result);
                return result;
            } else {
                // 读取错误响应
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line);
                    }
                    log.error("MCP请求失败，状态码: {}, 错误信息: {}", responseCode, error.toString());
                }
            }

            return null;
        } catch (Exception e) {
            log.error("发送SSE请求失败", e);
            return null;
        }
    }

    /**
     * 解析SSE响应
     */
    private Map<String, Object> parseSseResponse(String sseResponse) {
        try {
            // 解析JSON响应
            Map<String, Object> mcpResponse = objectMapper.readValue(sseResponse, Map.class);
            log.info("解析MCP响应: {}", mcpResponse);

            if (mcpResponse.containsKey("result")) {
                Map<String, Object> mcpResult = (Map<String, Object>) mcpResponse.get("result");

                if (mcpResult.containsKey("content")) {
                    java.util.List<Map<String, Object>> content =
                            (java.util.List<Map<String, Object>>) mcpResult.get("content");

                    if (content != null && !content.isEmpty() && "text".equals(content.get(0).get("type"))) {
                        String textContent = (String) content.get(0).get("text");
                        log.info("提取的文本内容: {}", textContent);

                        if (textContent != null && !textContent.trim().isEmpty()) {
                            try {
                                // 解析内部JSON响应
                                Map<String, Object> parsedContent = objectMapper.readValue(textContent, Map.class);
                                log.info("解析后的内容: {}", parsedContent);

                                // 构建返回结果，包含两种格式的数据
                                Map<String, Object> result = new HashMap<>();

                                // 检查工具是否有错误
                                if (parsedContent.containsKey("mcp_tool_error_code")) {
                                    Object errorCode = parsedContent.get("mcp_tool_error_code");
                                    if (errorCode != null && !errorCode.equals(0)) {
                                        String errorMsg = (String) parsedContent.get("mcp_tool_error_msg");

                                        // 错误情况：返回原始MCP格式
                                        result.put("content", content);
                                        result.put("isError", true);
                                        result.put("error",
                                                "工具执行失败: " + errorMsg + " (错误代码: " + errorCode + ")");
                                        result.put("errorCode", errorCode);
                                        result.put("errorMessage", errorMsg);
                                        return result;
                                    }
                                }

                                // 成功情况：同时提供两种格式
                                // 1. 原始MCP协议数据（用于"响应过程"显示）
                                result.put("content", content);
                                result.put("isError", false);

                                // 2. 解析后的业务数据（用于"测试结果"显示）
                                if (parsedContent.containsKey("mcp_tool_data") && parsedContent.get("mcp_tool_data")
                                        != null) {
                                    Object toolData = parsedContent.get("mcp_tool_data");

                                    if (toolData instanceof String) {
                                        // 如果是字符串，尝试解析为JSON
                                        String toolDataStr = (String) toolData;

                                        // 检查是否是简单的时间字符串（不是JSON）
                                        if (toolDataStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                                            // 直接返回时间字符串，不解析为JSON
                                            Map<String, Object> timeResult = new HashMap<>();
                                            timeResult.put("currentTime", toolDataStr);
                                            result.put("businessData", timeResult);
                                            return result;
                                        }

                                        if (!toolDataStr.trim().isEmpty()) {
                                            try {
                                                Map<String, Object> actualData = objectMapper.readValue(toolDataStr,
                                                        Map.class);
                                                // 将业务数据添加到结果中
                                                result.put("businessData", actualData);
                                                log.info("成功解析 tool_data: {}", actualData);
                                            } catch (Exception e) {
                                                log.warn("解析tool_data字符串失败: {}", toolDataStr, e);
                                                result.put("businessData", Map.of("error", "数据解析失败"));
                                            }
                                        }
                                    } else if (toolData instanceof Map) {
                                        // 如果已经是Map对象，直接使用
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> actualData = (Map<String, Object>) toolData;
                                        result.put("businessData", actualData);
                                        log.info("直接使用Map格式的tool_data: {}", actualData);
                                    }
                                }

                                // 添加原始解析内容供调试
                                result.put("toolResponse", parsedContent);

                                return result;

                            } catch (Exception e) {
                                log.error("解析工具响应数据失败: {}", textContent, e);

                                // 解析失败时返回原始content格式
                                Map<String, Object> result = new HashMap<>();
                                result.put("content", content);
                                result.put("isError", true);
                                result.put("error", "解析工具响应失败: " + e.getMessage());
                                return result;
                            }
                        }
                    }

                    // 返回原始MCP结果格式
                    Map<String, Object> result = new HashMap<>();
                    result.put("content", content);
                    result.put("isError", false);
                    return result;
                }
            }

            if (mcpResponse.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) mcpResponse.get("error");

                // 错误情况返回MCP格式
                Map<String, Object> result = new HashMap<>();
                result.put("content", java.util.List.of());
                result.put("isError", true);
                result.put("error", "MCP错误: " + error.get("message"));
                return result;
            }

        } catch (Exception e) {
            log.error("解析SSE响应失败: {}", sseResponse, e);
        }

        // 最终兜底返回MCP格式
        Map<String, Object> result = new HashMap<>();
        result.put("content", java.util.List.of());
        result.put("isError", true);
        result.put("error", "解析MCP响应失败");
        return result;
    }

    @Override
    public Map<String, Object> testToolWithSessionId(Long toolNum, Map<String, Object> arguments, String sessionId) {
        try {
            log.info("使用传入的 sessionId: {}", sessionId);

            if (sessionId == null || sessionId.trim().isEmpty()) {
                return createErrorResponse("sessionId 不能为空");
            }

            // 确保MCP连接已初始化（使用传入的sessionId）
            if (!isInitialized) {
                initializeConnection(sessionId);
            }

            // 修改查询逻辑：toolId实际上是toolNum
            McpToolDTO tool = null;
            try {
                // 先尝试通过toolNum查询（这是正确的方式）
                List<McpToolDTO> tools = mcpToolService.getMcpToolsByNumValid(toolNum, "1");
                if (!tools.isEmpty()) {
                    tool = tools.get(0);
                    log.info("通过toolNum {}找到工具: {}", toolNum, tool.getToolName());
                }
            } catch (Exception e) {
                log.warn("通过toolNum {}查询工具失败: {}", toolNum, e.getMessage());

                // 如果toolNum查询失败，尝试通过id查询
                try {
                    tool = mcpToolService.getMcpToolById(toolNum);
                    log.info("通过id {}找到工具: {}", toolNum, tool.getToolName());
                } catch (Exception e2) {
                    log.warn("通过id {}查询工具失败: {}", toolNum, e2.getMessage());
                }
            }

            if (tool == null) {
                // 如果还是找不到，列出所有可用工具供调试
                try {
                    List<McpToolDTO> allTools = mcpToolService.getAllMcpTools();
                    log.info("当前数据库中的所有工具: {}",
                            allTools.stream()
                                    .map(t -> String.format("ID:%d, ToolNum:%d, Name:%s",
                                            t.getId(), t.getToolNum(), t.getToolName()))
                                    .collect(java.util.stream.Collectors.toList()));
                } catch (Exception e) {
                    log.error("查询所有工具失败", e);
                }

                return createErrorResponse("工具不存在，toolNum: " + toolNum);
            }

            log.info("找到工具: ID={}, ToolNum={}, Name={}",
                    tool.getId(), tool.getToolNum(), tool.getToolName());

            // 构造MCP调用请求
            Map<String, Object> mcpRequest = new HashMap<>();
            mcpRequest.put("jsonrpc", "2.0");
            mcpRequest.put("id", requestId.incrementAndGet());
            mcpRequest.put("method", "tools/call");

            Map<String, Object> params = new HashMap<>();
            params.put("name", tool.getToolName());
            params.put("arguments", arguments != null ? arguments : new HashMap<>());
            mcpRequest.put("params", params);

            // 使用SSE方式发送请求，传入 sessionId
            String mcpUrl = mcpProperties.getServer().getUrl();
            log.info("准备调用MCP服务器: {}, 工具: {}, 参数: {}, sessionId: {}",
                    mcpUrl, tool.getToolName(), arguments, sessionId);
            String response = sendSseRequest(mcpUrl, mcpRequest, sessionId);

            if (response != null && !response.trim().isEmpty()) {
                log.info("收到MCP响应: {}", response);
                // 解析SSE响应
                return parseSseResponse(response);
            } else {
                log.error("MCP服务器无响应或响应为空");
                return createErrorResponse("MCP服务器无响应");
            }

        } catch (Exception e) {
            log.error("调用MCP工具时出错", e);
            return createErrorResponse("MCP客户端调用失败: " + e.getMessage());
        }
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("isError", true);
        result.put("error", error);
        return result;
    }

}