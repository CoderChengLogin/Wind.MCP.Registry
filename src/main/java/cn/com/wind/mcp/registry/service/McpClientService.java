package cn.com.wind.mcp.registry.service;

import java.util.Map;

public interface McpClientService {

    /**
     * 测试工具调用
     *
     * @param toolId 工具ID     * @param arguments 工具参数
     * @return 测试结果
     */
    Map<String, Object> testTool(Long toolId, Map<String, Object> arguments);

    Map<String, Object> testToolWithSessionId(Long toolId, Map<String, Object> arguments, String sessionId);

    /**
     * 检查MCP连接状态
     *
     * @return 连接状态
     */
    boolean isConnected();
}