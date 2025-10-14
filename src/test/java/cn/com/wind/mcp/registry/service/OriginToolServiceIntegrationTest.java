package cn.com.wind.mcp.registry.service;

import java.util.List;
import java.util.Map;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.entity.OriginToolExpo;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Origin工具Service集成测试类
 * 综合测试OriginToolHttpService, OriginToolExpoService, OriginProviderConfigService
 *
 * @author system
 * @date 2025-01-14
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
@ActiveProfiles("jenkins")
@DisplayName("Origin工具Service集成测试")
class OriginToolServiceIntegrationTest {

    @Autowired
    private OriginToolHttpService originToolHttpService;

    @Autowired
    private OriginToolExpoService originToolExpoService;

    @Autowired
    private OriginProviderConfigService originProviderConfigService;

    private OriginToolHttp testHttpTool;
    private OriginToolExpo testExpoTool;
    private OriginProviderConfig testConfig;

    /**
     * 测试前的数据准备
     */
    @BeforeEach
    void setUp() {
        // 准备HTTP工具测试数据
        testHttpTool = new OriginToolHttp();
        testHttpTool.setProviderToolNum(2001L);
        testHttpTool.setNameDisplay("HTTP测试工具");
        testHttpTool.setReqMethod("GET");
        testHttpTool.setReqUrl("http://example.com/api/test");
        testHttpTool.setCreateBy("test_http_user");
        testHttpTool.setProviderId(200L);

        // 准备Expo工具测试数据
        testExpoTool = new OriginToolExpo();
        testExpoTool.setProviderToolNum(3001L);
        testExpoTool.setNameDisplay("Expo测试工具");
        testExpoTool.setProviderToolName("test_expo");
        testExpoTool.setFunctionName("test");
        testExpoTool.setCreateBy("test_expo_user");
        testExpoTool.setProviderId(300L);

        // 准备ProviderConfig测试数据
        testConfig = new OriginProviderConfig();
        testConfig.setAppName("test_load_balancer");
        testConfig.setSiteType("production");
        testConfig.setAppIp("192.168.1.100");
        testConfig.setAppPort(8080);
        testConfig.setLoadFactor(1);
        testConfig.setRequestTimeout(60);
        testConfig.setMaxFailCount(3);
        testConfig.setIsEnabled(true);
        testConfig.setStatus(1);
        testConfig.setProviderId(100L);
        testConfig.setCreateBy("test_admin");
        testConfig.setUpdateBy("test_admin");
        testConfig.setEnv("prod");
    }

    // ===== OriginToolHttpService 测试 =====

    /**
     * 测试创建和统计HTTP工具 - 按创建人
     */
    @Test
    @DisplayName("HTTP工具 - 创建并按创建人统计")
    void testHttpTool_CreateAndCountByCreateBy() {
        // When: 保存HTTP工具
        boolean saved = originToolHttpService.save(testHttpTool);

        // Then: 验证保存成功
        assertTrue(saved, "HTTP工具应该保存成功");

        // When: 统计该创建人的工具数量
        long count = originToolHttpService.countByCreateBy("test_http_user");

        // Then: 验证统计结果
        assertTrue(count > 0, "应该能统计到创建的工具");
    }

    /**
     * 测试统计HTTP工具 - 按提供者ID
     */
    @Test
    @DisplayName("HTTP工具 - 按提供者ID统计")
    void testHttpTool_CountByProviderId() {
        // Given: 保存HTTP工具
        originToolHttpService.save(testHttpTool);

        // When: 按提供者ID统计
        long count = originToolHttpService.countByProviderId(200L);

        // Then: 验证结果
        assertTrue(count > 0, "应该能统计到该提供者的工具");
    }

    // ===== OriginToolExpoService 测试 =====

    /**
     * 测试创建和统计Expo工具 - 按创建人
     */
    @Test
    @DisplayName("Expo工具 - 创建并按创建人统计")
    void testExpoTool_CreateAndCountByCreateBy() {
        // When: 保存Expo工具
        boolean saved = originToolExpoService.save(testExpoTool);

        // Then: 验证保存成功
        assertTrue(saved, "Expo工具应该保存成功");

        // When: 统计该创建人的工具数量
        long count = originToolExpoService.countByCreateBy("test_expo_user");

        // Then: 验证统计结果
        assertTrue(count > 0, "应该能统计到创建的Expo工具");
    }

    /**
     * 测试统计Expo工具 - 按提供者ID
     */
    @Test
    @DisplayName("Expo工具 - 按提供者ID统计")
    void testExpoTool_CountByProviderId() {
        // Given: 保存Expo工具
        originToolExpoService.save(testExpoTool);

        // When: 按提供者ID统计
        long count = originToolExpoService.countByProviderId(300L);

        // Then: 验证结果
        assertTrue(count > 0, "应该能统计到该提供者的Expo工具");
    }

    // ===== OriginProviderConfigService 测试 =====

