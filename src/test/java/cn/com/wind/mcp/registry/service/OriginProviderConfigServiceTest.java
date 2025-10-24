package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.entity.OriginToolExpo;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.entity.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 应用节点配置服务测试类
 * 测试CRUD操作和关联工具查询
 *
 * @author system
 * @date 2025-10-24
 */
@SpringBootTest
@Transactional
@ActiveProfiles("jenkins")
@DisplayName("应用节点配置服务测试")
public class OriginProviderConfigServiceTest {

    @Autowired
    private OriginProviderConfigService configService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private OriginToolHttpService httpToolService;

    @Autowired
    private OriginToolExpoService expoToolService;

    private Provider testProvider;
    private OriginProviderConfig testConfig1;
    private OriginProviderConfig testConfig2;

    /**
     * 初始化测试数据
     */
    @BeforeEach
    void setUp() {
        // 创建测试用户
        testProvider = new Provider();
        testProvider.setUsername("test_config_" + System.currentTimeMillis());
        testProvider.setPassword("test123");
        testProvider.setSalt("testsalt");
        testProvider.setEmail("test@example.com");
        testProvider.setStatus(1);
        testProvider.setCreateTime(LocalDateTime.now());
        providerService.save(testProvider);

        // 创建第一个应用配置(河西站点)
        testConfig1 = new OriginProviderConfig();
        testConfig1.setAppNum(System.currentTimeMillis());
        testConfig1.setProviderId(testProvider.getId());
        testConfig1.setAppName("test_app_load_balancer");
        testConfig1.setSiteType("河西");
        testConfig1.setAppIp("192.168.1.100");
        testConfig1.setAppPort(8080);
        testConfig1.setLoadFactor(100); // Integer类型,表示权重因子*100
        testConfig1.setIsEnabled(true);
        testConfig1.setStatus(1);
        testConfig1.setHealthCheckUrl("/health");
        testConfig1.setHealthCheckInterval(30);
        testConfig1.setCreateTime(LocalDateTime.now());
        testConfig1.setUpdateTime(LocalDateTime.now());
        testConfig1.setCreateBy("test");
        testConfig1.setUpdateBy("test");
        configService.save(testConfig1);

        // 创建第二个应用配置(外高桥站点)
        testConfig2 = new OriginProviderConfig();
        testConfig2.setAppNum(System.currentTimeMillis() + 1);
        testConfig2.setProviderId(testProvider.getId());
        testConfig2.setAppName("test_app_load_balancer"); // 相同app_name,模拟负载均衡
        testConfig2.setSiteType("外高桥");
        testConfig2.setAppIp("192.168.2.200");
        testConfig2.setAppPort(8080);
        testConfig2.setLoadFactor(80); // Integer类型,表示权重因子*100
        testConfig2.setIsEnabled(true);
        testConfig2.setStatus(1);
        testConfig2.setHealthCheckUrl("/health");
        testConfig2.setHealthCheckInterval(60);
        testConfig2.setCreateTime(LocalDateTime.now());
        testConfig2.setUpdateTime(LocalDateTime.now());
        testConfig2.setCreateBy("test");
        testConfig2.setUpdateBy("test");
        configService.save(testConfig2);
    }

    /**
     * 测试: 创建应用配置(自动生成app_num)
     */
    @Test
    @DisplayName("应成功创建应用配置并自动生成app_num")
    void testCreateConfig_AutoGenerateAppNum_Success() {
        // 准备数据(不设置app_num)
        OriginProviderConfig newConfig = new OriginProviderConfig();
        newConfig.setProviderId(testProvider.getId());
        newConfig.setAppName("new_test_app");
        newConfig.setSiteType("测试站");
        newConfig.setAppIp("10.0.0.1");
        newConfig.setAppPort(9090);
        newConfig.setCreateBy("test");
        newConfig.setUpdateBy("test");

        // 执行创建
        boolean result = configService.createConfig(newConfig);

        // 验证
        assertTrue(result, "创建应成功");
        assertNotNull(newConfig.getAppNum(), "app_num应被自动生成");
        assertTrue(newConfig.getAppNum() > 0, "app_num应为正数");
        assertEquals(1, newConfig.getStatus(), "status应默认为1");
        assertTrue(newConfig.getIsEnabled(), "is_enabled应默认为true");
        assertNotNull(newConfig.getCreateTime(), "create_time应被设置");
    }

