package cn.com.wind.mcp.registry.controller;

import java.time.LocalDateTime;

import javax.servlet.http.HttpSession;

import cn.com.wind.mcp.registry.dto.UnifiedToolAddDto;
import cn.com.wind.mcp.registry.entity.ExpoTemplateConverter;
import cn.com.wind.mcp.registry.entity.HttpTemplateConverter;
import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.service.ExpoTemplateConverterService;
import cn.com.wind.mcp.registry.service.HttpTemplateConverterService;
import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.service.OriginToolHttpService;
import cn.com.wind.mcp.registry.util.PermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 工具录入向导Controller
 * 一体化录入页面：源工具 + 工具信息 + 转换模板
 */
@Slf4j
@Controller
@RequestMapping("/tool-wizard")
public class ToolWizardController {

    @Autowired
    private OriginToolHttpService originToolHttpService;

    @Autowired
    private McpToolService mcpToolService;

    @Autowired
    private HttpTemplateConverterService httpTemplateConverterService;

    @Autowired
    private ExpoTemplateConverterService expoTemplateConverterService;

    /**
     * 工具录入向导首页
     */
    @GetMapping("")
    public String index(Model model) {
        log.info("访问工具录入向导");
        return "tool-wizard/index";
    }

    /**
     * 一体化工具录入页面
     */
    @GetMapping("/unified-add")
    public String unifiedAdd(Model model) {
        log.info("访问一体化工具录入页面");
        return "tool-wizard/unified-add";
    }

