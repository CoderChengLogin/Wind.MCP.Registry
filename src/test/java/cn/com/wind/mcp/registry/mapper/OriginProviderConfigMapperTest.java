package cn.com.wind.mcp.registry.mapper;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OriginProviderConfig Mapper 测试
 * <p>
 * 测试提供商应用配置的CRUD操作
 * </p>
 *
 * @author Claude Code
 * @date 2025-10-14
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
@ActiveProfiles("jenkins")
public class OriginProviderConfigMapperTest {

    @Autowired
    private OriginProviderConfigMapper originProviderConfigMapper;

    /**
     * 测试插入提供商配置
     */
    @Test
    void testInsertOriginProviderConfig() {
        OriginProviderConfig config = new OriginProviderConfig();
        config.setAppNum(10001L);
        config.setProviderId(1L);
        config.setAppName("测试应用");
        config.setSiteType("测试站");
        config.setAppIp("192.168.1.100");
        config.setAppPort(8080);
        config.setLoadFactor(10);
        config.setRequestTimeout(30);
        config.setMaxFailCount(3);
        config.setIsEnabled(true);
        config.setAppDescription("测试应用配置");
        config.setHealthCheckUrl("/health");
        config.setHealthCheckInterval(60000);
        config.setStatus(1);
        config.setEnv("test");
        config.setConfigKey("test.config");
        config.setConfigValue("test_value");
        config.setCreateBy("system");
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateBy("system");
        config.setUpdateTime(LocalDateTime.now());

        int result = originProviderConfigMapper.insert(config);
        assertEquals(1, result);
        assertNotNull(config.getId());
    }

    /**
     * 测试根据ID查询配置
     */
    @Test
    void testSelectById() {
        // 先插入一个配置
        OriginProviderConfig config = new OriginProviderConfig();
        config.setAppNum(10002L);
        config.setProviderId(1L);
        config.setAppName("查询测试应用");
        config.setAppIp("192.168.1.101");
        config.setAppPort(8081);
        config.setLoadFactor(5);
        config.setIsEnabled(true);
        config.setStatus(1);
        config.setEnv("dev");
        config.setCreateBy("system");
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateBy("system");
        config.setUpdateTime(LocalDateTime.now());

        originProviderConfigMapper.insert(config);
        Long id = config.getId();

        // 查询
        OriginProviderConfig selected = originProviderConfigMapper.selectById(id);
        assertNotNull(selected);
        assertEquals("查询测试应用", selected.getAppName());
        assertEquals("192.168.1.101", selected.getAppIp());
        assertEquals(8081, selected.getAppPort());
        assertEquals(5, selected.getLoadFactor());
        assertEquals(true, selected.getIsEnabled());
        assertEquals("dev", selected.getEnv());
    }

    /**
     * 测试更新配置
     */
    @Test
    void testUpdateById() {
        // 先插入一个配置
        OriginProviderConfig config = new OriginProviderConfig();
        config.setAppNum(10003L);
        config.setProviderId(1L);
        config.setAppName("更新测试应用");
        config.setAppIp("192.168.1.102");
        config.setAppPort(8082);
        config.setLoadFactor(10);
        config.setIsEnabled(true);
        config.setStatus(1);
        config.setEnv("test");
        config.setCreateBy("system");
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateBy("system");
        config.setUpdateTime(LocalDateTime.now());

        originProviderConfigMapper.insert(config);
        Long id = config.getId();

        // 更新
        config.setAppName("更新后的应用名称");
        config.setAppIp("192.168.1.200");
        config.setAppPort(9090);
        config.setLoadFactor(20);
        config.setIsEnabled(false);
        config.setEnv("prod");
        config.setConfigKey("updated.key");
        config.setConfigValue("updated_value");
        config.setUpdateBy("updater");
        config.setUpdateTime(LocalDateTime.now());

        int result = originProviderConfigMapper.updateById(config);
        assertEquals(1, result);

        // 验证更新
        OriginProviderConfig updated = originProviderConfigMapper.selectById(id);
        assertEquals("更新后的应用名称", updated.getAppName());
        assertEquals("192.168.1.200", updated.getAppIp());
        assertEquals(9090, updated.getAppPort());
        assertEquals(20, updated.getLoadFactor());
        assertEquals(false, updated.getIsEnabled());
        assertEquals("prod", updated.getEnv());
        assertEquals("updated.key", updated.getConfigKey());
        assertEquals("updated_value", updated.getConfigValue());
        assertEquals("updater", updated.getUpdateBy());
    }

    /**
     * 测试删除配置
     */
    @Test
    void testDeleteById() {
        // 先插入一个配置
        OriginProviderConfig config = new OriginProviderConfig();
        config.setAppNum(10004L);
        config.setProviderId(1L);
        config.setAppName("删除测试应用");
        config.setAppIp("192.168.1.103");
        config.setAppPort(8083);
        config.setLoadFactor(10);
        config.setIsEnabled(true);
        config.setStatus(1);
        config.setEnv("test");
        config.setCreateBy("system");
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateBy("system");
        config.setUpdateTime(LocalDateTime.now());

        originProviderConfigMapper.insert(config);
        Long id = config.getId();

        // 删除
        int result = originProviderConfigMapper.deleteById(id);
        assertEquals(1, result);

        // 验证删除
        OriginProviderConfig deleted = originProviderConfigMapper.selectById(id);
        assertNull(deleted);
    }
}
