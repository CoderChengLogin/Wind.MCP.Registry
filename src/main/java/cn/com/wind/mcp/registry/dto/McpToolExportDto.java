package cn.com.wind.mcp.registry.dto;

import cn.com.wind.mcp.registry.entity.*;
import lombok.Data;

import java.io.Serializable;

/**
 * MCP工具导出/导入DTO
 * 用于工具的完整导出和导入,包含MCP工具信息、原始接口信息和转换模板信息
 *
 * @author system
 * @date 2025-10-21
 */
@Data
public class McpToolExportDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 导出格式版本号,用于后续兼容性处理
     */
    private String exportVersion = "1.0";

    /**
     * MCP工具信息(必填)
     */
    private McpTool mcpTool;

    /**
     * 原始HTTP接口信息(可选,仅当convertType为http时有值)
     */
    private OriginToolHttp originToolHttp;

    /**
     * 原始Expo接口信息(可选,仅当convertType为expo时有值)
     */
    private OriginToolExpo originToolExpo;

    /**
     * HTTP转换模板信息(可选,仅当convertType为http时有值)
     */
    private HttpTemplateConverter httpTemplateConverter;

    /**
     * Expo转换模板信息(可选,仅当convertType为expo时有值)
     */
    private ExpoTemplateConverter expoTemplateConverter;
}