    /**
     * 一键保存工具数据
     * 支持两种场景:
     * 1. 完整流程: 原始工具(步骤1-2) + MCP工具(步骤3) + 转换模板(步骤4)
     * 2. 简化流程: 仅MCP工具(步骤3) + 转换模板(步骤4), 跳过步骤1-2
     */
    @PostMapping("/save-unified")
    @ResponseBody
    public String saveUnified(UnifiedToolAddDto dto, HttpSession session) {
        log.info("收到工具保存请求");
        log.info("完整请求数据: {}", dto);

        try {
            // 获取当前登录用户ID
            Long currentProviderId = PermissionUtil.getCurrentProviderId(session);
            if (currentProviderId == null) {
                log.error("用户未登录");
                return "error:用户未登录,请先登录";
            }

            // 生成提供者工具编号（使用当前时间戳）
            Long providerToolNum = System.currentTimeMillis();

            // 场景判断: 如果selectedToolType不为空,说明用户选择了原始工具类型(完整流程)
            boolean hasOriginTool = dto.getSelectedToolType() != null &&
                !dto.getSelectedToolType().trim().isEmpty() &&
                !"skip".equals(dto.getSelectedToolType());

            // 1. 如果有原始工具,则保存原始工具(步骤1-2)
            if (hasOriginTool) {
                log.info("检测到原始工具类型: {}, 执行完整流程", dto.getSelectedToolType());

                if ("http".equals(dto.getSelectedToolType())) {
                    // 保存原始HTTP工具
                    OriginToolHttp httpTool = new OriginToolHttp();
                    httpTool.setProviderToolNum(providerToolNum);
                    httpTool.setProviderId(currentProviderId);
                    httpTool.setNameDisplay(dto.getNameDisplay());
                    httpTool.setDescDisplay(dto.getDescDisplay());
                    httpTool.setReqUrl(dto.getReqUrl());
                    httpTool.setReqMethod(dto.getReqMethod());
                    httpTool.setReqHeaders(dto.getReqHeaders());
                    httpTool.setInputSchema(dto.getInputSchema());
                    httpTool.setOutputSchema(dto.getOutputSchema());
                    httpTool.setProviderAppNum(
                        dto.getProviderAppNum() != null ? Long.valueOf(dto.getProviderAppNum()) : null);
                    httpTool.setCreateTime(LocalDateTime.now());
                    httpTool.setCreateBy("system");
                    httpTool.setUpdateTime(LocalDateTime.now());
                    httpTool.setUpdateBy("system");

                    originToolHttpService.save(httpTool);
                    log.info("保存HTTP工具成功: {}", httpTool.getId());
                } else if ("expo".equals(dto.getSelectedToolType())) {
                    // 这里可以添加Expo工具的保存逻辑
                    log.info("当前版本暂不支持Expo工具保存，但已接收到Expo工具数据");
                }
            } else {
                log.info("未选择原始工具类型, 执行简化流程(仅录入MCP工具和转换模板)");
            }

            // 2. 保存MCP工具信息(步骤3 - 必填)
            if (dto.getToolName() == null || dto.getToolName().trim().isEmpty()) {
                log.error("MCP工具名称不能为空");
                return "error:MCP工具名称不能为空";
            }

            McpTool mcpTool = new McpTool();
            mcpTool.setToolNum(providerToolNum); // 使用相同的工具编号
            mcpTool.setToolVersion(1L);
            mcpTool.setValid("1");
            mcpTool.setProviderId(currentProviderId); // 设置提供者ID
            mcpTool.setToolName(dto.getToolName());
            mcpTool.setToolDescription(dto.getToolDescription());
            mcpTool.setNameDisplay(dto.getMcpNameDisplay());
            mcpTool.setDescriptionDisplay(dto.getDescriptionDisplay());
            mcpTool.setInputSchema(dto.getMcpInputSchema());
            mcpTool.setOutputSchema(dto.getMcpOutputSchema());
            mcpTool.setStreamOutput(dto.getStreamOutput());
            mcpTool.setConvertType(dto.getConvertType());
            mcpTool.setToolType(dto.getToolType());
            mcpTool.setCreateTime(LocalDateTime.now());
            mcpTool.setCreateBy("system");
            mcpTool.setUpdateTime(LocalDateTime.now());
            mcpTool.setUpdateBy("system");

            mcpToolService.save(mcpTool);
            log.info("保存MCP工具成功: {}", mcpTool.getId());

            // 3. 保存模板转换器(步骤4 - 必填)
            // 根据原始工具类型决定使用哪种模板转换器
            String converterType = determineConverterType(dto);
            log.info("确定的模板转换器类型: {}", converterType);

            if ("http".equals(converterType)) {
                // 保存HTTP模板转换器
                if (dto.getTemplateReqUrl() == null || dto.getTemplateReqUrl().trim().isEmpty()) {
                    log.error("HTTP模板转换器URL不能为空");
                    return "error:HTTP模板转换器URL不能为空";
                }

                HttpTemplateConverter converter = new HttpTemplateConverter();
                converter.setToolNum(providerToolNum);
                converter.setToolVersion(1L);
                converter.setReqUrl(dto.getTemplateReqUrl());
                converter.setReqMethod(dto.getTemplateReqMethod());
                converter.setReqHeaders(dto.getTemplateReqHeaders());
                converter.setReqBody(dto.getReqBody());
                converter.setRespBody(dto.getRespBody());
                converter.setProviderToolNum(providerToolNum);
                converter.setCreateTime(LocalDateTime.now());
                converter.setCreateBy("system");
                converter.setUpdateTime(LocalDateTime.now());
                converter.setUpdateBy("system");

                httpTemplateConverterService.save(converter);
                log.info("保存HTTP模板转换器成功: {}", converter.getId());

            } else if ("expo".equals(converterType)) {
                // 保存Expo模板转换器
                if (dto.getTemplateAppClass() == null) {
                    log.error("Expo模板转换器AppClass不能为空");
                    return "error:Expo模板转换器AppClass不能为空";
                }
                if (dto.getTemplateCommandId() == null) {
                    log.error("Expo模板转换器CommandId不能为空");
                    return "error:Expo模板转换器CommandId不能为空";
                }

                ExpoTemplateConverter converter = new ExpoTemplateConverter();
                converter.setToolNum(providerToolNum);
                converter.setToolVersion(1L);
                converter.setAppClass(dto.getTemplateAppClass());
                converter.setCommandId(dto.getTemplateCommandId());
                converter.setInputArgs(dto.getTemplateInputArgs());
                converter.setOutputArgs(dto.getTemplateOutputArgs());
                converter.setProviderToolNum(providerToolNum);
                converter.setCreateTime(LocalDateTime.now());
                converter.setCreateBy("system");
                converter.setUpdateTime(LocalDateTime.now());
                converter.setUpdateBy("system");

                expoTemplateConverterService.save(converter);
                log.info("保存Expo模板转换器成功: {}", converter.getId());

            } else {
                log.error("未知的模板转换器类型: {}", converterType);
                return "error:未知的模板转换器类型: " + converterType;
            }

            return "success";

        } catch (Exception e) {
            log.error("保存工具数据失败: ", e);
            return "error:" + e.getMessage();
        }
    }

    /**
     * 确定模板转换器类型
     * 规则:
     * 1. 如果选择了原始工具类型(完整流程),则使用原始工具类型
     * 2. 如果未选择原始工具类型(简化流程),则根据MCP工具的convertType字段确定
     *
     * @param dto 统一工具录入DTO
     * @return 模板转换器类型: "http" 或 "expo"
     */
    private String determineConverterType(UnifiedToolAddDto dto) {
        // 场景1: 完整流程 - 选择了原始工具类型
        if (dto.getSelectedToolType() != null &&
            !dto.getSelectedToolType().trim().isEmpty() &&
            !"skip".equals(dto.getSelectedToolType())) {
            log.info("完整流程: 使用原始工具类型 {}", dto.getSelectedToolType());
            return dto.getSelectedToolType();
        }

        // 场景2: 简化流程 - 根据MCP工具的convertType确定
        // convertType可能的值: "http", "expo" 等
        if (dto.getConvertType() != null && !dto.getConvertType().trim().isEmpty()) {
            log.info("简化流程: 使用MCP工具的convertType {}", dto.getConvertType());
            return dto.getConvertType();
        }

        // 默认使用HTTP
        log.warn("无法确定模板转换器类型,默认使用HTTP");
        return "http";
    }
}