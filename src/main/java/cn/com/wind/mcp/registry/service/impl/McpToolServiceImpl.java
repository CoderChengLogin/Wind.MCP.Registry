package cn.com.wind.mcp.registry.service.impl;

import java.util.List;

import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.mapper.McpToolMapper;
import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.service.ToolValidationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * MCP工具Service实现类
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Slf4j
@Service
public class McpToolServiceImpl extends ServiceImpl<McpToolMapper, McpTool> implements McpToolService {

    @Autowired
    private ToolValidationService toolValidationService;

    @Override
    public List<McpTool> searchTools(String keyword) {
        QueryWrapper<McpTool> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("name", keyword)
                .or()
                .like("description", keyword)
                .or()
                .like("tags", keyword);
        }

        return list(queryWrapper);
    }

    @Override
    public boolean saveOrUpdateWithValidation(McpTool tool) {
        // 验证工具信息
        ToolValidationService.ValidationResult result = toolValidationService.validateMcpTool(tool);
        if (!result.isValid()) {
            log.warn("MCP工具验证失败: {}", result.getMessage());
            throw new RuntimeException(result.getMessage());
        }

        // 设置唯一标识和其他元数据
        if (result.getUniqueId() != null) {
            // 可以将唯一标识存储在配置字段中
            String config = tool.getConfiguration();
            if (config == null) {
                config = "{\"uniqueId\":\"" + result.getUniqueId() + "\"}";
            } else if (!config.contains("uniqueId")) {
                // 简单地在JSON中添加uniqueId字段
                config = config.replace("}", ",\"uniqueId\":\"" + result.getUniqueId() + "\"}");
                if (config.startsWith(",")) {
                    config = "{" + config.substring(1);
                }
            }
            tool.setConfiguration(config);
        }

        // 执行保存或更新
        return saveOrUpdate(tool);
    }

    @Override
    public McpTool findByUniqueId(String uniqueId) {
        QueryWrapper<McpTool> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("configuration", "\"uniqueId\":\"" + uniqueId + "\"");
        return getOne(queryWrapper);
    }
}