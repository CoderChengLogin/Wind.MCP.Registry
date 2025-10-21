package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.*;
import cn.com.wind.mcp.registry.mapper.McpTestSuccessRecordMapper;
import cn.com.wind.mcp.registry.service.*;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MCP工具测试成功记录Service实现类
 *
 * @author system
 * @date 2025-10-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpTestSuccessRecordServiceImpl
        extends ServiceImpl<McpTestSuccessRecordMapper, McpTestSuccessRecord>
        implements McpTestSuccessRecordService {

    private final McpToolService mcpToolService;
    private final HttpTemplateConverterService httpTemplateConverterService;
    private final ExpoTemplateConverterService expoTemplateConverterService;
    private final OriginToolHttpService originToolHttpService;
    private final OriginToolExpoService originToolExpoService;

    /**
     * 保存测试成功记录
     * 采用事务处理确保数据完整性
     *
     * @param toolId           MCP工具ID
     * @param testParameters   测试参数(JSON格式)
     * @param testResult       测试结果(JSON格式)
     * @param operatorId       操作者ID
     * @param operatorUsername 操作者用户名
     * @return 保存是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveTestRecord(Long toolId, String testParameters, String testResult,
                                  Long operatorId, String operatorUsername) {
        try {
            log.info("开始保存测试成功记录: toolId={}, operatorId={}, operatorUsername={}",
                    toolId, operatorId, operatorUsername);

            // 1. 获取MCP工具信息
            McpTool mcpTool = mcpToolService.getById(toolId);
            if (mcpTool == null) {
                log.error("MCP工具不存在: toolId={}", toolId);
                return false;
            }

            // 2. 构建工具快照(包含完整信息)
            Map<String, Object> toolSnapshot = buildToolSnapshot(mcpTool);

            // 3. 解析测试结果生成摘要
            String resultSummary = generateResultSummary(testResult);

            // 4. 创建测试记录
            McpTestSuccessRecord record = new McpTestSuccessRecord()
                    .setToolId(toolId)
                    .setToolNum(String.valueOf(mcpTool.getToolNum()))
                    .setToolName(mcpTool.getToolName())
                    .setToolVersion(String.valueOf(mcpTool.getToolVersion()))
                    .setToolSnapshot(JSONUtil.toJsonStr(toolSnapshot))
                    .setTestParameters(testParameters)
                    .setTestResult(testResult)
                    .setTestResultSummary(resultSummary)
                    .setTestTimestamp(LocalDateTime.now())
                    .setOperatorId(operatorId)
                    .setOperatorUsername(operatorUsername)
                    .setCreateTime(LocalDateTime.now())
                    .setCreateBy(operatorUsername);

            // 5. 保存记录
            boolean success = save(record);

            if (success) {
                log.info("测试成功记录保存完成: recordId={}, toolId={}, operatorUsername={}",
                        record.getId(), toolId, operatorUsername);
            } else {
                log.error("测试成功记录保存失败: toolId={}", toolId);
            }

            return success;
        } catch (Exception e) {
            log.error("保存测试成功记录时发生异常: toolId=" + toolId, e);
            throw e;
        }
    }

    /**
     * 构建工具完整快照
     * 包含MCP工具信息、转换模板、原始工具信息
     *
     * @param mcpTool MCP工具实体
     * @return 工具快照Map
     */
    private Map<String, Object> buildToolSnapshot(McpTool mcpTool) {
        Map<String, Object> snapshot = new HashMap<>();

        // MCP工具基本信息
        snapshot.put("mcpTool", mcpTool);

        // 根据转换类型添加对应的转换模板信息
        String convertType = mcpTool.getConvertType();
        if (convertType != null) {
            convertType = convertType.toLowerCase();

            // HTTP转换模板 (convertType = '1' 或 'http')
            if ("1".equals(convertType) || convertType.contains("http")) {
                HttpTemplateConverter httpConverter = httpTemplateConverterService.lambdaQuery()
                        .eq(HttpTemplateConverter::getToolNum, mcpTool.getToolNum())
                        .eq(HttpTemplateConverter::getToolVersion, mcpTool.getToolVersion())
                        .one();
                if (httpConverter != null) {
                    snapshot.put("httpConverter", httpConverter);
                    log.debug("添加HTTP转换模板快照: toolNum={}", mcpTool.getToolNum());

                    // 原始HTTP工具信息
                    if (httpConverter.getProviderToolNum() != null) {
                        OriginToolHttp originHttp = originToolHttpService.lambdaQuery()
                                .eq(OriginToolHttp::getProviderToolNum, httpConverter.getProviderToolNum())
                                .one();
                        if (originHttp != null) {
                            snapshot.put("originHttp", originHttp);
                            log.debug("添加原始HTTP工具快照: providerToolNum={}", httpConverter.getProviderToolNum());
                        }
                    }
                }
            }
            // Expo转换模板 (convertType = '2' 或 'expo')
            else if ("2".equals(convertType) || "expo".equals(convertType)) {
                ExpoTemplateConverter expoConverter = expoTemplateConverterService.lambdaQuery()
                        .eq(ExpoTemplateConverter::getToolNum, mcpTool.getToolNum())
                        .eq(ExpoTemplateConverter::getToolVersion, mcpTool.getToolVersion())
                        .one();
                if (expoConverter != null) {
                    snapshot.put("expoConverter", expoConverter);
                    log.debug("添加Expo转换模板快照: toolNum={}", mcpTool.getToolNum());

                    // 原始Expo工具信息
                    if (expoConverter.getProviderToolNum() != null) {
                        OriginToolExpo originExpo = originToolExpoService.lambdaQuery()
                                .eq(OriginToolExpo::getProviderToolNum, expoConverter.getProviderToolNum())
                                .one();
                        if (originExpo != null) {
                            snapshot.put("originExpo", originExpo);
                            log.debug("添加原始Expo工具快照: providerToolNum={}", expoConverter.getProviderToolNum());
                        }
                    }
                }
            }
            // Manual/Code类型 (convertType = '3', 'manual' 或 'code') - 无需转换模板
            else if ("3".equals(convertType) || "manual".equals(convertType) || "code".equals(convertType)) {
                log.debug("手动转换模板类型,无需额外快照: convertType={}", mcpTool.getConvertType());
            }
        }

        return snapshot;
    }

    /**
     * 生成测试结果摘要
     *
     * @param testResult 测试结果JSON字符串
     * @return 结果摘要
     */
    private String generateResultSummary(String testResult) {
        try {
            Map<String, Object> resultMap = JSONUtil.toBean(testResult, Map.class);

            // 从结果中提取关键信息
            Integer errorCode = (Integer) resultMap.get("mcp_tool_error_code");
            String errorMsg = (String) resultMap.get("mcp_tool_error_msg");

            if (errorCode != null && errorCode == 0) {
                return "测试成功";
            } else {
                return "测试成功 (警告: " + (errorMsg != null ? errorMsg : "未知错误") + ")";
            }
        } catch (Exception e) {
            log.warn("生成测试结果摘要失败", e);
            return "测试成功";
        }
    }
}
