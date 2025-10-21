package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.entity.OriginToolExpo;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.OriginProviderConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProviderAppController unit test
 * Tests all provider application node management endpoints
 *
 * @author system
 * @date Created in 2025-10-14
 */
class ProviderAppControllerTest {

    @InjectMocks
    private ProviderAppController providerAppController;

    @Mock
    private OriginProviderConfigService originProviderConfigService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ========== List Page Tests ==========

    /**
     * Test list page - not logged in should redirect to login
     */
    @Test
    void testListPage_NotLoggedIn_ShouldRedirectToLogin() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        String viewName = providerAppController.listPage(session, model);

        assertEquals("redirect:/provider/login", viewName);
        verify(model, never()).addAttribute(anyString(), any());
    }

    /**
     * Test list page - logged in should return list view with load balancers
     */
    @Test
    void testListPage_LoggedIn_ShouldReturnListView() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        // Mock distinct app_name query
        OriginProviderConfig config1 = new OriginProviderConfig();
        config1.setAppName("app-service-1");

        OriginProviderConfig config2 = new OriginProviderConfig();
        config2.setAppName("app-service-2");

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.list(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(config1, config2))
                .thenReturn(createNodeList(2, true))
                .thenReturn(createNodeList(3, false));

        String viewName = providerAppController.listPage(session, model);

        assertEquals("provider-app/list", viewName);
        verify(model).addAttribute(eq("loadBalancers"), anyList());
        verify(model).addAttribute("providerId", 1L);
    }

    /**
     * Test list page - exception handling
     */
    @Test
    void testListPage_ExceptionHandling() {
        Provider provider = new Provider();
        provider.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.list(any(QueryWrapper.class)))
                .thenThrow(new RuntimeException("Database error"));

        String viewName = providerAppController.listPage(session, model);

        assertEquals("provider-app/list", viewName);
        verify(model).addAttribute(eq("loadBalancers"), anyList());
        verify(model).addAttribute("providerId", 1L);
    }

    // ========== Page API Tests ==========

    /**
     * Test page API - not logged in
     */
    @Test
    void testPage_NotLoggedIn() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        Map<String, Object> result = providerAppController.page(1, 10, null, null, null, session);

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户未登录", result.get("message"));
    }

    /**
     * Test page API - successful pagination
     */
    @Test
    void testPage_Success() {
        Provider provider = new Provider();
        provider.setId(1L);

        @SuppressWarnings("unchecked")
        Page<OriginProviderConfig> mockPage = mock(Page.class);
        when(mockPage.getTotal()).thenReturn(10L);
        when(mockPage.getRecords()).thenReturn(createNodeList(5, true));
        when(mockPage.getPages()).thenReturn(2L);
        when(mockPage.getCurrent()).thenReturn(1L);
        when(mockPage.getSize()).thenReturn(5L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(mockPage);

        Map<String, Object> result = providerAppController.page(1, 10, null, null, null, session);

        assertTrue((Boolean) result.get("success"));
        assertEquals(10L, result.get("total"));
        assertEquals(2L, result.get("pages"));
        assertNotNull(result.get("records"));
    }

    /**
     * Test page API - with filters
     */
    @Test
    void testPage_WithFilters() {
        Provider provider = new Provider();
        provider.setId(1L);

        @SuppressWarnings("unchecked")
        Page<OriginProviderConfig> mockPage = mock(Page.class);
        when(mockPage.getTotal()).thenReturn(2L);
        when(mockPage.getRecords()).thenReturn(createNodeList(2, true));
        when(mockPage.getPages()).thenReturn(1L);
        when(mockPage.getCurrent()).thenReturn(1L);
        when(mockPage.getSize()).thenReturn(10L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.page(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(mockPage);

        Map<String, Object> result = providerAppController.page(1, 10, "app-service",
                "production", true, session);

        assertTrue((Boolean) result.get("success"));
        assertEquals(2L, result.get("total"));
    }

    /**
     * Test page API - exception handling
     */
    @Test
    void testPage_ExceptionHandling() {
        Provider provider = new Provider();
        provider.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.page(any(Page.class), any(QueryWrapper.class)))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.page(1, 10, null, null, null, session);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("查询失败"));
    }

    // ========== List By App Name Tests ==========

    /**
     * Test list by app name - not logged in
     */
    @Test
    void testListByAppName_NotLoggedIn() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        Map<String, Object> result = providerAppController.listByAppName("app-service", session);

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户未登录", result.get("message"));
    }

    /**
     * Test list by app name - success
     */
    @Test
    void testListByAppName_Success() {
        Provider provider = new Provider();
        provider.setId(1L);

        List<OriginProviderConfig> nodes = createNodeListWithSiteTypes();

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.list(any(QueryWrapper.class))).thenReturn(nodes);

        Map<String, Object> result = providerAppController.listByAppName("app-service", session);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("nodesBySite"));
        assertEquals(3, result.get("totalCount"));
    }

    /**
     * Test list by app name - exception handling
     */
    @Test
    void testListByAppName_ExceptionHandling() {
        Provider provider = new Provider();
        provider.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.list(any(QueryWrapper.class)))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.listByAppName("app-service", session);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("查询失败"));
    }

    // ========== Detail Tests ==========

    /**
     * Test detail - node exists
     */
    @Test
    void testDetail_NodeExists() {
        OriginProviderConfig config = new OriginProviderConfig();
        config.setId(1L);
        config.setAppName("app-service");
        config.setStatus(1);

        when(originProviderConfigService.getById(1L)).thenReturn(config);

        Map<String, Object> result = providerAppController.detail(1L);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("data"));
    }

    /**
     * Test detail - node not found
     */
    @Test
    void testDetail_NodeNotFound() {
        when(originProviderConfigService.getById(1L)).thenReturn(null);

        Map<String, Object> result = providerAppController.detail(1L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("应用节点不存在", result.get("message"));
    }

    /**
     * Test detail - node deleted
     */
    @Test
    void testDetail_NodeDeleted() {
        OriginProviderConfig config = new OriginProviderConfig();
        config.setId(1L);
        config.setStatus(-1);

        when(originProviderConfigService.getById(1L)).thenReturn(config);

        Map<String, Object> result = providerAppController.detail(1L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("应用节点不存在", result.get("message"));
    }

    /**
     * Test detail - exception handling
     */
    @Test
    void testDetail_ExceptionHandling() {
        when(originProviderConfigService.getById(1L))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.detail(1L);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("获取失败"));
    }

    // ========== Create Tests ==========

    /**
     * Test create - not logged in
     */
    @Test
    void testCreate_NotLoggedIn() {
        OriginProviderConfig config = new OriginProviderConfig();
        config.setAppName("test-app");

        when(session.getAttribute("currentProvider")).thenReturn(null);

        Map<String, Object> result = providerAppController.create(config, session);

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户未登录", result.get("message"));
    }

    /**
     * Test create - success
     */
    @Test
    void testCreate_Success() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginProviderConfig config = new OriginProviderConfig();
        config.setAppName("test-app");

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.createConfig(any(OriginProviderConfig.class))).thenReturn(true);

        Map<String, Object> result = providerAppController.create(config, session);

        assertTrue((Boolean) result.get("success"));
        assertEquals("创建成功", result.get("message"));
        assertEquals(1L, config.getProviderId());
        assertEquals("testuser", config.getCreateBy());
        assertEquals("testuser", config.getUpdateBy());
    }

    /**
     * Test create - failed
     */
    @Test
    void testCreate_Failed() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginProviderConfig config = new OriginProviderConfig();

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.createConfig(any(OriginProviderConfig.class))).thenReturn(false);

        Map<String, Object> result = providerAppController.create(config, session);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("创建失败"));
    }

    /**
     * Test create - exception handling
     */
    @Test
    void testCreate_ExceptionHandling() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginProviderConfig config = new OriginProviderConfig();

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.createConfig(any(OriginProviderConfig.class)))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.create(config, session);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("创建失败"));
    }

    // ========== Update Tests ==========

    /**
     * Test update - not logged in
     */
    @Test
    void testUpdate_NotLoggedIn() {
        OriginProviderConfig config = new OriginProviderConfig();
        config.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(null);

        Map<String, Object> result = providerAppController.update(config, session);

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户未登录", result.get("message"));
    }

    /**
     * Test update - success
     */
    @Test
    void testUpdate_Success() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginProviderConfig config = new OriginProviderConfig();
        config.setId(1L);
        config.setAppName("updated-app");

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.updateConfig(any(OriginProviderConfig.class))).thenReturn(true);

        Map<String, Object> result = providerAppController.update(config, session);

        assertTrue((Boolean) result.get("success"));
        assertEquals("更新成功", result.get("message"));
        assertEquals("testuser", config.getUpdateBy());
    }

    /**
     * Test update - failed
     */
    @Test
    void testUpdate_Failed() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginProviderConfig config = new OriginProviderConfig();
        config.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.updateConfig(any(OriginProviderConfig.class))).thenReturn(false);

        Map<String, Object> result = providerAppController.update(config, session);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("更新失败"));
    }

    /**
     * Test update - exception handling
     */
    @Test
    void testUpdate_ExceptionHandling() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginProviderConfig config = new OriginProviderConfig();
        config.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.updateConfig(any(OriginProviderConfig.class)))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.update(config, session);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("更新失败"));
    }

    // ========== Toggle Enable Tests ==========

    /**
     * Test toggle enable - enable success
     */
    @Test
    void testToggleEnable_EnableSuccess() {
        when(originProviderConfigService.enableConfig(1L)).thenReturn(true);

        Map<String, Object> result = providerAppController.toggleEnable(1L, true);

        assertTrue((Boolean) result.get("success"));
        assertEquals("启用成功", result.get("message"));
        verify(originProviderConfigService).enableConfig(1L);
    }

    /**
     * Test toggle enable - disable success
     */
    @Test
    void testToggleEnable_DisableSuccess() {
        when(originProviderConfigService.disableConfig(1L)).thenReturn(true);

        Map<String, Object> result = providerAppController.toggleEnable(1L, false);

        assertTrue((Boolean) result.get("success"));
        assertEquals("禁用成功", result.get("message"));
        verify(originProviderConfigService).disableConfig(1L);
    }

    /**
     * Test toggle enable - failed
     */
    @Test
    void testToggleEnable_Failed() {
        when(originProviderConfigService.enableConfig(1L)).thenReturn(false);

        Map<String, Object> result = providerAppController.toggleEnable(1L, true);

        assertFalse((Boolean) result.get("success"));
        assertEquals("操作失败", result.get("message"));
    }

    /**
     * Test toggle enable - exception handling
     */
    @Test
    void testToggleEnable_ExceptionHandling() {
        when(originProviderConfigService.enableConfig(1L))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.toggleEnable(1L, true);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("操作失败"));
    }

    // ========== Delete Tests ==========

    /**
     * Test delete - success
     */
    @Test
    void testDelete_Success() {
        when(originProviderConfigService.deleteConfig(1L)).thenReturn(true);

        Map<String, Object> result = providerAppController.delete(1L);

        assertTrue((Boolean) result.get("success"));
        assertEquals("删除成功", result.get("message"));
    }

    /**
     * Test delete - failed
     */
    @Test
    void testDelete_Failed() {
        when(originProviderConfigService.deleteConfig(1L)).thenReturn(false);

        Map<String, Object> result = providerAppController.delete(1L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("删除失败", result.get("message"));
    }

    /**
     * Test delete - exception handling
     */
    @Test
    void testDelete_ExceptionHandling() {
        when(originProviderConfigService.deleteConfig(1L))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.delete(1L);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("删除失败"));
    }

    // ========== Batch Delete Tests ==========

    /**
     * Test batch delete - success
     */
    @Test
    void testBatchDelete_Success() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        when(originProviderConfigService.deleteConfig(anyLong())).thenReturn(true);

        Map<String, Object> result = providerAppController.batchDelete(ids);

        assertTrue((Boolean) result.get("success"));
        assertEquals("批量删除成功", result.get("message"));
        verify(originProviderConfigService, times(3)).deleteConfig(anyLong());
    }

    /**
     * Test batch delete - partial failure
     */
    @Test
    void testBatchDelete_PartialFailure() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        when(originProviderConfigService.deleteConfig(1L)).thenReturn(true);
        when(originProviderConfigService.deleteConfig(2L)).thenReturn(false);

        Map<String, Object> result = providerAppController.batchDelete(ids);

        assertFalse((Boolean) result.get("success"));
        assertEquals("批量删除失败", result.get("message"));
    }

    /**
     * Test batch delete - exception handling
     */
    @Test
    void testBatchDelete_ExceptionHandling() {
        List<Long> ids = Arrays.asList(1L, 2L);

        when(originProviderConfigService.deleteConfig(anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.batchDelete(ids);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("批量删除失败"));
    }

    // ========== Check Health Tests ==========

    /**
     * Test check health - has health check URL
     */
    @Test
    void testCheckHealth_HasHealthCheckUrl() {
        OriginProviderConfig config = new OriginProviderConfig();
        config.setId(1L);
        config.setHealthCheckUrl("http://example.com/health");

        when(originProviderConfigService.getById(1L)).thenReturn(config);

        Map<String, Object> result = providerAppController.checkHealth(1L);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("healthStatus"));
    }

    /**
     * Test check health - no health check URL
     */
    @Test
    void testCheckHealth_NoHealthCheckUrl() {
        OriginProviderConfig config = new OriginProviderConfig();
        config.setId(1L);
        config.setHealthCheckUrl(null);

        when(originProviderConfigService.getById(1L)).thenReturn(config);

        Map<String, Object> result = providerAppController.checkHealth(1L);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("未配置健康检查URL"));
    }

    /**
     * Test check health - node not found
     */
    @Test
    void testCheckHealth_NodeNotFound() {
        when(originProviderConfigService.getById(1L)).thenReturn(null);

        Map<String, Object> result = providerAppController.checkHealth(1L);

        assertFalse((Boolean) result.get("success"));
    }

    /**
     * Test check health - exception handling
     */
    @Test
    void testCheckHealth_ExceptionHandling() {
        when(originProviderConfigService.getById(1L))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.checkHealth(1L);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("检查失败"));
    }

    // ========== List Enabled Tests ==========

    /**
     * Test list enabled - not logged in
     */
    @Test
    void testListEnabled_NotLoggedIn() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        Map<String, Object> result = providerAppController.listEnabled(session);

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户未登录", result.get("message"));
    }

    /**
     * Test list enabled - success
     */
    @Test
    void testListEnabled_Success() {
        Provider provider = new Provider();
        provider.setId(1L);

        List<OriginProviderConfig> enabledApps = createNodeList(3, true);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.list(any(QueryWrapper.class))).thenReturn(enabledApps);

        Map<String, Object> result = providerAppController.listEnabled(session);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("apps"));
    }

    /**
     * Test list enabled - deduplicate by app_name
     * 测试同一个应用有多个站点时的去重逻辑
     */
    @Test
    void testListEnabled_DeduplicateByAppName() {
        Provider provider = new Provider();
        provider.setId(1L);

        // 创建同一个应用的多个节点(不同站点,不同负载因子)
        List<OriginProviderConfig> allApps = new ArrayList<>();

        // MyApp - 测试站 - 负载因子10
        OriginProviderConfig app1 = new OriginProviderConfig();
        app1.setId(1L);
        app1.setAppName("MyApp");
        app1.setSiteType("测试站");
        app1.setAppIp("192.168.1.1");
        app1.setAppPort(8080);
        app1.setLoadFactor(10);
        app1.setIsEnabled(true);
        app1.setStatus(1);
        allApps.add(app1);

        // MyApp - 河西 - 负载因子20 (最高)
        OriginProviderConfig app2 = new OriginProviderConfig();
        app2.setId(2L);
        app2.setAppName("MyApp");
        app2.setSiteType("河西");
        app2.setAppIp("192.168.1.2");
        app2.setAppPort(8080);
        app2.setLoadFactor(20);
        app2.setIsEnabled(true);
        app2.setStatus(1);
        allApps.add(app2);

        // MyApp - 外高桥 - 负载因子15
        OriginProviderConfig app3 = new OriginProviderConfig();
        app3.setId(3L);
        app3.setAppName("MyApp");
        app3.setSiteType("外高桥");
        app3.setAppIp("192.168.1.3");
        app3.setAppPort(8080);
        app3.setLoadFactor(15);
        app3.setIsEnabled(true);
        app3.setStatus(1);
        allApps.add(app3);

        // 另一个应用 - 只有一个节点
        OriginProviderConfig app4 = new OriginProviderConfig();
        app4.setId(4L);
        app4.setAppName("AnotherApp");
        app4.setSiteType("生产站");
        app4.setAppIp("192.168.1.4");
        app4.setAppPort(9090);
        app4.setLoadFactor(30);
        app4.setIsEnabled(true);
        app4.setStatus(1);
        allApps.add(app4);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.list(any(QueryWrapper.class))).thenReturn(allApps);

        Map<String, Object> result = providerAppController.listEnabled(session);

        assertTrue((Boolean) result.get("success"));
        @SuppressWarnings("unchecked")
        List<OriginProviderConfig> apps = (List<OriginProviderConfig>) result.get("apps");

        // 验证去重后只有2个应用
        assertEquals(2, apps.size());

        // 验证MyApp选择了负载因子最高的节点(河西站,ID=2)
        OriginProviderConfig myApp = apps.stream()
                .filter(app -> "MyApp".equals(app.getAppName()))
                .findFirst()
                .orElse(null);
        assertNotNull(myApp);
        assertEquals(2L, myApp.getId());
        assertEquals("河西", myApp.getSiteType());
        assertEquals(20, myApp.getLoadFactor());

        // 验证AnotherApp存在
        OriginProviderConfig anotherApp = apps.stream()
                .filter(app -> "AnotherApp".equals(app.getAppName()))
                .findFirst()
                .orElse(null);
        assertNotNull(anotherApp);
        assertEquals(4L, anotherApp.getId());
    }

    /**
     * Test list enabled - exception handling
     */
    @Test
    void testListEnabled_ExceptionHandling() {
        Provider provider = new Provider();
        provider.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originProviderConfigService.list(any(QueryWrapper.class)))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.listEnabled(session);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("获取失败"));
    }

    // ========== Related Tools Tests ==========

    /**
     * Test get related HTTP tools - success
     */
    @Test
    void testGetRelatedHttpTools_Success() {
        List<OriginToolHttp> httpTools = createHttpToolList(2);

        when(originProviderConfigService.getRelatedHttpTools(1L)).thenReturn(httpTools);

        Map<String, Object> result = providerAppController.getRelatedHttpTools(1L);

        assertTrue((Boolean) result.get("success"));
        assertEquals(2, result.get("count"));
        assertNotNull(result.get("tools"));
    }

    /**
     * Test get related HTTP tools - exception handling
     */
    @Test
    void testGetRelatedHttpTools_ExceptionHandling() {
        when(originProviderConfigService.getRelatedHttpTools(1L))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.getRelatedHttpTools(1L);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("查询失败"));
    }

    /**
     * Test get related Expo tools - success
     */
    @Test
    void testGetRelatedExpoTools_Success() {
        List<OriginToolExpo> expoTools = createExpoToolList(3);

        when(originProviderConfigService.getRelatedExpoTools(1L)).thenReturn(expoTools);

        Map<String, Object> result = providerAppController.getRelatedExpoTools(1L);

        assertTrue((Boolean) result.get("success"));
        assertEquals(3, result.get("count"));
        assertNotNull(result.get("tools"));
    }

    /**
     * Test get related Expo tools - exception handling
     */
    @Test
    void testGetRelatedExpoTools_ExceptionHandling() {
        when(originProviderConfigService.getRelatedExpoTools(1L))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.getRelatedExpoTools(1L);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("查询失败"));
    }

    /**
     * Test get related tools - success (HTTP + Expo)
     */
    @Test
    void testGetRelatedTools_Success() {
        List<OriginToolHttp> httpTools = createHttpToolList(2);
        List<OriginToolExpo> expoTools = createExpoToolList(3);

        when(originProviderConfigService.getRelatedHttpTools(1L)).thenReturn(httpTools);
        when(originProviderConfigService.getRelatedExpoTools(1L)).thenReturn(expoTools);

        Map<String, Object> result = providerAppController.getRelatedTools(1L);

        assertTrue((Boolean) result.get("success"));
        assertEquals(2, result.get("httpCount"));
        assertEquals(3, result.get("expoCount"));
        assertEquals(5, result.get("totalCount"));
        assertNotNull(result.get("httpTools"));
        assertNotNull(result.get("expoTools"));
    }

    /**
     * Test get related tools - exception handling
     */
    @Test
    void testGetRelatedTools_ExceptionHandling() {
        when(originProviderConfigService.getRelatedHttpTools(1L))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerAppController.getRelatedTools(1L);

        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("查询失败"));
    }

    // ========== Helper Methods ==========

    /**
     * Create a list of OriginProviderConfig nodes for testing
     */
    private List<OriginProviderConfig> createNodeList(int count, boolean enabled) {
        List<OriginProviderConfig> nodes = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            OriginProviderConfig config = new OriginProviderConfig();
            config.setId((long) i);
            config.setAppName("app-service-" + i);
            config.setAppIp("192.168.1." + i);
            config.setAppPort(8080 + i);
            config.setSiteType("production");
            config.setIsEnabled(enabled);
            config.setStatus(1);
            nodes.add(config);
        }
        return nodes;
    }

    /**
     * Create a list of nodes with different site types
     */
    private List<OriginProviderConfig> createNodeListWithSiteTypes() {
        List<OriginProviderConfig> nodes = new ArrayList<>();

        OriginProviderConfig config1 = new OriginProviderConfig();
        config1.setId(1L);
        config1.setSiteType("production");
        config1.setStatus(1);

        OriginProviderConfig config2 = new OriginProviderConfig();
        config2.setId(2L);
        config2.setSiteType("production");
        config2.setStatus(1);

        OriginProviderConfig config3 = new OriginProviderConfig();
        config3.setId(3L);
        config3.setSiteType("development");
        config3.setStatus(1);

        nodes.add(config1);
        nodes.add(config2);
        nodes.add(config3);

        return nodes;
    }

    /**
     * Create a list of HTTP tools for testing
     */
    private List<OriginToolHttp> createHttpToolList(int count) {
        List<OriginToolHttp> tools = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            OriginToolHttp tool = new OriginToolHttp();
            tool.setId((long) i);
            tool.setNameDisplay("http-tool-" + i);
            tools.add(tool);
        }
        return tools;
    }

    /**
     * Create a list of Expo tools for testing
     */
    private List<OriginToolExpo> createExpoToolList(int count) {
        List<OriginToolExpo> tools = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            OriginToolExpo tool = new OriginToolExpo();
            tool.setId((long) i);
            tool.setNameDisplay("expo-tool-" + i);
            tools.add(tool);
        }
        return tools;
    }
}