    /**
     * 测试: 根据app_num查询配置
     */
    @Test
    @DisplayName("应根据app_num成功查询配置")
    void testGetByAppNum_Success() {
        // 执行查询
        OriginProviderConfig found = configService.getByAppNum(testConfig1.getAppNum());

        // 验证
        assertNotNull(found, "应找到配置");
        assertEquals(testConfig1.getId(), found.getId(), "ID应匹配");
        assertEquals("test_app_load_balancer", found.getAppName(), "应用名应匹配");
        assertEquals("河西", found.getSiteType(), "站点类型应匹配");
    }

    /**
     * 测试: 根据提供商ID查询所有配置
     */
    @Test
    @DisplayName("应根据提供商ID查询所有配置")
    void testGetByProviderId_Success() {
        // 执行查询
        List<OriginProviderConfig> configs = configService.getByProviderId(testProvider.getId());

        // 验证
        assertNotNull(configs, "结果不应为null");
        assertTrue(configs.size() >= 2, "至少应有2条配置");

        // 验证两条测试数据都在结果中
        boolean hasConfig1 = configs.stream()
                .anyMatch(c -> c.getId().equals(testConfig1.getId()));
        boolean hasConfig2 = configs.stream()
                .anyMatch(c -> c.getId().equals(testConfig2.getId()));
        assertTrue(hasConfig1, "应包含配置1");
        assertTrue(hasConfig2, "应包含配置2");
    }

    /**
     * 测试: 根据站点类型查询配置
     */
    @Test
    @DisplayName("应根据站点类型查询配置")
    void testGetBySiteType_Success() {
        // 执行查询
        List<OriginProviderConfig> configs = configService.getBySiteType("河西");

        // 验证
        assertNotNull(configs, "结果不应为null");
        assertTrue(configs.size() >= 1, "至少应有1条配置");

        // 验证所有结果的站点类型都是"河西"
        boolean allMatch = configs.stream()
                .allMatch(c -> "河西".equals(c.getSiteType()));
        assertTrue(allMatch, "所有结果的站点类型都应为'河西'");
    }

    /**
     * 测试: 查询所有启用的配置
     */
    @Test
    @DisplayName("应查询所有启用的配置")
    void testGetEnabledConfigs_Success() {
        // 执行查询
        List<OriginProviderConfig> enabledConfigs = configService.getEnabledConfigs();

        // 验证
        assertNotNull(enabledConfigs, "结果不应为null");
        assertTrue(enabledConfigs.size() >= 2, "至少应有2条启用的配置");

        // 验证所有结果都是启用状态
        boolean allEnabled = enabledConfigs.stream()
                .allMatch(c -> Boolean.TRUE.equals(c.getIsEnabled()));
        assertTrue(allEnabled, "所有结果都应为启用状态");
    }

    /**
     * 测试: 更新应用配置
     */
    @Test
    @DisplayName("应成功更新应用配置")
    void testUpdateConfig_Success() {
        // 修改配置
        testConfig1.setAppIp("192.168.1.101");
        testConfig1.setLoadFactor(150); // Integer类型,表示权重因子*100

        // 执行更新
        boolean result = configService.updateConfig(testConfig1);

        // 验证
        assertTrue(result, "更新应成功");

        // 重新查询验证
        OriginProviderConfig updated = configService.getById(testConfig1.getId());
        assertEquals("192.168.1.101", updated.getAppIp(), "IP应已更新");
        assertEquals(150, updated.getLoadFactor(), "负载因子应已更新");
        assertNotNull(updated.getUpdateTime(), "update_time应被设置");
    }

    /**
     * 测试: 逻辑删除应用配置
     */
    @Test
    @DisplayName("应成功逻辑删除应用配置")
    void testDeleteConfig_Success() {
        // 执行逻辑删除
        boolean result = configService.deleteConfig(testConfig1.getId());

        // 验证
        assertTrue(result, "删除应成功");

        // 重新查询验证status
        OriginProviderConfig deleted = configService.getById(testConfig1.getId());
        assertNotNull(deleted, "记录应仍存在(逻辑删除)");
        assertEquals(-1, deleted.getStatus(), "status应为-1(已删除)");
    }

    /**
     * 测试: 启用应用配置
     */
    @Test
    @DisplayName("应成功启用应用配置")
    void testEnableConfig_Success() {
        // 先禁用
        testConfig1.setIsEnabled(false);
        testConfig1.setStatus(0);
        configService.updateById(testConfig1);

        // 执行启用
        boolean result = configService.enableConfig(testConfig1.getId());

        // 验证
        assertTrue(result, "启用应成功");

        // 重新查询验证
        OriginProviderConfig enabled = configService.getById(testConfig1.getId());
        assertTrue(enabled.getIsEnabled(), "is_enabled应为true");
        assertEquals(1, enabled.getStatus(), "status应为1");
    }

