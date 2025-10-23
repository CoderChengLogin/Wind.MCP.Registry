package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.dto.McpToolExportDto;
import cn.com.wind.mcp.registry.dto.McpToolImportValidationResult;
import cn.com.wind.mcp.registry.dto.mcptool.McpToolDTO;
import cn.com.wind.mcp.registry.entity.*;
import cn.com.wind.mcp.registry.mapper.*;
import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.service.ToolValidationService;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private McpToolMapper mcpToolMapper;
    @Autowired
    private OriginToolHttpMapper originToolHttpMapper;
    @Autowired
    private OriginToolExpoMapper originToolExpoMapper;
    @Autowired
    private HttpTemplateConverterMapper httpTemplateConverterMapper;
    @Autowired
    private ExpoTemplateConverterMapper expoTemplateConverterMapper;

    @Override
    public List<McpTool> searchTools(String keyword) {
        QueryWrapper<McpTool> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("tool_name", keyword)
                    .or()
                    .like("tool_description", keyword)
                    .or()
                    .like("name_display", keyword);
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
            // 可以将唯一标识存储在输入schema字段中作为元数据
            String schema = tool.getInputSchema();
            if (schema == null) {
                schema = "{\"uniqueId\":\"" + result.getUniqueId() + "\"}";
            } else if (!schema.contains("uniqueId")) {
                // 简单地在JSON中添加uniqueId字段
                schema = schema.replace("}", ",\"uniqueId\":\"" + result.getUniqueId() + "\"}");
                if (schema.startsWith(",")) {
                    schema = "{" + schema.substring(1);
                }
            }
            tool.setInputSchema(schema);
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

    /**
     * 统计指定用户创建的工具数量
     *
     * @param createBy 创建人
     * @return 工具数量
     */
    @Override
    public long countByCreateBy(String createBy) {
        return count(new QueryWrapper<McpTool>().eq("create_by", createBy));
    }

    /**
     * 统计指定提供者的工具数量
     *
     * @param providerId 提供者ID
     * @return 工具数量
     */
    @Override
    public long countByProviderId(Long providerId) {
        return count(new QueryWrapper<McpTool>().eq("provider_id", providerId));
    }

    @Override
    public McpToolDTO getMcpToolById(Long id) {
        McpTool mcpTool = mcpToolMapper.selectById(id);
        if (mcpTool == null) {
            throw new RuntimeException("找不到ID为" + id + "的MCP工具");
        }
        return convertToDTO(mcpTool);
    }

    @Override
    public List<McpToolDTO> getMcpToolsByNumValid(Long toolNum, String valid) {
        List<McpTool> entities = mcpToolMapper.selectList(
                new QueryWrapper<McpTool>()
                        .eq("tool_num", toolNum)
                        .eq("valid", valid)
        );
        return entities.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<McpToolDTO> getAllMcpTools() {
        List<McpTool> entities = mcpToolMapper.selectList(new QueryWrapper<>());
        return entities.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 将实体转换为DTO
     */
    private McpToolDTO convertToDTO(McpTool mcpTool) {
        McpToolDTO dto = new McpToolDTO();
        BeanUtils.copyProperties(mcpTool, dto);
        return dto;
    }

    /**
     * 验证导入的工具数据
     *
     * @param exportDto         导入的数据
     * @param currentProviderId 当前登录用户的提供者ID
     * @return 校验结果
     */
    @Override
    public McpToolImportValidationResult validateImportData(McpToolExportDto exportDto, Long currentProviderId) {
        McpToolImportValidationResult result = new McpToolImportValidationResult();
        result.setValid(true);

        // 1. 基础数据验证
        if (exportDto == null) {
            return McpToolImportValidationResult.failure("导入数据不能为空");
        }

        McpTool mcpTool = exportDto.getMcpTool();
        if (mcpTool == null) {
            return McpToolImportValidationResult.failure("MCP工具信息不能为空");
        }

        // 2. 核心校验: MCP工具英文名验证
        if (StrUtil.isBlank(mcpTool.getToolName())) {
            result.addError("MCP工具英文名不能为空");
        } else {
            // 验证格式: 只能包含字母、数字、下划线
            if (!mcpTool.getToolName().matches("^[a-zA-Z0-9_]+$")) {
                result.addError("MCP工具英文名格式不正确,只能包含字母、数字、下划线");
            }

            // 验证重复性: 检查工具英文名是否已存在
            QueryWrapper<McpTool> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("tool_name", mcpTool.getToolName());
            long count = count(queryWrapper);
            if (count > 0) {
                result.addError("导入失败:MCP工具英文名'" + mcpTool.getToolName() + "'已存在,请修改后重新导入");
            }
        }

        // 3. convertType验证
        String convertType = mcpTool.getConvertType();
        if (StrUtil.isNotBlank(convertType)) {
            // convertType数据库存储为: '1'=http, '2'=expo, '3'=code
            // 但前端/DTO可能传入: "http", "expo", "code" 或 "1", "2", "3"
            boolean isValidConvertType = "1".equals(convertType) || "2".equals(convertType) || "3".equals(convertType)
                    || "http".equalsIgnoreCase(convertType) || "expo".equalsIgnoreCase(convertType)
                    || "code".equalsIgnoreCase(convertType);
            if (!isValidConvertType) {
                result.addError("转换类型(convertType)无效,必须是: 'http'/'1', 'expo'/'2', 'code'/'3' 之一");
            }
        } else {
            result.addError("转换类型(convertType)不能为空");
        }

        // 4. JSON Schema格式验证
        if (StrUtil.isNotBlank(mcpTool.getInputSchema())) {
            if (!isValidJson(mcpTool.getInputSchema())) {
                result.addError("输入Schema(inputSchema)不是有效的JSON格式");
            }
        }
        if (StrUtil.isNotBlank(mcpTool.getOutputSchema())) {
            if (!isValidJson(mcpTool.getOutputSchema())) {
                result.addError("输出Schema(outputSchema)不是有效的JSON格式");
            }
        }

        // 5. 关联数据完整性检查
        if (convertType != null) {
            boolean isHttp = "1".equals(convertType) || "http".equalsIgnoreCase(convertType);
            boolean isExpo = "2".equals(convertType) || "expo".equalsIgnoreCase(convertType);

            if (isHttp) {
                // HTTP工具必须有转换模板
                if (exportDto.getHttpTemplateConverter() == null) {
                    result.addError("HTTP类型工具缺少关联的HTTP转换模板信息");
                }
                // 原始HTTP工具为可选,但如果缺失给出警告
                if (exportDto.getOriginToolHttp() == null) {
                    result.addWarning("HTTP类型工具缺少原始HTTP接口信息(可选,但建议提供)");
                }
            } else if (isExpo) {
                // Expo工具必须有转换模板
                if (exportDto.getExpoTemplateConverter() == null) {
                    result.addError("Expo类型工具缺少关联的Expo转换模板信息");
                }
                // 原始Expo工具为可选,但如果缺失给出警告
                if (exportDto.getOriginToolExpo() == null) {
                    result.addWarning("Expo类型工具缺少原始Expo接口信息(可选,但建议提供)");
                }
            }
        }

        log.info("导入数据校验完成: valid={}, errors={}, warnings={}",
                result.isValid(), result.getErrors(), result.getWarnings());

        return result;
    }

    /**
     * 导入工具数据
     * 使用事务确保数据一致性
     *
     * @param exportDto         导入的数据
     * @param currentProviderId 当前登录用户的提供者ID
     * @param username          当前登录用户的用户名
     * @return 导入后的MCP工具
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public McpTool importTool(McpToolExportDto exportDto, Long currentProviderId, String username) {
        log.info("开始导入工具, toolName={}, providerId={}", exportDto.getMcpTool().getToolName(), currentProviderId);

        // 1. 准备MCP工具数据
        McpTool mcpTool = exportDto.getMcpTool();

        // 清空ID,让数据库自动生成新ID
        mcpTool.setId(null);

        // 生成新的工具编号 (使用时间戳)
        Long newToolNum = System.currentTimeMillis();
        mcpTool.setToolNum(newToolNum);

        // 设置提供者ID和创建/更新信息
        mcpTool.setProviderId(currentProviderId);
        mcpTool.setCreateBy(username);
        mcpTool.setUpdateBy(username);
        mcpTool.setCreateTime(LocalDateTime.now());
        mcpTool.setUpdateTime(LocalDateTime.now());

        // 如果toolVersion为空,设置为1
        if (mcpTool.getToolVersion() == null) {
            mcpTool.setToolVersion(1L);
        }

        // 如果valid为空,设置为'1'(有效)
        if (StrUtil.isBlank(mcpTool.getValid())) {
            mcpTool.setValid("1");
        }

        // 保存MCP工具
        boolean mcpSaved = save(mcpTool);
        if (!mcpSaved) {
            throw new RuntimeException("保存MCP工具失败");
        }

        log.info("MCP工具保存成功, id={}, toolNum={}", mcpTool.getId(), mcpTool.getToolNum());

        // 获取convertType并标准化判断
        String convertType = mcpTool.getConvertType();
        boolean isHttp = "1".equals(convertType) || "http".equalsIgnoreCase(convertType);
        boolean isExpo = "2".equals(convertType) || "expo".equalsIgnoreCase(convertType);

        log.info("根据convertType={} 严格导入相关数据, isHttp={}, isExpo={}", convertType, isHttp, isExpo);

        // 2. 保存原始HTTP接口 (仅当convertType为HTTP且数据存在时)
        if (isHttp && exportDto.getOriginToolHttp() != null) {
            OriginToolHttp httpTool = exportDto.getOriginToolHttp();
            httpTool.setId(null); // 清空ID
            httpTool.setProviderToolNum(newToolNum); // 使用新的toolNum
            httpTool.setProviderId(currentProviderId);
            httpTool.setCreateBy(username);
            httpTool.setUpdateBy(username);
            httpTool.setCreateTime(LocalDateTime.now());
            httpTool.setUpdateTime(LocalDateTime.now());

            int httpInserted = originToolHttpMapper.insert(httpTool);
            log.info("原始HTTP接口保存结果: {}", httpInserted > 0 ? "成功" : "失败");
        } else if (exportDto.getOriginToolHttp() != null) {
            log.info("忽略原始HTTP接口数据 (convertType={} 不匹配)", convertType);
        }

        // 3. 保存原始Expo接口 (仅当convertType为Expo且数据存在时)
        if (isExpo && exportDto.getOriginToolExpo() != null) {
            OriginToolExpo expoTool = exportDto.getOriginToolExpo();
            expoTool.setId(null); // 清空ID
            expoTool.setProviderToolNum(newToolNum); // 使用新的toolNum
            expoTool.setProviderId(currentProviderId);
            expoTool.setCreateBy(username);
            expoTool.setUpdateBy(username);
            expoTool.setCreateTime(LocalDateTime.now());
            expoTool.setUpdateTime(LocalDateTime.now());

            int expoInserted = originToolExpoMapper.insert(expoTool);
            log.info("原始Expo接口保存结果: {}", expoInserted > 0 ? "成功" : "失败");
        } else if (exportDto.getOriginToolExpo() != null) {
            log.info("忽略原始Expo接口数据 (convertType={} 不匹配)", convertType);
        }

        // 4. 保存HTTP转换模板 (仅当convertType为HTTP且数据存在时)
        if (isHttp && exportDto.getHttpTemplateConverter() != null) {
            HttpTemplateConverter httpConverter = exportDto.getHttpTemplateConverter();
            httpConverter.setId(null); // 清空ID
            httpConverter.setToolNum(newToolNum); // 使用新的toolNum
            httpConverter.setToolVersion(mcpTool.getToolVersion());
            httpConverter.setProviderToolNum(newToolNum);
            httpConverter.setProviderId(currentProviderId);
            httpConverter.setCreateBy(username);
            httpConverter.setUpdateBy(username);
            httpConverter.setCreateTime(LocalDateTime.now());
            httpConverter.setUpdateTime(LocalDateTime.now());

            int httpConverterInserted = httpTemplateConverterMapper.insert(httpConverter);
            log.info("HTTP转换模板保存结果: {}", httpConverterInserted > 0 ? "成功" : "失败");
        } else if (exportDto.getHttpTemplateConverter() != null) {
            log.info("忽略HTTP转换模板数据 (convertType={} 不匹配)", convertType);
        }

        // 5. 保存Expo转换模板 (仅当convertType为Expo且数据存在时)
        if (isExpo && exportDto.getExpoTemplateConverter() != null) {
            ExpoTemplateConverter expoConverter = exportDto.getExpoTemplateConverter();
            expoConverter.setId(null); // 清空ID
            expoConverter.setToolNum(newToolNum); // 使用新的toolNum
            expoConverter.setToolVersion(mcpTool.getToolVersion());
            expoConverter.setProviderToolNum(newToolNum);
            expoConverter.setCreateBy(username);
            expoConverter.setUpdateBy(username);
            expoConverter.setCreateTime(LocalDateTime.now());
            expoConverter.setUpdateTime(LocalDateTime.now());

            int expoConverterInserted = expoTemplateConverterMapper.insert(expoConverter);
            log.info("Expo转换模板保存结果: {}", expoConverterInserted > 0 ? "成功" : "失败");
        } else if (exportDto.getExpoTemplateConverter() != null) {
            log.info("忽略Expo转换模板数据 (convertType={} 不匹配)", convertType);
        }

        log.info("工具导入完成, toolName={}, toolNum={}", mcpTool.getToolName(), newToolNum);

        return mcpTool;
    }

    /**
     * 验证字符串是否为有效的JSON格式
     *
     * @param jsonStr JSON字符串
     * @return true=有效, false=无效
     */
    private boolean isValidJson(String jsonStr) {
        if (StrUtil.isBlank(jsonStr)) {
            return false;
        }
        try {
            JSONUtil.parse(jsonStr);
            return true;
        } catch (Exception e) {
            log.debug("JSON格式验证失败: {}", e.getMessage());
            return false;
        }
    }
}