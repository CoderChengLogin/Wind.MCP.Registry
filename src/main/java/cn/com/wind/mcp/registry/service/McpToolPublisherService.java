package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.dto.McpToolPublishDto;

/**
 * MCP工具发布服务接口
 * 负责将工具数据发布到目标数据库 wind_mcp_server
 *
 * @author system
 */
public interface McpToolPublisherService {

    /**
     * 发布工具到目标数据库
     *
     * @param publishDto 发布数据传输对象
     * @throws Exception 发布失败时抛出异常
     */
    void publishTool(McpToolPublishDto publishDto) throws Exception;

    /**
     * 检查并初始化目标数据库
     * 如果wind_mcp_server数据库不存在，则创建
     *
     * @throws Exception 初始化失败时抛出异常
     */
    void initializeTargetDatabase() throws Exception;
}