    /**
     * 测试: 禁用应用配置
     */
    @Test
    @DisplayName("应成功禁用应用配置")
    void testDisableConfig_Success() {
        // 执行禁用
        boolean result = configService.disableConfig(testConfig1.getId());

        // 验证
        assertTrue(result, "禁用应成功");

        // 重新查询验证
        OriginProviderConfig disabled = configService.getById(testConfig1.getId());
        assertFalse(disabled.getIsEnabled(), "is_enabled应为false");
        assertEquals(0, disabled.getStatus(), "status应为0");
    }

    /**
     * 测试: 查询关联的HTTP工具
     */
    @Test
    @DisplayName("应成功查询关联的HTTP工具")
    void testGetRelatedHttpTools_Success() {
        // 创建关联的HTTP工具
        OriginToolHttp httpTool = new OriginToolHttp();
        httpTool.setProviderToolNum(System.currentTimeMillis());
        httpTool.setProviderAppNum(testConfig1.getAppNum()); // 关联到testConfig1
        httpTool.setReqUrl("http://example.com/api/test");
        httpTool.setReqMethod("GET");
        httpTool.setProviderId(testProvider.getId());
        httpTool.setCreateTime(LocalDateTime.now());
        httpToolService.save(httpTool);

        // 执行查询
        List<OriginToolHttp> relatedTools = configService.getRelatedHttpTools(testConfig1.getAppNum());

        // 验证
        assertNotNull(relatedTools, "结果不应为null");
        assertEquals(1, relatedTools.size(), "应有1个关联的HTTP工具");
        assertEquals(httpTool.getId(), relatedTools.get(0).getId(), "工具ID应匹配");
    }

    /**
     * 测试: 查询关联的Expo工具
     */
    @Test
    @DisplayName("应成功查询关联的Expo工具")
    void testGetRelatedExpoTools_Success() {
        // 创建关联的Expo工具
        OriginToolExpo expoTool = new OriginToolExpo();
        expoTool.setProviderToolNum(System.currentTimeMillis());
        expoTool.setProviderAppNum(testConfig2.getAppNum()); // 关联到testConfig2
        expoTool.setAppClass(1);
        expoTool.setCommandId(2000);
        expoTool.setFunctionName("testFunction");
        expoTool.setProviderId(testProvider.getId());
        expoTool.setCreateTime(LocalDateTime.now());
        expoToolService.save(expoTool);

        // 执行查询
        List<OriginToolExpo> relatedTools = configService.getRelatedExpoTools(testConfig2.getAppNum());

        // 验证
        assertNotNull(relatedTools, "结果不应为null");
        assertEquals(1, relatedTools.size(), "应有1个关联的Expo工具");
        assertEquals(expoTool.getId(), relatedTools.get(0).getId(), "工具ID应匹配");
    }

    /**
     * 测试: 获取所有负载均衡器名称
     */
    @Test
    @DisplayName("应成功获取所有负载均衡器名称")
    void testGetAllLoadBalancerNames_Success() {
        // 执行查询
        List<String> loadNames = configService.getAllLoadBalancerNames();

        // 验证
        assertNotNull(loadNames, "结果不应为null");
        assertTrue(loadNames.contains("test_app_load_balancer"),
                "应包含测试负载均衡器名称");
    }

    /**
     * 测试: 根据负载名称查询节点
     */
    @Test
    @DisplayName("应根据负载名称查询所有节点")
    void testGetNodesByLoadName_Success() {
        // 执行查询
        List<OriginProviderConfig> nodes = configService.getNodesByLoadName("test_app_load_balancer");

        // 验证
        assertNotNull(nodes, "结果不应为null");
        assertEquals(2, nodes.size(), "应有2个服务节点");

        // 验证节点信息
        boolean hasHexi = nodes.stream().anyMatch(n -> "河西".equals(n.getSiteType()));
        boolean hasWaigaoqiao = nodes.stream().anyMatch(n -> "外高桥".equals(n.getSiteType()));
        assertTrue(hasHexi, "应包含河西节点");
        assertTrue(hasWaigaoqiao, "应包含外高桥节点");
    }

