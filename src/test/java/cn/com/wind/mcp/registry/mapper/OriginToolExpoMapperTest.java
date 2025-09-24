package cn.com.wind.mcp.registry.mapper;

import java.time.LocalDateTime;

import cn.com.wind.mcp.registry.entity.OriginToolExpo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * OriginToolExpo Mapper 测试
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
public class OriginToolExpoMapperTest {

    @Autowired
    private OriginToolExpoMapper originToolExpoMapper;

    @Test
    void testInsertOriginToolExpo() {
        OriginToolExpo tool = new OriginToolExpo();
        tool.setProviderToolNum(12345L);
        tool.setProviderToolName("Test Expo Tool");
        tool.setNameDisplay("Test Expo Display");
        tool.setDescDisplay("This is a test expo tool");
        tool.setAppClass(1);
        tool.setCommandId(100);
        tool.setFunctionName("testFunction");
        tool.setExpoApiDefine("<api>test</api>");
        tool.setProviderAppNum(999L);
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        int result = originToolExpoMapper.insert(tool);
        assertEquals(1, result);
        assertNotNull(tool.getId());
    }

    @Test
    void testSelectById() {
        // 先插入一个Expo工具
        OriginToolExpo tool = new OriginToolExpo();
        tool.setProviderToolNum(54321L);
        tool.setProviderToolName("Test Select Tool");
        tool.setNameDisplay("Test Select Display");
        tool.setDescDisplay("Test select expo tool");
        tool.setAppClass(2);
        tool.setCommandId(200);
        tool.setFunctionName("selectFunction");
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        originToolExpoMapper.insert(tool);
        Long id = tool.getId();

        // 查询
        OriginToolExpo selected = originToolExpoMapper.selectById(id);
        assertNotNull(selected);
        assertEquals(54321L, selected.getProviderToolNum());
        assertEquals("Test Select Tool", selected.getProviderToolName());
        assertEquals("Test Select Display", selected.getNameDisplay());
        assertEquals("Test select expo tool", selected.getDescDisplay());
        assertEquals(2, selected.getAppClass());
        assertEquals(200, selected.getCommandId());
        assertEquals("selectFunction", selected.getFunctionName());
    }

    @Test
    void testUpdateById() {
        // 先插入一个Expo工具
        OriginToolExpo tool = new OriginToolExpo();
        tool.setProviderToolNum(98765L);
        tool.setProviderToolName("Test Update Tool");
        tool.setNameDisplay("Test Update Display");
        tool.setDescDisplay("Test update expo tool");
        tool.setAppClass(1);
        tool.setCommandId(300);
        tool.setFunctionName("updateFunction");
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        originToolExpoMapper.insert(tool);
        Long id = tool.getId();

        // 更新
        tool.setProviderToolName("Updated Expo Tool");
        tool.setNameDisplay("Updated Expo Display");
        tool.setDescDisplay("Updated expo tool description");
        tool.setAppClass(3);
        tool.setCommandId(350);
        tool.setFunctionName("updatedFunction");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("updater");

        int result = originToolExpoMapper.updateById(tool);
        assertEquals(1, result);

        // 验证更新
        OriginToolExpo updated = originToolExpoMapper.selectById(id);
        assertEquals("Updated Expo Tool", updated.getProviderToolName());
        assertEquals("Updated Expo Display", updated.getNameDisplay());
        assertEquals("Updated expo tool description", updated.getDescDisplay());
        assertEquals(3, updated.getAppClass());
        assertEquals(350, updated.getCommandId());
        assertEquals("updatedFunction", updated.getFunctionName());
        assertEquals("updater", updated.getUpdateBy());
    }

    @Test
    void testDeleteById() {
        // 先插入一个Expo工具
        OriginToolExpo tool = new OriginToolExpo();
        tool.setProviderToolNum(11111L);
        tool.setProviderToolName("Test Delete Tool");
        tool.setNameDisplay("Test Delete Display");
        tool.setDescDisplay("Test delete expo tool");
        tool.setAppClass(1);
        tool.setCommandId(400);
        tool.setFunctionName("deleteFunction");
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        originToolExpoMapper.insert(tool);
        Long id = tool.getId();

        // 删除
        int result = originToolExpoMapper.deleteById(id);
        assertEquals(1, result);

        // 验证删除
        OriginToolExpo deleted = originToolExpoMapper.selectById(id);
        assertNull(deleted);
    }
}