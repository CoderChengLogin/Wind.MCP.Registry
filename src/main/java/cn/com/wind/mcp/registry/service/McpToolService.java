package cn.com.wind.mcp.registry.service;

import java.util.List;

import cn.com.wind.mcp.registry.entity.McpTool;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * MCP工具Service接口
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
public interface McpToolService extends IService<McpTool> {

    /**
     * 搜索工具
     *
     * @param keyword 关键词
     * @return 工具列表
     */
    List<McpTool> searchTools(String keyword);

    /**
     * 保存或更新工具（带验证）
     *
     * @param tool 工具信息
     * @return 保存结果
     */
    boolean saveOrUpdateWithValidation(McpTool tool);

    /**
     * 根据唯一标识查找工具
     *
     * @param uniqueId 唯一标识
     * @return 工具信息
     */
    McpTool findByUniqueId(String uniqueId);

    /**
     * 统计指定用户创建的工具数量
     *
     * @param createBy 创建人
     * @return 工具数量
     */
    long countByCreateBy(String createBy);

    /**
     * 统计指定提供者的工具数量
     *
     * @param providerId 提供者ID
     * @return 工具数量
     */
    long countByProviderId(Long providerId);
}
