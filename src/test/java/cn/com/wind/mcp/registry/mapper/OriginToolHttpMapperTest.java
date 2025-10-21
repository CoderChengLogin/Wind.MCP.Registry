package cn.com.wind.mcp.registry.mapper;

import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OriginToolHttp Mapper 测试
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
@ActiveProfiles("jenkins")
public class OriginToolHttpMapperTest {

    @Autowired
    private OriginToolHttpMapper originToolHttpMapper;

    @Test
    void testInsertOriginToolHttp() {
        OriginToolHttp tool = new OriginToolHttp();
        tool.setProviderToolNum(12345L);
        tool.setNameDisplay("Test HTTP Tool");
        tool.setDescDisplay("This is a test HTTP tool");
        tool.setReqUrl("https://api.example.com/test");
        tool.setReqMethod("POST");
        tool.setReqHeaders("{\"Content-Type\":\"application/json\"}");
        tool.setInputSchema("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}");
        tool.setOutputSchema("{\"type\":\"object\",\"properties\":{\"result\":{\"type\":\"string\"}}}");
        tool.setProviderId(1L);
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        int result = originToolHttpMapper.insert(tool);
        assertEquals(1, result);
        assertNotNull(tool.getId());
    }

    @Test
    void testSelectById() {
        // 先插入一个HTTP工具
        OriginToolHttp tool = new OriginToolHttp();
        tool.setProviderToolNum(54321L);
        tool.setNameDisplay("Test Select Tool");
        tool.setDescDisplay("Test select HTTP tool");
        tool.setReqUrl("https://api.example.com/select");
        tool.setReqMethod("GET");
        tool.setProviderId(1L);
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        originToolHttpMapper.insert(tool);
        Long id = tool.getId();

        // 查询
        OriginToolHttp selected = originToolHttpMapper.selectById(id);
        assertNotNull(selected);
        assertEquals("Test Select Tool", selected.getNameDisplay());
        assertEquals("Test select HTTP tool", selected.getDescDisplay());
        assertEquals("https://api.example.com/select", selected.getReqUrl());
        assertEquals("GET", selected.getReqMethod());
    }

    @Test
    void testUpdateById() {
        // 先插入一个HTTP工具
        OriginToolHttp tool = new OriginToolHttp();
        tool.setProviderToolNum(98765L);
        tool.setNameDisplay("Test Update Tool");
        tool.setDescDisplay("Test update HTTP tool");
        tool.setReqUrl("https://api.example.com/update");
        tool.setReqMethod("PUT");
        tool.setProviderId(1L);
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        originToolHttpMapper.insert(tool);
        Long id = tool.getId();

        // 更新
        tool.setNameDisplay("Updated HTTP Tool");
        tool.setDescDisplay("Updated HTTP tool description");
        tool.setReqUrl("https://api.example.com/updated");
        tool.setReqMethod("PATCH");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("updater");

        int result = originToolHttpMapper.updateById(tool);
        assertEquals(1, result);

        // 验证更新
        OriginToolHttp updated = originToolHttpMapper.selectById(id);
        assertEquals("Updated HTTP Tool", updated.getNameDisplay());
        assertEquals("Updated HTTP tool description", updated.getDescDisplay());
        assertEquals("https://api.example.com/updated", updated.getReqUrl());
        assertEquals("PATCH", updated.getReqMethod());
        assertEquals("updater", updated.getUpdateBy());
    }

    @Test
    void testDeleteById() {
        // 先插入一个HTTP工具
        OriginToolHttp tool = new OriginToolHttp();
        tool.setProviderToolNum(11111L);
        tool.setNameDisplay("Test Delete Tool");
        tool.setDescDisplay("Test delete HTTP tool");
        tool.setReqUrl("https://api.example.com/delete");
        tool.setReqMethod("DELETE");
        tool.setProviderId(1L);
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        originToolHttpMapper.insert(tool);
        Long id = tool.getId();

        // 删除
        int result = originToolHttpMapper.deleteById(id);
        assertEquals(1, result);

        // 验证删除
        OriginToolHttp deleted = originToolHttpMapper.selectById(id);
        assertNull(deleted);
    }

    @Test
    void testHelperMethods() {
        // 测试实体类的辅助方法
        OriginToolHttp tool = new OriginToolHttp();
        tool.setName("Test Helper");
        tool.setDescription("Test helper methods");
        tool.setMethod("POST");
        tool.setUrl("https://api.example.com/helper");

        assertEquals("Test Helper", tool.getNameDisplay());
        assertEquals("Test helper methods", tool.getDescDisplay());
        assertEquals("POST", tool.getReqMethod());
        assertEquals("https://api.example.com/helper", tool.getReqUrl());
        assertEquals("http", tool.getType());
    }
}