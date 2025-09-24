package cn.com.wind.mcp.registry.dto;

import lombok.Data;

/**
 * 工具向导DTO - 用于分步表单数据传输
 */
@Data
public class ToolWizardDto {

    /**
     * 工具类型: expo, http
     */
    private String toolType;

    /**
     * 原始工具信息
     */
    private OriginToolDto originTool;

    /**
     * MCP工具信息
     */
    private McpToolDto mcpTool;

    /**
     * 模板转换器信息
     */
    private ConverterDto converter;

    @Data
    public static class OriginToolDto {
        private String name;
        private String description;
        private String version;
        private Integer appClass;
        private Integer commandId;
        private String method;
        private String url;
        private String headers;
    }

    @Data
    public static class McpToolDto {
        private String toolName;
        private String toolType;
        private String toolDescription;
        private String nameDisplay;
        private String descriptionDisplay;
    }

    @Data
    public static class ConverterDto {
        private String inputArgs;
        private String outputArgs;
        private String inputSchema;
        private String outputSchema;
    }
}