    /**
     * 测试创建应用配置 - 自动生成app_num
     */
    @Test
    @DisplayName("应用配置 - 创建并自动生成appNum")
    void testProviderConfig_CreateWithAutoAppNum() {
        // Given: 不设置app_num
        testConfig.setAppNum(null);

        // When: 创建配置
        boolean created = originProviderConfigService.createConfig(testConfig);

        // Then: 验证结果
        assertTrue(created, "应该创建成功");
        assertNotNull(testConfig.getAppNum(), "应该自动生成app_num");
        assertTrue(testConfig.getAppNum() > 0, "app_num应该是正数");
    }

    /**
     * 测试查询应用配置 - 按提供者ID
     */
    @Test
    @DisplayName("应用配置 - 按提供者ID查询")
    void testProviderConfig_GetByProviderId() {
        // Given: 创建配置
        originProviderConfigService.createConfig(testConfig);

        // When: 按提供者ID查询
        List<OriginProviderConfig> configs = originProviderConfigService.getByProviderId(100L);

        // Then: 验证结果
        assertNotNull(configs, "查询结果不应为null");
        assertFalse(configs.isEmpty(), "应该能查询到配置");
    }

    /**
     * 测试查询启用的配置
     */
    @Test
    @DisplayName("应用配置 - 查询所有启用的配置")
    void testProviderConfig_GetEnabledConfigs() {
        // Given: 创建启用的配置
        testConfig.setIsEnabled(true);
        testConfig.setStatus(1);
        originProviderConfigService.createConfig(testConfig);

        // When: 查询所有启用的配置
        List<OriginProviderConfig> enabledConfigs = originProviderConfigService.getEnabledConfigs();

        // Then: 验证结果
        assertNotNull(enabledConfigs, "查询结果不应为null");
        assertTrue(enabledConfigs.stream().allMatch(OriginProviderConfig::getIsEnabled),
            "所有配置都应该是启用状态");
    }

    /**
     * 测试更新应用配置
     */
    @Test
    @DisplayName("应用配置 - 更新配置")
    void testProviderConfig_UpdateConfig() {
        // Given: 创建配置
        originProviderConfigService.createConfig(testConfig);
        Long configId = testConfig.getId();

        // When: 更新配置
        testConfig.setAppPort(9090);
        testConfig.setLoadFactor(5);
        boolean updated = originProviderConfigService.updateConfig(testConfig);

        // Then: 验证结果
        assertTrue(updated, "更新应该成功");
        OriginProviderConfig updatedConfig = originProviderConfigService.getById(configId);
        assertEquals(9090, updatedConfig.getAppPort(), "端口应该已更新");
        assertEquals(5, updatedConfig.getLoadFactor(), "负载因子应该已更新");
    }

    /**
     * 测试逻辑删除应用配置
     */
    @Test
    @DisplayName("应用配置 - 逻辑删除")
    void testProviderConfig_DeleteConfig() {
        // Given: 创建配置
        originProviderConfigService.createConfig(testConfig);
        Long configId = testConfig.getId();

        // When: 逻辑删除
        boolean deleted = originProviderConfigService.deleteConfig(configId);

        // Then: 验证结果
        assertTrue(deleted, "删除应该成功");
        OriginProviderConfig deletedConfig = originProviderConfigService.getById(configId);
        assertEquals(-1, deletedConfig.getStatus(), "状态应该为-1");
    }

    /**
     * 测试启用应用配置
     */
    @Test
    @DisplayName("应用配置 - 启用配置")
    void testProviderConfig_EnableConfig() {
        // Given: 创建禁用的配置
        testConfig.setIsEnabled(false);
        testConfig.setStatus(0);
        originProviderConfigService.createConfig(testConfig);
        Long configId = testConfig.getId();

        // When: 启用配置
        boolean enabled = originProviderConfigService.enableConfig(configId);

        // Then: 验证结果
        assertTrue(enabled, "启用应该成功");
        OriginProviderConfig enabledConfig = originProviderConfigService.getById(configId);
        assertTrue(enabledConfig.getIsEnabled(), "should be enabled");
        assertEquals(1, enabledConfig.getStatus(), "状态应该为1");
    }

    /**
     * 测试禁用应用配置
     */
    @Test
    @DisplayName("应用配置 - 禁用配置")
    void testProviderConfig_DisableConfig() {
        // Given: 创建启用的配置
        originProviderConfigService.createConfig(testConfig);
        Long configId = testConfig.getId();

        // When: 禁用配置
        boolean disabled = originProviderConfigService.disableConfig(configId);

        // Then: 验证结果
        assertTrue(disabled, "禁用应该成功");
        OriginProviderConfig disabledConfig = originProviderConfigService.getById(configId);
        assertFalse(disabledConfig.getIsEnabled(), "应该已禁用");
        assertEquals(0, disabledConfig.getStatus(), "状态应该为0");
    }

    /**
     * 测试查询关联的HTTP工具
     */
    @Test
    @DisplayName("应用配置 - 查询关联的HTTP工具")
    void testProviderConfig_GetRelatedHttpTools() {
        // Given: 创建配置并关联HTTP工具
        originProviderConfigService.createConfig(testConfig);
        Long appNum = testConfig.getAppNum();

        testHttpTool.setProviderAppNum(appNum);
        originToolHttpService.save(testHttpTool);

        // When: 查询关联的HTTP工具
        List<OriginToolHttp> relatedTools = originProviderConfigService.getRelatedHttpTools(appNum);

        // Then: 验证结果
        assertNotNull(relatedTools, "查询结果不应为null");
        assertFalse(relatedTools.isEmpty(), "应该能查询到关联的工具");
    }

