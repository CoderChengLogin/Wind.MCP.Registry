package cn.com.wind.mcp.registry.mapper;

import java.util.Date;

import cn.com.wind.mcp.registry.entity.VirtualServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * VirtualServer Mapper 测试
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
public class VirtualServerMapperTest {

    @Autowired
    private VirtualServerMapper virtualServerMapper;

    @Test
    void testInsertVirtualServer() {
        VirtualServer server = new VirtualServer();
        server.setVserverId("vserver_001");
        server.setUserid("user_001");
        server.setName("Test Virtual Server");
        server.setDesc("This is a test virtual server");
        server.setUrl("https://test.example.com");
        server.setStatus("1");
        server.setCreateTime(new Date());
        server.setCreateBy("system");
        server.setUpdateTime(new Date());
        server.setUpdateBy("system");

        int result = virtualServerMapper.insert(server);
        assertEquals(1, result);
        assertNotNull(server.getId());
    }

    @Test
    void testSelectById() {
        // 先插入一个虚拟服务器
        VirtualServer server = new VirtualServer();
        server.setVserverId("vserver_select");
        server.setUserid("user_select");
        server.setName("Test Select Server");
        server.setDesc("Test select virtual server");
        server.setUrl("https://select.example.com");
        server.setStatus("1");
        server.setCreateTime(new Date());
        server.setCreateBy("system");
        server.setUpdateTime(new Date());
        server.setUpdateBy("system");

        virtualServerMapper.insert(server);
        Long id = server.getId();

        // 查询
        VirtualServer selected = virtualServerMapper.selectById(id);
        assertNotNull(selected);
        assertEquals("vserver_select", selected.getVserverId());
        assertEquals("user_select", selected.getUserid());
        assertEquals("Test Select Server", selected.getName());
        assertEquals("Test select virtual server", selected.getDesc());
        assertEquals("https://select.example.com", selected.getUrl());
        assertEquals("1", selected.getStatus());
    }

    @Test
    void testUpdateById() {
        // 先插入一个虚拟服务器
        VirtualServer server = new VirtualServer();
        server.setVserverId("vserver_update");
        server.setUserid("user_update");
        server.setName("Test Update Server");
        server.setDesc("Test update virtual server");
        server.setUrl("https://update.example.com");
        server.setStatus("1");
        server.setCreateTime(new Date());
        server.setCreateBy("system");
        server.setUpdateTime(new Date());
        server.setUpdateBy("system");

        virtualServerMapper.insert(server);
        Long id = server.getId();

        // 更新
        server.setName("Updated Virtual Server");
        server.setDesc("Updated virtual server description");
        server.setUrl("https://updated.example.com");
        server.setStatus("0");
        server.setUpdateTime(new Date());
        server.setUpdateBy("updater");

        int result = virtualServerMapper.updateById(server);
        assertEquals(1, result);

        // 验证更新
        VirtualServer updated = virtualServerMapper.selectById(id);
        assertEquals("Updated Virtual Server", updated.getName());
        assertEquals("Updated virtual server description", updated.getDesc());
        assertEquals("https://updated.example.com", updated.getUrl());
        assertEquals("0", updated.getStatus());
        assertEquals("updater", updated.getUpdateBy());
    }

    @Test
    void testDeleteById() {
        // 先插入一个虚拟服务器
        VirtualServer server = new VirtualServer();
        server.setVserverId("vserver_delete");
        server.setUserid("user_delete");
        server.setName("Test Delete Server");
        server.setDesc("Test delete virtual server");
        server.setUrl("https://delete.example.com");
        server.setStatus("1");
        server.setCreateTime(new Date());
        server.setCreateBy("system");
        server.setUpdateTime(new Date());
        server.setUpdateBy("system");

        virtualServerMapper.insert(server);
        Long id = server.getId();

        // 删除
        int result = virtualServerMapper.deleteById(id);
        assertEquals(1, result);

        // 验证删除
        VirtualServer deleted = virtualServerMapper.selectById(id);
        assertNull(deleted);
    }
}