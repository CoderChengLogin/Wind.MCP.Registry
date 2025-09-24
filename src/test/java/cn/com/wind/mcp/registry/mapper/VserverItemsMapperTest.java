package cn.com.wind.mcp.registry.mapper;

import java.util.Date;

import cn.com.wind.mcp.registry.entity.VserverItems;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * VserverItems Mapper 测试
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
public class VserverItemsMapperTest {

    @Autowired
    private VserverItemsMapper vserverItemsMapper;

    @Test
    void testInsertVserverItems() {
        VserverItems item = new VserverItems();
        item.setVserverId("vserver_001");
        item.setMcpItemNum(12345L);
        item.setMcpItemType("1");
        item.setStatus("1");
        item.setOrderNum(1);
        item.setCreateTime(new Date());
        item.setCreateBy("system");
        item.setUpdateTime(new Date());
        item.setUpdateBy("system");

        int result = vserverItemsMapper.insert(item);
        assertEquals(1, result);
        assertNotNull(item.getId());
    }

    @Test
    void testSelectById() {
        // 先插入一个虚拟服务器条目
        VserverItems item = new VserverItems();
        item.setVserverId("vserver_select");
        item.setMcpItemNum(54321L);
        item.setMcpItemType("2");
        item.setStatus("1");
        item.setOrderNum(2);
        item.setCreateTime(new Date());
        item.setCreateBy("system");
        item.setUpdateTime(new Date());
        item.setUpdateBy("system");

        vserverItemsMapper.insert(item);
        Long id = item.getId();

        // 查询
        VserverItems selected = vserverItemsMapper.selectById(id);
        assertNotNull(selected);
        assertEquals("vserver_select", selected.getVserverId());
        assertEquals(54321L, selected.getMcpItemNum());
        assertEquals("2", selected.getMcpItemType());
        assertEquals("1", selected.getStatus());
        assertEquals(2, selected.getOrderNum());
    }

    @Test
    void testUpdateById() {
        // 先插入一个虚拟服务器条目
        VserverItems item = new VserverItems();
        item.setVserverId("vserver_update");
        item.setMcpItemNum(98765L);
        item.setMcpItemType("1");
        item.setStatus("1");
        item.setOrderNum(3);
        item.setCreateTime(new Date());
        item.setCreateBy("system");
        item.setUpdateTime(new Date());
        item.setUpdateBy("system");

        vserverItemsMapper.insert(item);
        Long id = item.getId();

        // 更新
        item.setMcpItemType("3");
        item.setStatus("0");
        item.setOrderNum(5);
        item.setUpdateTime(new Date());
        item.setUpdateBy("updater");

        int result = vserverItemsMapper.updateById(item);
        assertEquals(1, result);

        // 验证更新
        VserverItems updated = vserverItemsMapper.selectById(id);
        assertEquals("3", updated.getMcpItemType());
        assertEquals("0", updated.getStatus());
        assertEquals(5, updated.getOrderNum());
        assertEquals("updater", updated.getUpdateBy());
    }

    @Test
    void testDeleteById() {
        // 先插入一个虚拟服务器条目
        VserverItems item = new VserverItems();
        item.setVserverId("vserver_delete");
        item.setMcpItemNum(11111L);
        item.setMcpItemType("1");
        item.setStatus("1");
        item.setOrderNum(4);
        item.setCreateTime(new Date());
        item.setCreateBy("system");
        item.setUpdateTime(new Date());
        item.setUpdateBy("system");

        vserverItemsMapper.insert(item);
        Long id = item.getId();

        // 删除
        int result = vserverItemsMapper.deleteById(id);
        assertEquals(1, result);

        // 验证删除
        VserverItems deleted = vserverItemsMapper.selectById(id);
        assertNull(deleted);
    }
}