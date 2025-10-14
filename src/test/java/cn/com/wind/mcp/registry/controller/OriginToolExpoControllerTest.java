package cn.com.wind.mcp.registry.controller;

import java.util.Arrays;
import java.util.Collections;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.entity.OriginToolExpo;
import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.OriginProviderConfigService;
import cn.com.wind.mcp.registry.service.OriginToolExpoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OriginToolExpoController unit test
 * Tests all Expo tool management endpoints
 *
 * @author system
 * @date Created in 2025-10-14
 */
class OriginToolExpoControllerTest {

    @InjectMocks
    private OriginToolExpoController originToolExpoController;

    @Mock
    private OriginToolExpoService originToolExpoService;

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

    // ========== List Tests ==========

    /**
     * Test list - user not logged in
     */
    @Test
    void testList_NotLoggedIn() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        @SuppressWarnings("unchecked")
        Page<OriginToolExpo> mockPage = mock(Page.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getPages()).thenReturn(0L);
        when(mockPage.getTotal()).thenReturn(0L);

        when(originToolExpoService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        String viewName = originToolExpoController.list(model, 1, 10, session);

        assertEquals("origin-tools-expo/list", viewName);
        verify(model).addAttribute("tools", Collections.emptyList());
        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("toolType", "expo");
    }

    /**
     * Test list - user logged in with tools
     */
    @Test
    void testList_LoggedIn() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolExpo tool = new OriginToolExpo();
        tool.setId(1L);
        tool.setNameDisplay("Test Expo Tool");

        @SuppressWarnings("unchecked")
        Page<OriginToolExpo> mockPage = mock(Page.class);
        when(mockPage.getRecords()).thenReturn(Collections.singletonList(tool));
        when(mockPage.getPages()).thenReturn(1L);
        when(mockPage.getTotal()).thenReturn(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        String viewName = originToolExpoController.list(model, 1, 10, session);

        assertEquals("origin-tools-expo/list", viewName);
        verify(model).addAttribute(eq("tools"), anyList());
        verify(model).addAttribute("totalPages", 1L);
    }

    // ========== Detail Tests ==========

    /**
     * Test detail - tool exists
     */
    @Test
    void testDetail_ToolExists() {
        OriginToolExpo tool = new OriginToolExpo();
        tool.setId(1L);
        tool.setNameDisplay("Test Tool");
        tool.setProviderAppNum(10L);

        OriginProviderConfig providerApp = new OriginProviderConfig();
        providerApp.setId(10L);
        providerApp.setAppName("test-app");

        when(originToolExpoService.getById(1L)).thenReturn(tool);
        when(originProviderConfigService.getById(10L)).thenReturn(providerApp);

        String viewName = originToolExpoController.detail(1L, model);

        assertEquals("origin-tools-expo/detail", viewName);
        verify(model).addAttribute("tool", tool);
        verify(model).addAttribute("providerApp", providerApp);
    }

    /**
     * Test detail - tool not found
     */
    @Test
    void testDetail_ToolNotFound() {
        when(originToolExpoService.getById(1L)).thenReturn(null);

        String viewName = originToolExpoController.detail(1L, model);

        assertEquals("redirect:/origin-expo-tools", viewName);
        verify(model, never()).addAttribute(anyString(), any());
    }

    /**
     * Test detail - tool without provider app
     */
    @Test
    void testDetail_ToolWithoutProviderApp() {
        OriginToolExpo tool = new OriginToolExpo();
        tool.setId(1L);
        tool.setNameDisplay("Test Tool");
        tool.setProviderAppNum(null);

        when(originToolExpoService.getById(1L)).thenReturn(tool);

        String viewName = originToolExpoController.detail(1L, model);

        assertEquals("origin-tools-expo/detail", viewName);
        verify(model).addAttribute("tool", tool);
        verify(model, never()).addAttribute(eq("providerApp"), any());
    }

    // ========== Add Form Tests ==========

    /**
     * Test add form - should return form view with empty tool
     */
    @Test
    void testAddForm_ShouldReturnFormView() {
        String viewName = originToolExpoController.addForm(model);

        assertEquals("origin-tools-expo/form", viewName);
        verify(model).addAttribute(eq("tool"), any(OriginToolExpo.class));
    }

