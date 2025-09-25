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
        log.info("一键保存工具数据: {}", dto);

        try {
            // 生成提供者工具编号（使用当前时间戳）
            Long providerToolNum = System.currentTimeMillis();

            // 1. 保存原始HTTP工具
            OriginToolHttp httpTool = new OriginToolHttp();
            httpTool.setProviderToolNum(providerToolNum);
            httpTool.setNameDisplay(dto.getHttpToolName());
            httpTool.setDescDisplay(dto.getHttpDescription());
            httpTool.setReqUrl(dto.getHttpToolUrl());
            httpTool.setReqMethod(dto.getHttpMethod());
            httpTool.setReqHeaders(dto.getHttpHeaders());
            httpTool.setCreateTime(LocalDateTime.now());
            httpTool.setCreateBy("system");
            httpTool.setUpdateTime(LocalDateTime.now());
            httpTool.setUpdateBy("system");

            // 根据输入生成JSON Schema
            if (dto.getHttpParams() != null && !dto.getHttpParams().trim().isEmpty()) {
                httpTool.setInputSchema(dto.getHttpParams());
            }

            originToolHttpService.save(httpTool);
            log.info("保存HTTP工具成功: {}", httpTool.getId());

            // 2. 保存MCP工具信息
            McpTool mcpTool = new McpTool();
            mcpTool.setToolNum(providerToolNum); // 使用相同的工具编号
            mcpTool.setToolVersion(1L);
            mcpTool.setValid("1");
            mcpTool.setToolName(dto.getMcpToolEnglishName());
            mcpTool.setToolDescription(dto.getMcpToolDescription());
            mcpTool.setNameDisplay(dto.getMcpToolChineseName());
            mcpTool.setDescriptionDisplay(dto.getMcpToolDescription());
            mcpTool.setConvertType("1"); // HTTP工具
            mcpTool.setToolType("1"); // tool类型
            mcpTool.setStreamOutput("0"); // 非流式输出
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
            converter.setReqUrl(dto.getHttpToolUrl());
            converter.setReqMethod(dto.getHttpMethod());
            converter.setReqHeaders(dto.getHttpHeaders());
            converter.setReqBody(dto.getHttpParams());
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