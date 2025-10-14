package cn.com.wind.mcp.registry.service.impl;

import java.util.regex.Pattern;

import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.mapper.McpToolMapper;
import cn.com.wind.mcp.registry.mapper.OriginToolHttpMapper;
import cn.com.wind.mcp.registry.service.ToolValidationService;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 工具验证服务实现类
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Slf4j
@Service
public class ToolValidationServiceImpl implements ToolValidationService {

    // 工具名称规范：只允许字母、数字、连字符和下划线，长度3-50
    private static final Pattern TOOL_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,50}$");
    // 版本号规范：语义化版本 (Semantic Versioning)
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?$");
    @Autowired
    public McpToolMapper mcpToolMapper;
    @Autowired
    public OriginToolHttpMapper originToolHttpMapper;

    @Override
    public ValidationResult validateMcpTool(McpTool tool) {
        // 基本字段验证
        if (StrUtil.isBlank(tool.getToolName())) {
            return new ValidationResult(false, "工具名称不能为空");
        }

        if (!validateToolName(tool.getToolName())) {
            return new ValidationResult(false, "工具名称格式不正确，只允许字母、数字、连字符和下划线，长度3-50字符");
        }

        if (StrUtil.isBlank(tool.getToolType())) {
            return new ValidationResult(false, "工具类型不能为空");
        }

        if (StrUtil.isBlank(tool.getToolDescription())) {
            return new ValidationResult(false, "工具描述不能为空");
        }

        if (tool.getToolDescription().length() < 10) {
            return new ValidationResult(false, "工具描述至少需要10个字符");
        }

        if (tool.getToolDescription().length() > 2000) {
            return new ValidationResult(false, "工具描述不能超过2000个字符");
        }

        // 版本验证
        String version = tool.getToolVersion() == null ? "1" : tool.getToolVersion().toString();
        if (tool.getToolVersion() == null) {
            tool.setToolVersion(1L);
        }

        // 检查唯一性 (toolName + toolVersion)
        QueryWrapper<McpTool> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tool_name", tool.getToolName())
            .eq("tool_version", tool.getToolVersion());
        if (tool.getId() != null) {
            queryWrapper.ne("id", tool.getId());
        }

        McpTool existingTool = mcpToolMapper.selectOne(queryWrapper);
        if (existingTool != null) {
            return new ValidationResult(false, "工具名称和版本组合已存在，请修改名称或版本号");
        }

        // 生成唯一标识
        String uniqueId = generateUniqueIdentifier(tool.getToolName(), version, tool.getToolType());

        return new ValidationResult(true, "验证通过", uniqueId);
    }

    @Override
    public ValidationResult validateHttpTool(OriginToolHttp tool) {
        // 基本字段验证
        if (StrUtil.isBlank(tool.getName())) {
            return new ValidationResult(false, "工具名称不能为空");
        }

        if (!validateToolName(tool.getName())) {
            return new ValidationResult(false, "工具名称格式不正确，只允许字母、数字、连字符和下划线，长度3-50字符");
        }

        if (StrUtil.isBlank(tool.getType())) {
            return new ValidationResult(false, "工具类型不能为空");
        }

        if (StrUtil.isBlank(tool.getDescription())) {
            return new ValidationResult(false, "工具描述不能为空");
        }

        if (tool.getDescription().length() < 10) {
            return new ValidationResult(false, "工具描述至少需要10个字符");
        }

        if (tool.getDescription().length() > 2000) {
            return new ValidationResult(false, "工具描述不能超过2000个字符");
        }

        // HTTP特有验证
        if (StrUtil.isBlank(tool.getMethod())) {
            return new ValidationResult(false, "HTTP方法不能为空");
        }

        if (StrUtil.isBlank(tool.getUrl())) {
            return new ValidationResult(false, "请求URL不能为空");
        }

        // URL格式验证
        if (!isValidUrl(tool.getUrl())) {
            return new ValidationResult(false, "URL格式不正确");
        }

        // 检查唯一性 (name + method + url)
        QueryWrapper<OriginToolHttp> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", tool.getName())
            .eq("method", tool.getMethod())
            .eq("url", tool.getUrl());
        if (tool.getId() != null) {
            queryWrapper.ne("id", tool.getId());
        }

        OriginToolHttp existingTool = originToolHttpMapper.selectOne(queryWrapper);
        if (existingTool != null) {
            return new ValidationResult(false, "工具名称、HTTP方法和URL组合已存在");
        }

        // 生成唯一标识
        String uniqueId = generateUniqueIdentifier(tool.getName(), "1.0.0", tool.getType());

        return new ValidationResult(true, "验证通过", uniqueId);
    }

    @Override
    public String generateUniqueIdentifier(String toolName, String version, String toolType) {
        String content = toolName + ":" + version + ":" + toolType;
        return DigestUtil.sha256Hex(content).substring(0, 16).toUpperCase();
    }

    @Override
    public boolean validateToolName(String name) {
        if (StrUtil.isBlank(name)) {
            return false;
        }
        return TOOL_NAME_PATTERN.matcher(name).matches();
    }

    @Override
    public boolean validateVersion(String version) {
        if (StrUtil.isBlank(version)) {
            return false;
        }
        return VERSION_PATTERN.matcher(version).matches();
    }

    /**
     * 验证URL格式
     */
    private boolean isValidUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return false;
        }
        try {
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }
}