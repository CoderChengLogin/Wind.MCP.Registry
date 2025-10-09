package cn.com.wind.mcp.registry.dto;

import cn.com.wind.mcp.registry.entity.HttpTemplateConverter;
import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import lombok.Data;

/**
 * MCP工具编辑DTO
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Data
public class McpToolEditDto extends McpTool {

    /**
     * 原始HTTP接口信息
     */
    private OriginToolHttp httpTool;

    /**
     * 转换器信息
     */
    private HttpTemplateConverter converter;
}