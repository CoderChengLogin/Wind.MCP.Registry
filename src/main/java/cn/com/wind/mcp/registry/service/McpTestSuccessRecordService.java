package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.entity.McpTestSuccessRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * MCP工具测试成功记录Service接口
 *
 * @author system
 * @date 2025-10-20
 */
public interface McpTestSuccessRecordService extends IService<McpTestSuccessRecord> {

    /**
     * 保存测试成功记录
     * 包含完整的工具快照和测试参数
     *
     * @param toolId           MCP工具ID
     * @param testParameters   测试参数(JSON格式)
     * @param testResult       测试结果(JSON格式)
     * @param operatorId       操作者ID
     * @param operatorUsername 操作者用户名
     * @return 保存是否成功
     */
    boolean saveTestRecord(Long toolId, String testParameters, String testResult,
                           Long operatorId, String operatorUsername);
}
