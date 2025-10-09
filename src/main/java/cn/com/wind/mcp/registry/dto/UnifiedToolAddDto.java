package cn.com.wind.mcp.registry.dto;

import lombok.Data;

/**
 * 一体化工具录入DTO
 * 包含：源工具 + 工具信息 + 转换模板
 */
@Data
public class UnifiedToolAddDto {

    // ===== 工具类型 =====
    private String selectedToolType;  // 选择的工具类型

    // ===== 源工具信息 (HTTP接口) - 对应Bug2.md第二步 =====
    private String nameDisplay;      // 名称 -> name_display
    private String descDisplay;      // 功能描述 -> desc_display
    private String reqUrl;          // 请求url -> req_url
    private String reqMethod;       // 请求方式 -> req_method
    private String reqHeaders;      // 请求头 -> req_headers
    private String inputSchema;     // 输入参数 -> input_schema
    private String outputSchema;    // 输出参数 -> output_schema
    private String providerAppNum;  // 服务方app -> provider_app_num

    // ===== 源工具信息 (Expo工具) =====
    private String expoToolName;      // 工具名称
    private String expoToolVersion;   // 工具版本
    private String expoAppClass;      // 应用类别
    private String expoCommandId;     // 命令ID
    private String expoDescription;   // 工具描述

    // ===== MCP工具信息 - 对应Bug2.md第三步 =====
    private String toolName;           // MCP工具英文名 -> tool_name
    private String toolDescription;    // MCP工具描述 -> tool_description
    private String mcpNameDisplay;     // 多语言名称 -> name_display
    private String descriptionDisplay; // 多语言描述 -> description_display
    private String mcpInputSchema;     // input_json_schema -> input_schema
    private String mcpOutputSchema;    // output_json_schema -> output_schema
    private String streamOutput;       // 是否流式输出 -> stream_output
    private String convertType;        // 转换模板类型 -> convert_type
    private String toolType;           // MCP工具类型 -> tool_type

    // ===== 配置模板转换器 - 对应Bug2.md第四步 =====
    private String templateReqUrl;     // 请求路径 -> req_url
    private String templateReqMethod;  // 请求方式 -> req_method
    private String templateReqHeaders; // 请求头（jinja2） -> req_headers
    private String reqBody;            // 请求体（jinja2） -> req_body
    private String respBody;           // 响应体（jinja2） -> resp_body
}