    /**
     * Test new form - should return form view with empty tool
     */
    @Test
    void testNewForm_ShouldReturnFormView() {
        String viewName = originToolExpoController.newForm(model);

        assertEquals("origin-tools-expo/form", viewName);
        verify(model).addAttribute(eq("tool"), any(OriginToolExpo.class));
    }

    // ========== Edit Form Tests ==========

    /**
     * Test edit form - tool exists with permission
     */
    @Test
    void testEditForm_ToolExistsWithPermission() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolExpo tool = new OriginToolExpo();
        tool.setId(1L);
        tool.setProviderId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.getById(1L)).thenReturn(tool);

        String viewName = originToolExpoController.editForm(1L, model, session);

        assertEquals("origin-tools-expo/form", viewName);
        verify(model).addAttribute("tool", tool);
    }

    /**
     * Test edit form - tool not found
     */
    @Test
    void testEditForm_ToolNotFound() {
        when(originToolExpoService.getById(1L)).thenReturn(null);

        String viewName = originToolExpoController.editForm(1L, model, session);

        assertTrue(viewName.startsWith("redirect:/origin-expo-tools?error="));
    }

    /**
     * Test edit form - no permission
     */
    @Test
    void testEditForm_NoPermission() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolExpo tool = new OriginToolExpo();
        tool.setId(1L);
        tool.setProviderId(999L); // Different provider

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.getById(1L)).thenReturn(tool);

        String viewName = originToolExpoController.editForm(1L, model, session);

        assertTrue(viewName.startsWith("redirect:/origin-expo-tools?error="));
        assertTrue(viewName.contains("无权限"));
    }

    // ========== Save Tests ==========

    /**
     * Test save - create new tool
     */
    @Test
    void testSave_CreateNewTool() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginToolExpo tool = new OriginToolExpo();
        tool.setNameDisplay("New Tool");
        // id is null for new tool

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.saveOrUpdate(any(OriginToolExpo.class))).thenReturn(true);

        String viewName = originToolExpoController.save(tool, session);

        assertEquals("redirect:/origin-expo-tools", viewName);
        assertEquals(1L, tool.getProviderId());
        assertEquals("testuser", tool.getCreateBy());
        assertEquals("testuser", tool.getUpdateBy());
        assertNotNull(tool.getCreateTime());
        assertNotNull(tool.getUpdateTime());
        verify(originToolExpoService).saveOrUpdate(tool);
    }

    /**
     * Test save - update existing tool with permission
     */
    @Test
    void testSave_UpdateExistingTool() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginToolExpo existingTool = new OriginToolExpo();
        existingTool.setId(1L);
        existingTool.setProviderId(1L);

        OriginToolExpo toolToUpdate = new OriginToolExpo();
        toolToUpdate.setId(1L);
        toolToUpdate.setNameDisplay("Updated Tool");

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.getById(1L)).thenReturn(existingTool);
        when(originToolExpoService.saveOrUpdate(any(OriginToolExpo.class))).thenReturn(true);

        String viewName = originToolExpoController.save(toolToUpdate, session);

        assertEquals("redirect:/origin-expo-tools", viewName);
        assertEquals(1L, toolToUpdate.getProviderId()); // Should keep original provider
        assertEquals("testuser", toolToUpdate.getUpdateBy());
        assertNotNull(toolToUpdate.getUpdateTime());
        verify(originToolExpoService).saveOrUpdate(toolToUpdate);
    }

    /**
     * Test save - user not logged in
     */
    @Test
    void testSave_NotLoggedIn() {
        OriginToolExpo tool = new OriginToolExpo();

        when(session.getAttribute("currentProvider")).thenReturn(null);

        String viewName = originToolExpoController.save(tool, session);

        assertTrue(viewName.startsWith("redirect:/provider/login?error="));
        verify(originToolExpoService, never()).saveOrUpdate(any());
    }

    /**
     * Test save - update tool that doesn't exist
     */
    @Test
    void testSave_UpdateNonExistentTool() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginToolExpo toolToUpdate = new OriginToolExpo();
        toolToUpdate.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.getById(1L)).thenReturn(null);

        String viewName = originToolExpoController.save(toolToUpdate, session);

        assertTrue(viewName.startsWith("redirect:/origin-expo-tools?error="));
        assertTrue(viewName.contains("工具不存在"));
        verify(originToolExpoService, never()).saveOrUpdate(any());
    }

    /**
     * Test save - update without permission
     */
    @Test
    void testSave_UpdateWithoutPermission() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginToolExpo existingTool = new OriginToolExpo();
        existingTool.setId(1L);
        existingTool.setProviderId(999L); // Different provider

        OriginToolExpo toolToUpdate = new OriginToolExpo();
        toolToUpdate.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.getById(1L)).thenReturn(existingTool);

        String viewName = originToolExpoController.save(toolToUpdate, session);

        assertTrue(viewName.startsWith("redirect:/origin-expo-tools?error="));
        assertTrue(viewName.contains("无权限"));
        verify(originToolExpoService, never()).saveOrUpdate(any());
    }

    // ========== Delete Tests ==========

    /**
     * Test delete - success with permission
     */
    @Test
    void testDelete_Success() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolExpo tool = new OriginToolExpo();
        tool.setId(1L);
        tool.setProviderId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.getById(1L)).thenReturn(tool);
        when(originToolExpoService.removeById(1L)).thenReturn(true);

        String viewName = originToolExpoController.delete(1L, session);

        assertTrue(viewName.startsWith("redirect:/origin-expo-tools"));
        assertTrue(viewName.contains("success"));
        verify(originToolExpoService).removeById(1L);
    }

    /**
     * Test delete - tool not found
     */
    @Test
    void testDelete_ToolNotFound() {
        when(originToolExpoService.getById(1L)).thenReturn(null);

        String viewName = originToolExpoController.delete(1L, session);

        assertTrue(viewName.startsWith("redirect:/origin-expo-tools?error="));
        verify(originToolExpoService, never()).removeById(anyLong());
    }

    /**
     * Test delete - no permission
     */
    @Test
    void testDelete_NoPermission() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolExpo tool = new OriginToolExpo();
        tool.setId(1L);
        tool.setProviderId(999L); // Different provider

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.getById(1L)).thenReturn(tool);

        String viewName = originToolExpoController.delete(1L, session);

        assertTrue(viewName.startsWith("redirect:/origin-expo-tools?error="));
        verify(originToolExpoService, never()).removeById(anyLong());
    }

    // ========== Search Tests ==========

    /**
     * Test search - user not logged in
     */
    @Test
    void testSearch_NotLoggedIn() {
        @SuppressWarnings("unchecked")
        Page<OriginToolExpo> mockPage = mock(Page.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getPages()).thenReturn(0L);
        when(mockPage.getTotal()).thenReturn(0L);

        when(session.getAttribute("currentProvider")).thenReturn(null);
        when(originToolExpoService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        String viewName = originToolExpoController.search("test", model, 1, 10, session);

        assertEquals("origin-tools-expo/list", viewName);
        verify(model).addAttribute("tools", Collections.emptyList());
        verify(model).addAttribute("keyword", "test");
    }

    /**
     * Test search - user logged in with results
     */
    @Test
    void testSearch_LoggedInWithResults() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolExpo tool1 = new OriginToolExpo();
        tool1.setId(1L);
        tool1.setNameDisplay("Test Tool 1");

        OriginToolExpo tool2 = new OriginToolExpo();
        tool2.setId(2L);
        tool2.setDescDisplay("This is a test description");

        @SuppressWarnings("unchecked")
        Page<OriginToolExpo> mockPage = mock(Page.class);
        when(mockPage.getRecords()).thenReturn(Arrays.asList(tool1, tool2));
        when(mockPage.getPages()).thenReturn(1L);
        when(mockPage.getTotal()).thenReturn(2L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        String viewName = originToolExpoController.search("test", model, 1, 10, session);

        assertEquals("origin-tools-expo/list", viewName);
        verify(model).addAttribute(eq("tools"), anyList());
        verify(model).addAttribute("keyword", "test");
        verify(model).addAttribute("totalRecords", 2L);
    }

    /**
     * Test search - empty keyword
     */
    @Test
    void testSearch_EmptyKeyword() {
        Provider provider = new Provider();
        provider.setId(1L);

        @SuppressWarnings("unchecked")
        Page<OriginToolExpo> mockPage = mock(Page.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getPages()).thenReturn(0L);
        when(mockPage.getTotal()).thenReturn(0L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolExpoService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        String viewName = originToolExpoController.search("", model, 1, 10, session);

        assertEquals("origin-tools-expo/list", viewName);
        verify(model).addAttribute("keyword", "");
    }
}
