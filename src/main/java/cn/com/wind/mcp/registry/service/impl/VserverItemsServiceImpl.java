package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.VserverItems;
import cn.com.wind.mcp.registry.mapper.VserverItemsMapper;
import cn.com.wind.mcp.registry.service.VserverItemsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 虚拟服务器项目Service实现类
 *
 * @author system
 * @date 2025-01-21
 */
@Slf4j
@Service
public class VserverItemsServiceImpl extends ServiceImpl<VserverItemsMapper, VserverItems>
        implements VserverItemsService {

    /**
     * 确保vserver_items记录存在
     * 如果不存在则自动创建
     *
     * @param vserverId   虚拟服务器ID
     * @param mcpItemNum  MCP项目编号
     * @param mcpItemType MCP项目类型
     * @param createBy    创建人
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ensureVserverItemExists(String vserverId, Long mcpItemNum, String mcpItemType, String createBy) {
        log.info("检查vserver_items记录是否存在: vserverId={}, mcpItemNum={}, mcpItemType={}",
                vserverId, mcpItemNum, mcpItemType);

        // 查询是否已存在
        QueryWrapper<VserverItems> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vserver_id", vserverId)
                .eq("mcp_item_num", mcpItemNum)
                .eq("mcp_item_type", mcpItemType);

        VserverItems existing = this.getOne(queryWrapper);

        if (existing == null) {
            // 不存在,创建新记录
            log.info("vserver_items记录不存在,创建新记录");

            VserverItems newItem = new VserverItems();
            newItem.setVserverId(vserverId);
            newItem.setMcpItemNum(mcpItemNum);
            newItem.setMcpItemType(mcpItemType);
            newItem.setStatus("1"); // 默认启用
            newItem.setOrderNum(1); // 默认排序号
            newItem.setCreateTime(LocalDateTime.now());
            newItem.setCreateBy(createBy);
            newItem.setUpdateTime(LocalDateTime.now());
            newItem.setUpdateBy(createBy);

            boolean success = this.save(newItem);
            if (success) {
                log.info("vserver_items记录创建成功: id={}", newItem.getId());
            } else {
                log.error("vserver_items记录创建失败");
                throw new RuntimeException("创建vserver_items记录失败");
            }
        } else {
            log.info("vserver_items记录已存在: id={}", existing.getId());
        }
    }
}
