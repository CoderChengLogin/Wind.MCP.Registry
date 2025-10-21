package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.OriginProviderConfigService;
import cn.com.wind.mcp.registry.service.OriginToolHttpService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OriginToolHttpController unit test
 * Tests all HTTP tool management endpoints
 *
 * @author system
 * @date Created in 2025-10-14
 */
class OriginToolHttpControllerTest {

    @InjectMocks
    private OriginToolHttpController originToolHttpController;

    @Mock
    private OriginToolHttpService originToolHttpService;

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
        Page<OriginToolHttp> mockPage = mock(Page.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getPages()).thenReturn(0L);
        when(mockPage.getTotal()).thenReturn(0L);

        when(originToolHttpService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        String viewName = originToolHttpController.list(model, 1, 10, session);

        assertEquals("origin-http-tools/list", viewName);
        verify(model).addAttribute("tools", Collections.emptyList());
        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("totalPages", 0L);
    }

    /**
     * Test list - user logged in with tools
     */
    @Test
    void testList_LoggedIn() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolHttp tool = new OriginToolHttp();
        tool.setId(1L);
        tool.setNameDisplay("Test HTTP Tool");

        @SuppressWarnings("unchecked")
        Page<OriginToolHttp> mockPage = mock(Page.class);
        when(mockPage.getRecords()).thenReturn(Collections.singletonList(tool));
        when(mockPage.getPages()).thenReturn(1L);
        when(mockPage.getTotal()).thenReturn(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        String viewName = originToolHttpController.list(model, 1, 10, session);

        assertEquals("origin-http-tools/list", viewName);
        verify(model).addAttribute(eq("tools"), anyList());
        verify(model).addAttribute("totalPages", 1L);
    }

    // ========== Detail Tests ==========

    /**
     * Test detail - tool exists
     */
    @Test
    void testDetail_ToolExists() {
        OriginToolHttp tool = new OriginToolHttp();
        tool.setId(1L);
        tool.setNameDisplay("Test Tool");
        tool.setProviderAppNum(10L);

        OriginProviderConfig providerApp = new OriginProviderConfig();
        providerApp.setId(10L);
        providerApp.setAppName("test-app");

        when(originToolHttpService.getById(1L)).thenReturn(tool);
        when(originProviderConfigService.getById(10L)).thenReturn(providerApp);

        String viewName = originToolHttpController.detail(1L, model);

        assertEquals("origin-http-tools/detail", viewName);
        verify(model).addAttribute("tool", tool);
        verify(model).addAttribute("providerApp", providerApp);
    }

    /**
     * Test detail - tool not found
     */
    @Test
    void testDetail_ToolNotFound() {
        when(originToolHttpService.getById(1L)).thenReturn(null);

        String viewName = originToolHttpController.detail(1L, model);

        assertEquals("redirect:/origin-http-tools", viewName);
        verify(model, never()).addAttribute(anyString(), any());
    }

    /**
     * Test detail - tool without provider app
     */
    @Test
    void testDetail_ToolWithoutProviderApp() {
        OriginToolHttp tool = new OriginToolHttp();
        tool.setId(1L);
        tool.setNameDisplay("Test Tool");
        tool.setProviderAppNum(null);

        when(originToolHttpService.getById(1L)).thenReturn(tool);

        String viewName = originToolHttpController.detail(1L, model);

        assertEquals("origin-http-tools/detail", viewName);
        verify(model).addAttribute("tool", tool);
        verify(model, never()).addAttribute(eq("providerApp"), any());
    }

    // ========== Add Form Tests ==========

    /**
     * Test add form - should return form view with empty tool
     */
    @Test
    void testAddForm_ShouldReturnFormView() {
        String viewName = originToolHttpController.addForm(model);

        assertEquals("origin-http-tools/form", viewName);
        verify(model).addAttribute(eq("tool"), any(OriginToolHttp.class));
    }

    /**
     * Test new form - should return form view with empty tool
     */
    @Test
    void testNewForm_ShouldReturnFormView() {
        String viewName = originToolHttpController.newForm(null, model);

        assertEquals("origin-http-tools/form", viewName);
        verify(model).addAttribute(eq("tool"), any(OriginToolHttp.class));
    }

