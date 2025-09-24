package cn.com.wind.mcp.registry.dto;

import lombok.Data;

/**
 * 一体化工具录入DTO
 * 包含：源工具 + 工具信息 + 转换模板
 */
@Data
public class UnifiedToolAddDto {

    // ===== 源工具信息 (HTTP工具) =====
    private String httpToolName;
    private String httpToolUrl;
    private String httpMethod;
    private String httpHeaders;
    private String httpParams;
    private String httpDescription;

    // ===== MCP工具信息 =====
    private String mcpToolEnglishName;
    private String mcpToolChineseName;
    private String mcpToolType;
    private String mcpToolDescription;
    private String mcpToolUsage;
    private String mcpToolParameters;
    private String mcpToolReturns;
    private String mcpToolExamples;

    // ===== 转换模板信息 =====
    private String templateName;
    private String templateContent;
    private String templateDescription;
    private String mappingRules;
}