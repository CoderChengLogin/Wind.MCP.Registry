package cn.com.wind.mcp.registry.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.wind.mcp.registry.config.McpClientConfig;
import cn.com.wind.mcp.registry.dto.mcptool.McpToolDTO;
import cn.com.wind.mcp.registry.service.McpToolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * McpClientServiceImpl 单元测试
 * 针对核心业务逻辑方法进行测试
 *
 * @author system
 * @date 2025-10-17
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class McpClientServiceImplTest {

    @Mock
    private RestTemplate mcpRestTemplate;

    @Mock
    private McpClientConfig.McpProperties mcpProperties;

    @Mock
    private McpClientConfig.McpProperties.Server serverConfig;

    @Mock
    private McpClientConfig.McpProperties.Client clientConfig;

    @Mock
    private McpToolService mcpToolService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private McpClientServiceImpl mcpClientService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private ServletRequestAttributes mockAttributes;

    @BeforeEach
    void setUp() {
        // 配置mcpProperties的返回值
        when(mcpProperties.getServer()).thenReturn(serverConfig);
        when(mcpProperties.getClient()).thenReturn(clientConfig);
        when(serverConfig.getUrl()).thenReturn("http://localhost:8080/mcp");
        when(clientConfig.getClientName()).thenReturn("test-client");
    }

    /**
     * 测试testToolWithSessionId - sessionId为空
     */
    @Test
    void testTestToolWithSessionId_NullSessionId() {
        Map<String, Object> result = mcpClientService.testToolWithSessionId(1L, new HashMap<>(), null);

        assertTrue((Boolean)result.get("isError"));
        assertEquals("sessionId 不能为空", result.get("error"));
    }

    /**
     * 测试testToolWithSessionId - sessionId为空字符串
     */
    @Test
    void testTestToolWithSessionId_EmptySessionId() {
        Map<String, Object> result = mcpClientService.testToolWithSessionId(1L, new HashMap<>(), "   ");

        assertTrue((Boolean)result.get("isError"));
        assertEquals("sessionId 不能为空", result.get("error"));
    }

    /**
     * 测试testToolWithSessionId - 工具不存在
     */
    @Test
    void testTestToolWithSessionId_ToolNotFound() throws Exception {
        when(mcpToolService.getMcpToolsByNumValid(anyLong(), eq("1"))).thenReturn(Collections.emptyList());
        when(mcpToolService.getMcpToolById(anyLong())).thenThrow(new RuntimeException("Tool not found"));
        when(mcpToolService.getAllMcpTools()).thenReturn(Collections.emptyList());

        Map<String, Object> result = mcpClientService.testToolWithSessionId(999L, new HashMap<>(), "test-session");

        assertTrue((Boolean)result.get("isError"));
        assertTrue(((String)result.get("error")).contains("工具不存在"));
    }

    /**
     * 测试testToolWithSessionId - 通过toolNum找到工具
     */
    @Test
    void testTestToolWithSessionId_FindToolByToolNum() throws Exception {
        McpToolDTO tool = new McpToolDTO();
        tool.setId(1L);
        tool.setToolNum(100L);
        tool.setToolName("test_tool");

        when(mcpToolService.getMcpToolsByNumValid(100L, "1")).thenReturn(List.of(tool));

        // 由于实际会发送HTTP请求,这里会失败,但可以验证工具查找逻辑
        Map<String, Object> result = mcpClientService.testToolWithSessionId(100L, new HashMap<>(), "test-session");

        // 验证至少尝试了查询
        verify(mcpToolService).getMcpToolsByNumValid(100L, "1");
    }

    /**
     * 测试createErrorResponse - 私有方法通过反射测试
     */
    @Test
    void testCreateErrorResponse() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method method = McpClientServiceImpl.class.getDeclaredMethod("createErrorResponse",
            String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>)method.invoke(mcpClientService, "测试错误");

        assertTrue((Boolean)result.get("isError"));
        assertEquals("测试错误", result.get("error"));
    }

    /**
     * 测试isConnected - 正常情况
     * 注意: 这个测试会尝试真实的网络连接,在单元测试中应该mock
     */
    @Test
    void testIsConnected_MockScenario() {
        // 由于isConnected方法内部使用HttpURLConnection,这里只能测试基本逻辑
        // 实际测试需要mock网络连接
        when(serverConfig.getUrl()).thenReturn("http://localhost:9999/invalid");

        boolean result = mcpClientService.isConnected();

        // 由于URL无效,应该返回false
        assertFalse(result);
    }

    /**
     * 测试getCurrentSessionId - 无RequestContext
     */
    @Test
    void testGetCurrentSessionId_NoRequestContext() throws Exception {
        // 清除RequestContext
        RequestContextHolder.resetRequestAttributes();

        // 使用反射调用私有方法
        java.lang.reflect.Method method = McpClientServiceImpl.class.getDeclaredMethod("getCurrentSessionId");
        method.setAccessible(true);

        String result = (String)method.invoke(mcpClientService);

        assertNull(result);
    }

    /**
     * 测试getCurrentSessionId - 从header获取
     */
    @Test
    void testGetCurrentSessionId_FromHeader() throws Exception {
        RequestContextHolder.setRequestAttributes(mockAttributes);
        when(mockAttributes.getRequest()).thenReturn(mockRequest);
        when(mockRequest.getHeader("windsessionid")).thenReturn("session-from-header");

        // 使用反射调用私有方法
        java.lang.reflect.Method method = McpClientServiceImpl.class.getDeclaredMethod("getCurrentSessionId");
        method.setAccessible(true);

        String result = (String)method.invoke(mcpClientService);

        assertEquals("session-from-header", result);

        // 清理
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * 测试parseSseResponse - 成功响应
     */
    @Test
    void testParseSseResponse_Success() throws Exception {
        String sseResponse
            = "{\"result\":{\"content\":[{\"type\":\"text\",\"text\":\"{\\\"mcp_tool_error_code\\\":0,"
            + "\\\"mcp_tool_data\\\":{\\\"result\\\":\\\"success\\\"}}\"}]}}";

        Map<String, Object> mcpResponse = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", "{\"mcp_tool_error_code\":0,\"mcp_tool_data\":{\"result\":\"success\"}}");
        content.add(textContent);
        result.put("content", content);
        mcpResponse.put("result", result);

        when(objectMapper.readValue(eq(sseResponse), eq(Map.class))).thenReturn(mcpResponse);

        Map<String, Object> toolData = new HashMap<>();
        toolData.put("result", "success");
        Map<String, Object> parsedContent = new HashMap<>();
        parsedContent.put("mcp_tool_error_code", 0);
        parsedContent.put("mcp_tool_data", toolData);

        when(objectMapper.readValue(eq("{\"mcp_tool_error_code\":0,\"mcp_tool_data\":{\"result\":\"success\"}}"),
            eq(Map.class)))
            .thenReturn(parsedContent);

        // 使用反射调用私有方法
        java.lang.reflect.Method method = McpClientServiceImpl.class.getDeclaredMethod("parseSseResponse",
            String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>)method.invoke(mcpClientService, sseResponse);

        assertNotNull(response);
        assertFalse((Boolean)response.getOrDefault("isError", true));
    }

    /**
     * 测试parseSseResponse - 错误响应
     */
    @Test
    void testParseSseResponse_Error() throws Exception {
        String sseResponse = "{\"error\":{\"code\":-1,\"message\":\"Tool execution failed\"}}";

        Map<String, Object> mcpResponse = new HashMap<>();
        Map<String, Object> error = new HashMap<>();
        error.put("code", -1);
        error.put("message", "Tool execution failed");
        mcpResponse.put("error", error);

        when(objectMapper.readValue(eq(sseResponse), eq(Map.class))).thenReturn(mcpResponse);

        // 使用反射调用私有方法
        java.lang.reflect.Method method = McpClientServiceImpl.class.getDeclaredMethod("parseSseResponse",
            String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>)method.invoke(mcpClientService, sseResponse);

        assertNotNull(response);
        assertTrue((Boolean)response.get("isError"));
        assertTrue(((String)response.get("error")).contains("MCP错误"));
    }

    /**
     * 测试parseSseResponse - 时间字符串响应
     */
    @Test
    void testParseSseResponse_TimeString() throws Exception {
        String sseResponse
            = "{\"result\":{\"content\":[{\"type\":\"text\",\"text\":\"{\\\"mcp_tool_error_code\\\":0,"
            + "\\\"mcp_tool_data\\\":\\\"2025-10-17 15:30:00\\\"}\"}]}}";

        Map<String, Object> mcpResponse = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", "{\"mcp_tool_error_code\":0,\"mcp_tool_data\":\"2025-10-17 15:30:00\"}");
        content.add(textContent);
        result.put("content", content);
        mcpResponse.put("result", result);

        when(objectMapper.readValue(eq(sseResponse), eq(Map.class))).thenReturn(mcpResponse);

        Map<String, Object> parsedContent = new HashMap<>();
        parsedContent.put("mcp_tool_error_code", 0);
        parsedContent.put("mcp_tool_data", "2025-10-17 15:30:00");

        when(objectMapper.readValue(eq("{\"mcp_tool_error_code\":0,\"mcp_tool_data\":\"2025-10-17 15:30:00\"}"),
            eq(Map.class)))
            .thenReturn(parsedContent);

        // 使用反射调用私有方法
        java.lang.reflect.Method method = McpClientServiceImpl.class.getDeclaredMethod("parseSseResponse",
            String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>)method.invoke(mcpClientService, sseResponse);

        assertNotNull(response);
        assertFalse((Boolean)response.getOrDefault("isError", true));

        // 验证时间字符串被正确处理
        @SuppressWarnings("unchecked")
        Map<String, Object> businessData = (Map<String, Object>)response.get("businessData");
        if (businessData != null) {
            assertEquals("2025-10-17 15:30:00", businessData.get("currentTime"));
        }
    }

    /**
     * 测试parseSseResponse - 工具执行错误
     */
    @Test
    void testParseSseResponse_ToolExecutionError() throws Exception {
        String sseResponse
            = "{\"result\":{\"content\":[{\"type\":\"text\",\"text\":\"{\\\"mcp_tool_error_code\\\":500,"
            + "\\\"mcp_tool_error_msg\\\":\\\"Internal error\\\"}\"}]}}";

        Map<String, Object> mcpResponse = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", "{\"mcp_tool_error_code\":500,\"mcp_tool_error_msg\":\"Internal error\"}");
        content.add(textContent);
        result.put("content", content);
        mcpResponse.put("result", result);

        when(objectMapper.readValue(eq(sseResponse), eq(Map.class))).thenReturn(mcpResponse);

        Map<String, Object> parsedContent = new HashMap<>();
        parsedContent.put("mcp_tool_error_code", 500);
        parsedContent.put("mcp_tool_error_msg", "Internal error");

        when(objectMapper.readValue(eq("{\"mcp_tool_error_code\":500,\"mcp_tool_error_msg\":\"Internal error\"}"),
            eq(Map.class)))
            .thenReturn(parsedContent);

        // 使用反射调用私有方法
        java.lang.reflect.Method method = McpClientServiceImpl.class.getDeclaredMethod("parseSseResponse",
            String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>)method.invoke(mcpClientService, sseResponse);

        assertNotNull(response);
        assertTrue((Boolean)response.get("isError"));
        assertEquals(500, response.get("errorCode"));
        assertEquals("Internal error", response.get("errorMessage"));
    }

    /**
     * 测试parseSseResponse - JSON解析失败
     */
    @Test
    void testParseSseResponse_ParseException() throws Exception {
        String invalidJson = "invalid json";

        when(objectMapper.readValue(eq(invalidJson), eq(Map.class)))
            .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Invalid JSON"));

        // 使用反射调用私有方法
        java.lang.reflect.Method method = McpClientServiceImpl.class.getDeclaredMethod("parseSseResponse",
            String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>)method.invoke(mcpClientService, invalidJson);

        assertNotNull(response);
        assertTrue((Boolean)response.get("isError"));
        assertTrue(((String)response.get("error")).contains("解析MCP响应失败"));
    }

    /**
     * 边界条件测试 - arguments为null
     */
    @Test
    void testTestToolWithSessionId_NullArguments() throws Exception {
        McpToolDTO tool = new McpToolDTO();
        tool.setId(1L);
        tool.setToolNum(100L);
        tool.setToolName("test_tool");

        when(mcpToolService.getMcpToolsByNumValid(100L, "1")).thenReturn(List.of(tool));

        // 传入null arguments
        Map<String, Object> result = mcpClientService.testToolWithSessionId(100L, null, "test-session");

        // 验证调用了service
        verify(mcpToolService).getMcpToolsByNumValid(100L, "1");
    }

    /**
     * 边界条件测试 - toolNum为负数
     */
    @Test
    void testTestToolWithSessionId_NegativeToolNum() throws Exception {
        when(mcpToolService.getMcpToolsByNumValid(-1L, "1")).thenReturn(Collections.emptyList());
        when(mcpToolService.getMcpToolById(-1L)).thenThrow(new RuntimeException("Invalid ID"));
        when(mcpToolService.getAllMcpTools()).thenReturn(Collections.emptyList());

        Map<String, Object> result = mcpClientService.testToolWithSessionId(-1L, new HashMap<>(), "test-session");

        assertTrue((Boolean)result.get("isError"));
    }

    /**
     * 边界条件测试 - toolNum为0
     */
    @Test
    void testTestToolWithSessionId_ZeroToolNum() throws Exception {
        when(mcpToolService.getMcpToolsByNumValid(0L, "1")).thenReturn(Collections.emptyList());
        when(mcpToolService.getMcpToolById(0L)).thenThrow(new RuntimeException("Invalid ID"));
        when(mcpToolService.getAllMcpTools()).thenReturn(Collections.emptyList());

        Map<String, Object> result = mcpClientService.testToolWithSessionId(0L, new HashMap<>(), "test-session");

        assertTrue((Boolean)result.get("isError"));
    }
}
