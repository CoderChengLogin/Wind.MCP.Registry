package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.entity.VserverItems;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 虚拟服务器项目Service接口
 *
 * @author system
 * @date 2025-01-21
 */
public interface VserverItemsService extends IService<VserverItems> {

    /**
     * 确保vserver_items记录存在
     * 如果不存在则自动创建
     *
     * @param vserverId   虚拟服务器ID
     * @param mcpItemNum  MCP项目编号
     * @param mcpItemType MCP项目类型
     * @param createBy    创建人
     */
    void ensureVserverItemExists(String vserverId, Long mcpItemNum, String mcpItemType, String createBy);
}
