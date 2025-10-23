package cn.com.wind.mcp.registry.dto;

import lombok.Data;

/**
 * MCP工具发布数据传输对象
 * 用于前端提交发布请求时传递完整的工具信息
 *
 * @author system
 */
@Data
public class McpToolPublishDto {

    /**
     * MCP工具ID (必填)
     */
    private Long id;

    /**
     * 工具编号
     */
    private Long toolNum;

    /**
     * 工具版本
     */
    private Long toolVersion;

    /**
     * 有效标志
     */
    private String valid;

    /**
     * 工具英文名称
     */
    private String toolName;

    /**
     * 工具描述
     */
    private String toolDescription;

    /**
     * 多语言显示名称 (JSON格式)
     */
    private String nameDisplay;

    /**
     * 多语言显示描述 (JSON格式)
     */
    private String descriptionDisplay;

    /**
     * 输入JSON Schema
     */
    private String inputSchema;

    /**
     * 输出JSON Schema
     */
    private String outputSchema;

    /**
     * 是否流式输出 (0: 否, 1: 是)
     */
    private String streamOutput;

    /**
     * 转换模板类型 (1: HTTP, 2: Expo, 3: Manual)
     */
    private String convertType;

    /**
     * 工具类型 (1: tool, 2: agent)
     */
    private String toolType;

    /**
     * 提供者ID
     */
    private Long providerId;

    // ===== 原始HTTP工具信息 =====

    /**
     * HTTP工具URL
     */
    private String httpReqUrl;

    /**
     * HTTP请求方法
     */
    private String httpReqMethod;

    /**
     * HTTP请求头
     */
    private String httpReqHeaders;

    /**
     * HTTP输入Schema
     */
    private String httpInputSchema;

    /**
     * HTTP输出Schema
     */
    private String httpOutputSchema;

    // ===== 原始Expo工具信息 =====

    /**
     * Expo应用类别
     */
    private Integer expoAppClass;

    /**
     * Expo命令ID
     */
    private Integer expoCommandId;

    /**
     * Expo函数名称
     */
    private String expoFunctionName;

    /**
     * Expo API定义
     */
    private String expoApiDefine;

    // ===== HTTP模板转换器信息 =====

    /**
     * HTTP模板URL
     */
    private String httpTemplateReqUrl;

    /**
     * HTTP模板请求方法
     */
    private String httpTemplateReqMethod;

    /**
     * HTTP模板请求头
     */
    private String httpTemplateReqHeaders;

    /**
     * HTTP模板请求体
     */
    private String httpTemplateReqBody;

    /**
     * HTTP模板响应体
     */
    private String httpTemplateRespBody;

    // ===== Expo模板转换器信息 =====

    /**
     * Expo模板应用类别
     */
    private Integer expoTemplateAppClass;

    /**
     * Expo模板命令ID
     */
    private Integer expoTemplateCommandId;

    /**
     * Expo模板输入参数
     */
    private String expoTemplateInputArgs;

    /**
     * Expo模板输出参数
     */
    private String expoTemplateOutputArgs;
}
