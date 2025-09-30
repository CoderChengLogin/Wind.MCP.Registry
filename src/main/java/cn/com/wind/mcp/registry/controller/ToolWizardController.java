package cn.com.wind.mcp.registry.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import cn.com.wind.mcp.registry.dto.UnifiedToolAddDto;
import cn.com.wind.mcp.registry.entity.HttpTemplateConverter;
import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.HttpTemplateConverterService;
import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.service.OriginToolHttpService;
import cn.com.wind.mcp.registry.util.PermissionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 工具录入向导Controller
 * 一体化录入页面：源工具 + 工具信息 + 转换模板
 *
 * @author system
 * @date Created in 2024-01-01
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
     *
     * @param model 模型对象
     * @return 视图名称
     */
    @GetMapping("")
    public String index(Model model) {
        log.info("访问工具录入向导");
        return "tool-wizard/index";
    }

    /**
     * 一体化工具录入页面
     *
     * @param model 模型对象
     * @return 视图名称
     */
    @GetMapping("/unified-add")
    public String unifiedAdd(Model model) {
        log.info("访问一体化工具录入页面");
        return "tool-wizard/unified-add";
    }

    /**
     * 一键保存三部分关联内容
     *
     * @param dto 统一工具录入数据传输对象
     * @return 保存结果
     */
    @PostMapping("/save-unified")
    public String saveUnified(UnifiedToolAddDto dto, HttpSession session, RedirectAttributes redirectAttributes) {
        log.info("收到一键保存请求，工具类型: {}", dto.getSelectedToolType());
        log.info("完整请求数据: {}", dto);

        try {
            // 获取当前登录用户
            Provider currentProvider = PermissionUtil.getCurrentProvider(session);
            if (currentProvider == null) {
                log.error("用户未登录");
                return "用户未登录，请重新登录";
            }

            String currentUser = currentProvider.getUsername();
            log.info("当前登录用户: {}", currentUser);

            // 后端数据校验
            List<String> validationErrors = validateUnifiedToolData(dto);
            if (!validationErrors.isEmpty()) {
                log.error("数据校验失败: {}", validationErrors);
                return "数据校验失败: " + String.join("; ", validationErrors);
            }

            // 生成提供者工具编号（使用当前时间戳）
            Long providerToolNum = System.currentTimeMillis();

            // 1. 根据工具类型保存原始工具
            if ("http".equals(dto.getSelectedToolType())) {
                saveHttpTool(dto, providerToolNum, currentUser, session);
            } else if ("expo".equals(dto.getSelectedToolType())) {
                // 这里可以添加Expo工具的保存逻辑
                log.info("当前版本暂不支持Expo工具保存，但已接收到Expo工具数据: {}", dto.getExpoToolName());
            }

            // 2. 保存MCP工具信息
            saveMcpTool(dto, providerToolNum, currentUser, session);

            // 3. 保存HTTP模板转换器
            saveHttpTemplateConverter(dto, providerToolNum, currentUser, session);

            log.info("一键保存成功，工具编号: {}", providerToolNum);

            // 根据Bug3.md要求：新增工具成功后要跳转到list.html页面
            try {
                return "redirect:/mcp-tools?success=" + URLEncoder.encode("工具创建成功",
                    StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException ex) {
                log.error("URL编码失败", ex);
                return "redirect:/mcp-tools";
            }

        } catch (Exception e) {
            log.error("保存工具数据失败: ", e);
            redirectAttributes.addFlashAttribute("error", "保存失败: " + e.getMessage());
            return "redirect:/tool-wizard/unified-add";
        }
    }

    /**
     * 统一数据校验
     *
     * @param dto 数据传输对象
     * @return 校验错误列表
     */
    private List<String> validateUnifiedToolData(UnifiedToolAddDto dto) {
        List<String> errors = new ArrayList<>();

        // 基本验证
        if (StrUtil.isBlank(dto.getSelectedToolType())) {
            errors.add("工具类型不能为空");
            return errors; // 工具类型为空时直接返回
        }

        // 根据工具类型进行不同的校验
        if ("http".equals(dto.getSelectedToolType())) {
            validateHttpToolData(dto, errors);
        } else if ("expo".equals(dto.getSelectedToolType())) {
            validateExpoToolData(dto, errors);
        }

        // MCP工具信息校验
        validateMcpToolData(dto, errors);

        // 模板转换器校验
        validateTemplateConverterData(dto, errors);

        return errors;
    }

    /**
     * HTTP工具数据校验
     *
     * @param dto    数据传输对象
     * @param errors 错误列表
     */
    private void validateHttpToolData(UnifiedToolAddDto dto, List<String> errors) {
        if (StrUtil.isBlank(dto.getNameDisplay())) {
            errors.add("HTTP工具名称不能为空");
        } else if (dto.getNameDisplay().trim().length() < 2) {
            errors.add("HTTP工具名称至少需要2个字符");
        } else if (dto.getNameDisplay().trim().length() > 100) {
            errors.add("HTTP工具名称不能超过100个字符");
        }

        if (StrUtil.isBlank(dto.getDescDisplay())) {
            errors.add("HTTP工具功能描述不能为空");
        } else if (dto.getDescDisplay().trim().length() < 10) {
            errors.add("HTTP工具功能描述至少需要10个字符");
        } else if (dto.getDescDisplay().trim().length() > 500) {
            errors.add("HTTP工具功能描述不能超过500个字符");
        }

        if (StrUtil.isBlank(dto.getReqUrl())) {
            errors.add("请求URL不能为空");
        } else if (!isValidUrl(dto.getReqUrl())) {
            errors.add("请求URL格式不正确");
        }

        if (StrUtil.isBlank(dto.getReqMethod())) {
            errors.add("请求方式不能为空");
        }

        if (StrUtil.isBlank(dto.getReqHeaders())) {
            errors.add("请求头不能为空");
        } else if (!isValidJson(dto.getReqHeaders())) {
            errors.add("请求头必须是有效的JSON格式");
        }

        if (StrUtil.isBlank(dto.getInputSchema())) {
            errors.add("输入参数不能为空");
        } else if (!isValidJson(dto.getInputSchema())) {
            errors.add("输入参数必须是有效的JSON格式");
        }

        if (StrUtil.isBlank(dto.getOutputSchema())) {
            errors.add("输出参数不能为空");
        } else if (!isValidJson(dto.getOutputSchema())) {
            errors.add("输出参数必须是有效的JSON格式");
        }

        if (StrUtil.isBlank(dto.getProviderAppNum())) {
            errors.add("服务方app编号不能为空");
        }
    }

    /**
     * Expo工具数据校验
     *
     * @param dto    数据传输对象
     * @param errors 错误列表
     */
    private void validateExpoToolData(UnifiedToolAddDto dto, List<String> errors) {
        if (StrUtil.isBlank(dto.getExpoToolName())) {
            errors.add("Expo工具名称不能为空");
        } else if (!isValidToolName(dto.getExpoToolName())) {
            errors.add("Expo工具名称只能包含字母、数字、连字符和下划线，长度3-50个字符");
        }

        if (StrUtil.isBlank(dto.getExpoToolVersion())) {
            errors.add("Expo工具版本不能为空");
        } else if (!isValidVersion(dto.getExpoToolVersion())) {
            errors.add("Expo工具版本号格式不正确，请使用语义化版本（如：1.0.0）");
        }

        if (StrUtil.isBlank(dto.getExpoDescription())) {
            errors.add("Expo工具描述不能为空");
        } else if (dto.getExpoDescription().trim().length() < 10) {
            errors.add("Expo工具描述至少需要10个字符");
        } else if (dto.getExpoDescription().trim().length() > 500) {
            errors.add("Expo工具描述不能超过500个字符");
        }
    }

    /**
     * MCP工具数据校验
     *
     * @param dto    数据传输对象
     * @param errors 错误列表
     */
    private void validateMcpToolData(UnifiedToolAddDto dto, List<String> errors) {
        if (StrUtil.isBlank(dto.getToolName())) {
            errors.add("MCP工具英文名不能为空");
        } else if (!isValidToolName(dto.getToolName())) {
            errors.add("MCP工具英文名只能包含字母、数字、连字符和下划线，长度3-50个字符");
        }

        if (StrUtil.isBlank(dto.getToolDescription())) {
            errors.add("MCP工具描述不能为空");
        } else if (dto.getToolDescription().trim().length() < 10) {
            errors.add("MCP工具描述至少需要10个字符");
        } else if (dto.getToolDescription().trim().length() > 200) {
            errors.add("MCP工具描述不能超过200个字符");
        }

        if (StrUtil.isBlank(dto.getMcpNameDisplay())) {
            errors.add("多语言名称不能为空");
        } else if (dto.getMcpNameDisplay().trim().length() < 2) {
            errors.add("多语言名称至少需要2个字符");
        }

        if (StrUtil.isBlank(dto.getDescriptionDisplay())) {
            errors.add("多语言描述不能为空");
        } else if (dto.getDescriptionDisplay().trim().length() < 10) {
            errors.add("多语言描述至少需要10个字符");
        }

        if (StrUtil.isBlank(dto.getMcpInputSchema())) {
            errors.add("input_json_schema不能为空");
        } else if (!isValidJson(dto.getMcpInputSchema())) {
            errors.add("input_json_schema必须是有效的JSON Schema格式");
        }

        if (StrUtil.isBlank(dto.getMcpOutputSchema())) {
            errors.add("output_json_schema不能为空");
        } else if (!isValidJson(dto.getMcpOutputSchema())) {
            errors.add("output_json_schema必须是有效的JSON Schema格式");
        }

        if (StrUtil.isBlank(dto.getStreamOutput())) {
            errors.add("请选择是否流式输出");
        }

        if (StrUtil.isBlank(dto.getConvertType())) {
            errors.add("转换模板类型不能为空");
        } else if (dto.getConvertType().trim().length() < 2) {
            errors.add("转换模板类型至少需要2个字符");
        }

        if (StrUtil.isBlank(dto.getToolType())) {
            errors.add("请选择MCP工具类型");
        }
    }

    /**
     * 模板转换器数据校验
     *
     * @param dto    数据传输对象
     * @param errors 错误列表
     */
    private void validateTemplateConverterData(UnifiedToolAddDto dto, List<String> errors) {
        if (StrUtil.isBlank(dto.getTemplateReqUrl())) {
            errors.add("请求路径不能为空");
        }

        if (StrUtil.isBlank(dto.getTemplateReqMethod())) {
            errors.add("请选择请求方式");
        }

        if (StrUtil.isBlank(dto.getTemplateReqHeaders())) {
            errors.add("请求头模板不能为空");
        } else if (dto.getTemplateReqHeaders().trim().length() < 5) {
            errors.add("请求头模板内容过短");
        }

        if (StrUtil.isBlank(dto.getReqBody())) {
            errors.add("请求体模板不能为空");
        } else if (dto.getReqBody().trim().length() < 5) {
            errors.add("请求体模板内容过短");
        }

        if (StrUtil.isBlank(dto.getRespBody())) {
            errors.add("响应体模板不能为空");
        } else if (dto.getRespBody().trim().length() < 5) {
            errors.add("响应体模板内容过短");
        }
    }

    /**
     * 保存HTTP工具
     *
     * @param dto             数据传输对象
     * @param providerToolNum 提供者工具编号
     * @param currentUser     当前用户
     * @param session         HTTP会话
     */
    private void saveHttpTool(UnifiedToolAddDto dto, Long providerToolNum, String currentUser, HttpSession session) {
        OriginToolHttp httpTool = new OriginToolHttp();
        httpTool.setProviderToolNum(providerToolNum);
        httpTool.setProviderId(PermissionUtil.getCurrentProviderId(session)); // 设置providerId
        httpTool.setNameDisplay(dto.getNameDisplay());
        httpTool.setDescDisplay(dto.getDescDisplay());
        httpTool.setReqUrl(dto.getReqUrl());
        httpTool.setReqMethod(dto.getReqMethod());
        httpTool.setReqHeaders(dto.getReqHeaders());
        httpTool.setInputSchema(dto.getInputSchema());
        httpTool.setOutputSchema(dto.getOutputSchema());
        // 设置providerAppNum，处理可能的数字格式异常
        if (dto.getProviderAppNum() != null && !dto.getProviderAppNum().trim().isEmpty()) {
            try {
                httpTool.setProviderAppNum(Long.valueOf(dto.getProviderAppNum().trim()));
            } catch (NumberFormatException e) {
                log.warn("providerAppNum不是有效数字，将设置为null: {}", dto.getProviderAppNum());
                httpTool.setProviderAppNum(null);
            }
        } else {
            httpTool.setProviderAppNum(null);
        }
        httpTool.setCreateTime(LocalDateTime.now());
        httpTool.setCreateBy(currentUser);
        httpTool.setUpdateTime(LocalDateTime.now());
        httpTool.setUpdateBy(currentUser);

        originToolHttpService.save(httpTool);
        log.info("保存HTTP工具成功: {}", httpTool.getId());
    }

    /**
     * 保存MCP工具
     *
     * @param dto             数据传输对象
     * @param providerToolNum 提供者工具编号
     * @param currentUser     当前用户
     * @param session         HTTP会话
     */
    private void saveMcpTool(UnifiedToolAddDto dto, Long providerToolNum, String currentUser, HttpSession session) {
        McpTool mcpTool = new McpTool();
        mcpTool.setToolNum(providerToolNum); // 使用相同的工具编号
        mcpTool.setToolVersion(1L);
        mcpTool.setValid("1");
        mcpTool.setProviderId(PermissionUtil.getCurrentProviderId(session)); // 设置providerId
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
        mcpTool.setCreateBy(currentUser);
        mcpTool.setUpdateTime(LocalDateTime.now());
        mcpTool.setUpdateBy(currentUser);

        mcpToolService.save(mcpTool);
        log.info("保存MCP工具成功: {}", mcpTool.getId());
    }

    /**
     * 保存HTTP模板转换器
     *
     * @param dto             数据传输对象
     * @param providerToolNum 提供者工具编号
     * @param currentUser     当前用户
     * @param session         HTTP会话
     */
    private void saveHttpTemplateConverter(UnifiedToolAddDto dto, Long providerToolNum, String currentUser,
        HttpSession session) {
        HttpTemplateConverter converter = new HttpTemplateConverter();
        converter.setToolNum(providerToolNum);
        converter.setToolVersion(1L);
        converter.setProviderId(PermissionUtil.getCurrentProviderId(session)); // 设置providerId
        converter.setReqUrl(dto.getTemplateReqUrl());
        converter.setReqMethod(dto.getTemplateReqMethod());
        converter.setReqHeaders(dto.getTemplateReqHeaders());
        converter.setReqBody(dto.getReqBody());
        converter.setRespBody(dto.getRespBody());
        converter.setProviderToolNum(providerToolNum);
        converter.setCreateTime(LocalDateTime.now());
        converter.setCreateBy(currentUser);
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy(currentUser);

        httpTemplateConverterService.save(converter);
        log.info("保存HTTP模板转换器成功: {}", converter.getId());
    }

    /**
     * 验证URL格式
     *
     * @param url URL字符串
     * @return 是否有效
     */
    private boolean isValidUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return false;
        }
        return url.matches("^https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$");
    }

    /**
     * 验证JSON格式
     *
     * @param json JSON字符串
     * @return 是否有效
     */
    private boolean isValidJson(String json) {
        if (StrUtil.isBlank(json)) {
            return false;
        }
        try {
            JSONUtil.parse(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证工具名称格式
     *
     * @param toolName 工具名称
     * @return 是否有效
     */
    private boolean isValidToolName(String toolName) {
        if (StrUtil.isBlank(toolName)) {
            return false;
        }
        return toolName.matches("^[a-zA-Z0-9_-]{3,50}$");
    }

    /**
     * 验证版本号格式
     *
     * @param version 版本号
     * @return 是否有效
     */
    private boolean isValidVersion(String version) {
        if (StrUtil.isBlank(version)) {
            return false;
        }
        return version.matches("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?$");
    }
}