    /**
     * 测试查询关联的Expo工具
     */
    @Test
    @DisplayName("应用配置 - 查询关联的Expo工具")
    void testProviderConfig_GetRelatedExpoTools() {
        // Given: 创建配置并关联Expo工具
        originProviderConfigService.createConfig(testConfig);
        Long appNum = testConfig.getAppNum();

        testExpoTool.setProviderAppNum(appNum);
        originToolExpoService.save(testExpoTool);

        // When: 查询关联的Expo工具
        List<OriginToolExpo> relatedTools = originProviderConfigService.getRelatedExpoTools(appNum);

        // Then: 验证结果
        assertNotNull(relatedTools, "查询结果不应为null");
        assertFalse(relatedTools.isEmpty(), "应该能查询到关联的Expo工具");
    }

    /**
     * 测试查询所有负载均衡器名称
     */
    @Test
    @DisplayName("应用配置 - 查询所有负载均衡器名称")
    void testProviderConfig_GetAllLoadBalancerNames() {
        // Given: 创建多个配置
        originProviderConfigService.createConfig(testConfig);

        OriginProviderConfig config2 = new OriginProviderConfig();
        config2.setAppName("test_load_balancer_2");
        config2.setSiteType("staging");
        config2.setAppIp("192.168.1.101");
        config2.setAppPort(8081);
        config2.setStatus(1);
        config2.setIsEnabled(true);
        config2.setProviderId(100L);
        config2.setCreateBy("test_admin");
        config2.setUpdateBy("test_admin");
        originProviderConfigService.createConfig(config2);

        // When: 查询所有负载均衡器名称
        List<String> loadNames = originProviderConfigService.getAllLoadBalancerNames();

        // Then: 验证结果
        assertNotNull(loadNames, "查询结果不应为null");
        assertTrue(loadNames.size() >= 2, "应该至少有2个负载均衡器");
    }

    /**
     * 测试按负载名称查询节点
     */
    @Test
    @DisplayName("应用配置 - 按负载名称查询节点")
    void testProviderConfig_GetNodesByLoadName() {
        // Given: 创建同名的多个节点
        String loadName = "multi_node_balancer";
        testConfig.setAppName(loadName);
        originProviderConfigService.createConfig(testConfig);

        OriginProviderConfig node2 = new OriginProviderConfig();
        node2.setAppName(loadName);
        node2.setSiteType("production");
        node2.setAppIp("192.168.1.102");
        node2.setAppPort(8082);
        node2.setStatus(1);
        node2.setIsEnabled(true);
        node2.setProviderId(100L);
        node2.setCreateBy("test_admin");
        node2.setUpdateBy("test_admin");
        originProviderConfigService.createConfig(node2);

        // When: 按负载名称查询节点
        List<OriginProviderConfig> nodes = originProviderConfigService.getNodesByLoadName(loadName);

        // Then: 验证结果
        assertNotNull(nodes, "查询结果不应为null");
        assertEquals(2, nodes.size(), "应该有2个节点");
        assertTrue(nodes.stream().allMatch(n -> loadName.equals(n.getAppName())),
            "所有节点的app_name应该相同");
    }

    /**
     * 测试按站点类型分组查询节点
     */
    @Test
    @DisplayName("应用配置 - 按站点类型分组查询节点")
    void testProviderConfig_GetNodesGroupBySiteType() {
        // Given: 创建不同站点类型的节点
        String loadName = "grouped_balancer";
        testConfig.setAppName(loadName);
        testConfig.setSiteType("production");
        originProviderConfigService.createConfig(testConfig);

        OriginProviderConfig stagingNode = new OriginProviderConfig();
        stagingNode.setAppName(loadName);
        stagingNode.setSiteType("staging");
        stagingNode.setAppIp("192.168.1.103");
        stagingNode.setAppPort(8083);
        stagingNode.setStatus(1);
        stagingNode.setIsEnabled(true);
        stagingNode.setProviderId(100L);
        stagingNode.setCreateBy("test_admin");
        stagingNode.setUpdateBy("test_admin");
        originProviderConfigService.createConfig(stagingNode);

        // When: 按站点类型分组查询
        Map<String, List<OriginProviderConfig>> groupedNodes =
            originProviderConfigService.getNodesGroupBySiteType(loadName);

        // Then: 验证结果
        assertNotNull(groupedNodes, "查询结果不应为null");
        assertTrue(groupedNodes.containsKey("production"), "应该包含production组");
        assertTrue(groupedNodes.containsKey("staging"), "应该包含staging组");
        assertEquals(1, groupedNodes.get("production").size(), "production组应该有1个节点");
        assertEquals(1, groupedNodes.get("staging").size(), "staging组应该有1个节点");
    }
}
