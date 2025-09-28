package cn.com.wind.mcp.registry.controller;

import java.time.LocalDateTime;

import cn.com.wind.mcp.registry.dto.UnifiedToolAddDto;
import cn.com.wind.mcp.registry.entity.HttpTemplateConverter;
import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.service.HttpTemplateConverterService;
import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.service.OriginToolHttpService;
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
     * 一键保存三部分关联内容
     */
    @PostMapping("/save-unified")
    @ResponseBody
    public String saveUnified(UnifiedToolAddDto dto) {
        log.info("收到一键保存请求，工具类型: {}", dto.getSelectedToolType());
        log.info("完整请求数据: {}", dto);

        // 基本验证
        if (dto.getSelectedToolType() == null || dto.getSelectedToolType().trim().isEmpty()) {
            log.error("工具类型不能为空");
            return "error";
        }

        try {
            // 生成提供者工具编号（使用当前时间戳）
            Long providerToolNum = System.currentTimeMillis();

            // 1. 根据工具类型保存原始工具
            if ("http".equals(dto.getSelectedToolType())) {
                // 保存原始HTTP工具
                OriginToolHttp httpTool = new OriginToolHttp();
                httpTool.setProviderToolNum(providerToolNum);
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
                log.info("当前版本暂不支持Expo工具保存，但已接收到Expo工具数据: {}", dto.getExpoToolName());
            }

            // 2. 保存MCP工具信息
            McpTool mcpTool = new McpTool();
            mcpTool.setToolNum(providerToolNum); // 使用相同的工具编号
            mcpTool.setToolVersion(1L);
            mcpTool.setValid("1");
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

            // 3. 保存HTTP模板转换器
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

            return "success";

        } catch (Exception e) {
            log.error("保存工具数据失败: ", e);
            return "error";
        }
    }
}