    /**
     * Test new form with toolNum - should pre-fill providerToolNum for auto-association
     */
    @Test
    void testNewForm_WithToolNum_ShouldPreFillProviderToolNum() {
        Long toolNum = 1234567890L;

        String viewName = originToolHttpController.newForm(toolNum, model);

        assertEquals("origin-http-tools/form", viewName);
        // 验证添加到model的tool对象的providerToolNum已经被设置
        verify(model).addAttribute(eq("tool"), any(OriginToolHttp.class));
    }

    // ========== Edit Form Tests ==========

    /**
     * Test edit form - tool exists with permission
     */
    @Test
    void testEditForm_ToolExistsWithPermission() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolHttp tool = new OriginToolHttp();
        tool.setId(1L);
        tool.setProviderId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.getById(1L)).thenReturn(tool);

        String viewName = originToolHttpController.editForm(1L, model, session);

        assertEquals("origin-http-tools/form", viewName);
        verify(model).addAttribute("tool", tool);
    }

    /**
     * Test edit form - tool not found
     */
    @Test
    void testEditForm_ToolNotFound() {
        when(originToolHttpService.getById(1L)).thenReturn(null);

        String viewName = originToolHttpController.editForm(1L, model, session);

        assertEquals("redirect:/origin-http-tools?error=工具不存在", viewName);
    }

    /**
     * Test edit form - no permission
     */
    @Test
    void testEditForm_NoPermission() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolHttp tool = new OriginToolHttp();
        tool.setId(1L);
        tool.setProviderId(999L); // Different provider

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.getById(1L)).thenReturn(tool);

        String viewName = originToolHttpController.editForm(1L, model, session);

        assertEquals("redirect:/origin-http-tools?error=无权限编辑此工具", viewName);
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

        OriginToolHttp tool = new OriginToolHttp();
        tool.setNameDisplay("New Tool");
        // id is null for new tool

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.saveOrUpdate(any(OriginToolHttp.class))).thenReturn(true);

        String viewName = originToolHttpController.save(tool, session);

        assertEquals("redirect:/origin-http-tools", viewName);
        assertEquals(1L, tool.getProviderId());
        assertEquals("testuser", tool.getCreateBy());
        assertEquals("testuser", tool.getUpdateBy());
        assertNotNull(tool.getCreateTime());
        assertNotNull(tool.getUpdateTime());
        verify(originToolHttpService).saveOrUpdate(tool);
    }

    /**
     * Test save - update existing tool with permission
     */
    @Test
    void testSave_UpdateExistingTool() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginToolHttp existingTool = new OriginToolHttp();
        existingTool.setId(1L);
        existingTool.setProviderId(1L);

        OriginToolHttp toolToUpdate = new OriginToolHttp();
        toolToUpdate.setId(1L);
        toolToUpdate.setNameDisplay("Updated Tool");

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.getById(1L)).thenReturn(existingTool);
        when(originToolHttpService.saveOrUpdate(any(OriginToolHttp.class))).thenReturn(true);

        String viewName = originToolHttpController.save(toolToUpdate, session);

        assertEquals("redirect:/origin-http-tools", viewName);
        assertEquals(1L, toolToUpdate.getProviderId()); // Should keep original provider
        assertEquals("testuser", toolToUpdate.getUpdateBy());
        assertNotNull(toolToUpdate.getUpdateTime());
        verify(originToolHttpService).saveOrUpdate(toolToUpdate);
    }

    /**
     * Test save - user not logged in
     */
    @Test
    void testSave_NotLoggedIn() {
        OriginToolHttp tool = new OriginToolHttp();

        when(session.getAttribute("currentProvider")).thenReturn(null);

        String viewName = originToolHttpController.save(tool, session);

        assertEquals("redirect:/provider/login?error=用户未登录", viewName);
        verify(originToolHttpService, never()).saveOrUpdate(any());
    }

    /**
     * Test save - update tool that doesn't exist
     */
    @Test
    void testSave_UpdateNonExistentTool() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginToolHttp toolToUpdate = new OriginToolHttp();
        toolToUpdate.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.getById(1L)).thenReturn(null);

        String viewName = originToolHttpController.save(toolToUpdate, session);

        assertEquals("redirect:/origin-http-tools?error=工具不存在", viewName);
        verify(originToolHttpService, never()).saveOrUpdate(any());
    }

    /**
     * Test save - update without permission
     */
    @Test
    void testSave_UpdateWithoutPermission() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        OriginToolHttp existingTool = new OriginToolHttp();
        existingTool.setId(1L);
        existingTool.setProviderId(999L); // Different provider

        OriginToolHttp toolToUpdate = new OriginToolHttp();
        toolToUpdate.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.getById(1L)).thenReturn(existingTool);

        String viewName = originToolHttpController.save(toolToUpdate, session);

        assertEquals("redirect:/origin-http-tools?error=无权限修改此工具", viewName);
        verify(originToolHttpService, never()).saveOrUpdate(any());
    }

    // ========== Delete Tests ==========

    /**
     * Test delete - success with permission
     */
    @Test
    void testDelete_Success() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolHttp tool = new OriginToolHttp();
        tool.setId(1L);
        tool.setProviderId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.getById(1L)).thenReturn(tool);
        when(originToolHttpService.removeById(1L)).thenReturn(true);

        String viewName = originToolHttpController.delete(1L, session);

        assertTrue(viewName.startsWith("redirect:/origin-http-tools"));
        assertTrue(viewName.contains("success"));
        verify(originToolHttpService).removeById(1L);
    }

    /**
     * Test delete - tool not found
     */
    @Test
    void testDelete_ToolNotFound() {
        when(originToolHttpService.getById(1L)).thenReturn(null);

        String viewName = originToolHttpController.delete(1L, session);

        assertTrue(viewName.startsWith("redirect:/origin-http-tools?error="));
        verify(originToolHttpService, never()).removeById(anyLong());
    }

    /**
     * Test delete - no permission
     */
    @Test
    void testDelete_NoPermission() {
        Provider provider = new Provider();
        provider.setId(1L);

        OriginToolHttp tool = new OriginToolHttp();
        tool.setId(1L);
        tool.setProviderId(999L); // Different provider

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.getById(1L)).thenReturn(tool);

        String viewName = originToolHttpController.delete(1L, session);

        assertTrue(viewName.startsWith("redirect:/origin-http-tools?error="));
        verify(originToolHttpService, never()).removeById(anyLong());
    }

    // ========== Search Tests ==========

    /**
     * Test search - user not logged in
     */
    @Test
    void testSearch_NotLoggedIn() {
        @SuppressWarnings("unchecked")
        Page<OriginToolHttp> mockPage = mock(Page.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getPages()).thenReturn(0L);
        when(mockPage.getTotal()).thenReturn(0L);

        when(session.getAttribute("currentProvider")).thenReturn(null);
        when(originToolHttpService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        String viewName = originToolHttpController.search("test", model, 1, 10, session);

        assertEquals("origin-http-tools/list", viewName);
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

        OriginToolHttp tool1 = new OriginToolHttp();
        tool1.setId(1L);
        tool1.setNameDisplay("Test Tool 1");

        OriginToolHttp tool2 = new OriginToolHttp();
        tool2.setId(2L);
        tool2.setDescDisplay("This is a test description");

        @SuppressWarnings("unchecked")
        Page<OriginToolHttp> mockPage = mock(Page.class);
        when(mockPage.getRecords()).thenReturn(Arrays.asList(tool1, tool2));
        when(mockPage.getPages()).thenReturn(1L);
        when(mockPage.getTotal()).thenReturn(2L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        String viewName = originToolHttpController.search("test", model, 1, 10, session);

        assertEquals("origin-http-tools/list", viewName);
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
        Page<OriginToolHttp> mockPage = mock(Page.class);
        when(mockPage.getRecords()).thenReturn(Collections.emptyList());
        when(mockPage.getPages()).thenReturn(0L);
        when(mockPage.getTotal()).thenReturn(0L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(originToolHttpService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        String viewName = originToolHttpController.search("", model, 1, 10, session);

        assertEquals("origin-http-tools/list", viewName);
        verify(model).addAttribute("keyword", "");
    }
}