    /**
     * 测试: 按站点类型分组查询节点
     */
    @Test
    @DisplayName("应按站点类型分组查询节点")
    void testGetNodesGroupBySiteType_Success() {
        // 执行查询
        Map<String, List<OriginProviderConfig>> groupedNodes =
                configService.getNodesGroupBySiteType("test_app_load_balancer");

        // 验证
        assertNotNull(groupedNodes, "结果不应为null");
        assertEquals(2, groupedNodes.size(), "应有2个站点类型分组");
        assertTrue(groupedNodes.containsKey("河西"), "应包含'河西'分组");
        assertTrue(groupedNodes.containsKey("外高桥"), "应包含'外高桥'分组");

        // 验证每个分组的节点数
        assertEquals(1, groupedNodes.get("河西").size(), "'河西'分组应有1个节点");
        assertEquals(1, groupedNodes.get("外高桥").size(), "'外高桥'分组应有1个节点");
    }

    /**
     * 测试: 按站点类型分组查询节点(包含未分类)
     */
    @Test
    @DisplayName("应处理未分类的站点类型")
    void testGetNodesGroupBySiteType_WithNullSiteType() {
        // 创建没有站点类型的配置
        OriginProviderConfig configNoSiteType = new OriginProviderConfig();
        configNoSiteType.setAppNum(System.currentTimeMillis() + 100);
        configNoSiteType.setProviderId(testProvider.getId());
        configNoSiteType.setAppName("test_app_load_balancer");
        configNoSiteType.setSiteType(null); // 站点类型为null
        configNoSiteType.setAppIp("10.0.0.100");
        configNoSiteType.setAppPort(8080);
        configNoSiteType.setIsEnabled(true);
        configNoSiteType.setStatus(1);
        configNoSiteType.setCreateTime(LocalDateTime.now());
        configNoSiteType.setUpdateTime(LocalDateTime.now());
        configNoSiteType.setCreateBy("test");
        configNoSiteType.setUpdateBy("test");
        configService.save(configNoSiteType);

        // 执行查询
        Map<String, List<OriginProviderConfig>> groupedNodes =
                configService.getNodesGroupBySiteType("test_app_load_balancer");

        // 验证
        assertNotNull(groupedNodes, "结果不应为null");
        assertTrue(groupedNodes.containsKey("未分类"), "应包含'未分类'分组");
        assertTrue(groupedNodes.get("未分类").size() >= 1, "'未分类'分组应至少有1个节点");
    }

    /**
     * 测试: 创建配置时手动指定app_num
     */
    @Test
    @DisplayName("应支持手动指定app_num创建配置")
    void testCreateConfig_ManualAppNum_Success() {
        // 准备数据(手动指定app_num)
        Long manualAppNum = 123456789L;
        OriginProviderConfig newConfig = new OriginProviderConfig();
        newConfig.setAppNum(manualAppNum);
        newConfig.setProviderId(testProvider.getId());
        newConfig.setAppName("manual_app");
        newConfig.setAppIp("10.0.0.2");
        newConfig.setAppPort(9091);
        newConfig.setCreateBy("test");
        newConfig.setUpdateBy("test");

        // 执行创建
        boolean result = configService.createConfig(newConfig);

        // 验证
        assertTrue(result, "创建应成功");
        assertEquals(manualAppNum, newConfig.getAppNum(), "app_num应为手动指定的值");
    }

    /**
     * 测试: 删除不存在的配置
     */
    @Test
    @DisplayName("删除不存在的配置应返回false")
    void testDeleteConfig_NotFound_ReturnsFalse() {
        // 尝试删除不存在的配置
        boolean result = configService.deleteConfig(999999L);

        // 验证
        assertFalse(result, "删除不存在的配置应返回false");
    }

    /**
     * 测试: 启用不存在的配置
     */
    @Test
    @DisplayName("启用不存在的配置应返回false")
    void testEnableConfig_NotFound_ReturnsFalse() {
        // 尝试启用不存在的配置
        boolean result = configService.enableConfig(999999L);

        // 验证
        assertFalse(result, "启用不存在的配置应返回false");
    }

    /**
     * 测试: 禁用不存在的配置
     */
    @Test
    @DisplayName("禁用不存在的配置应返回false")
    void testDisableConfig_NotFound_ReturnsFalse() {
        // 尝试禁用不存在的配置
        boolean result = configService.disableConfig(999999L);

        // 验证
        assertFalse(result, "禁用不存在的配置应返回false");
    }
}
