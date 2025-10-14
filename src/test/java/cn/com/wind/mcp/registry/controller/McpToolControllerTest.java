package cn.com.wind.mcp.registry.controller;

import java.util.Collections;

import cn.com.wind.mcp.registry.dto.McpToolEditDto;
import cn.com.wind.mcp.registry.entity.HttpTemplateConverter;
import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.mapper.ExpoTemplateConverterMapper;
import cn.com.wind.mcp.registry.mapper.HttpTemplateConverterMapper;
import cn.com.wind.mcp.registry.mapper.OriginToolHttpMapper;
import cn.com.wind.mcp.registry.service.McpToolService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * McpToolController unit test
 * Tests MCP tool management including CRUD operations, search, and multi-table editing
 *
 * @author system
 * @date Created in 2025-10-14
 */
class McpToolControllerTest {

    @InjectMocks
    private McpToolController mcpToolController;

    @Mock
    private McpToolService mcpToolService;

    @Mock
    private OriginToolHttpMapper originToolHttpMapper;

    @Mock
    private HttpTemplateConverterMapper httpTemplateConverterMapper;

    @Mock
    private ExpoTemplateConverterMapper expoTemplateConverterMapper;

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
     * Test list page - user not logged in
     */
    @Test
    void testList_NotLoggedIn_ShouldReturnEmptyList() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        @SuppressWarnings("unchecked")
        Page<McpTool> emptyPage = mock(Page.class);
        when(emptyPage.getRecords()).thenReturn(Collections.emptyList());
        when(emptyPage.getPages()).thenReturn(0L);
        when(emptyPage.getTotal()).thenReturn(0L);
        when(mcpToolService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(emptyPage);

        String viewName = mcpToolController.list(model, 1, 10, "all", session);

        assertEquals("mcp-tools/list", viewName);
        verify(model).addAttribute(eq("tools"), anyList());
        verify(model).addAttribute("currentProviderId", null);
    }

    /**
     * Test list page - user logged in
     */
    @Test
    void testList_LoggedIn_ShouldReturnUserTools() {
        Provider provider = new Provider();
        provider.setId(1L);
        when(session.getAttribute("currentProvider")).thenReturn(provider);

        McpTool tool = new McpTool();
        tool.setId(1L);
        tool.setToolName("Test Tool");

        @SuppressWarnings("unchecked")
        Page<McpTool> toolPage = mock(Page.class);
        when(toolPage.getRecords()).thenReturn(Collections.singletonList(tool));
        when(toolPage.getPages()).thenReturn(1L);
        when(toolPage.getTotal()).thenReturn(1L);
        when(mcpToolService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(toolPage);

        String viewName = mcpToolController.list(model, 1, 10, "all", session);

        assertEquals("mcp-tools/list", viewName);
        verify(model).addAttribute(eq("tools"), anyList());
        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("totalPages", 1L);
        verify(model).addAttribute("totalRecords", 1L);
    }

    // ========== Edit Form Tests ==========

    /**
     * Test edit form - tool not found
     */
    @Test
    void testEditForm_ToolNotFound_ShouldRedirect() {
        when(mcpToolService.getById(1L)).thenReturn(null);

        String viewName = mcpToolController.editForm(1L, model, session);

        assertTrue(viewName.contains("redirect:/mcp-tools"));
        assertTrue(viewName.contains("error"));
    }

    /**
     * Test edit form - no permission
     */
    @Test
    void testEditForm_NoPermission_ShouldRedirect() {
        McpTool tool = new McpTool();
        tool.setId(1L);
        tool.setProviderId(999L); // Different provider

        Provider currentProvider = new Provider();
        currentProvider.setId(1L);

        when(mcpToolService.getById(1L)).thenReturn(tool);
        when(session.getAttribute("currentProvider")).thenReturn(currentProvider);

        String viewName = mcpToolController.editForm(1L, model, session);

        assertTrue(viewName.contains("redirect:/mcp-tools"));
        assertTrue(viewName.contains("error"));
    }

    /**
     * Test edit form - success with HTTP converter
     */
    @Test
    void testEditForm_Success_WithHttpConverter() {
        McpTool tool = new McpTool();
        tool.setId(1L);
        tool.setProviderId(1L);
        tool.setToolNum(12345L);
        tool.setConvertType("http");

        Provider currentProvider = new Provider();
        currentProvider.setId(1L);

        OriginToolHttp httpTool = new OriginToolHttp();
        httpTool.setId(1L);

        HttpTemplateConverter httpConverter = new HttpTemplateConverter();
        httpConverter.setId(1L);

        when(mcpToolService.getById(1L)).thenReturn(tool);
        when(session.getAttribute("currentProvider")).thenReturn(currentProvider);
        when(originToolHttpMapper.selectOne(any(QueryWrapper.class))).thenReturn(httpTool);
        when(httpTemplateConverterMapper.selectOne(any(QueryWrapper.class))).thenReturn(httpConverter);

        String viewName = mcpToolController.editForm(1L, model, session);

        assertEquals("mcp-tools/form", viewName);
        verify(model).addAttribute("tool", tool);
        verify(model).addAttribute("httpTool", httpTool);
        verify(model).addAttribute("httpConverter", httpConverter);
    }

    // ========== Detail Tests ==========

    /**
     * Test detail - tool not found
     */
    @Test
    void testDetail_ToolNotFound_ShouldRedirect() {
        when(mcpToolService.getById(1L)).thenReturn(null);

        String viewName = mcpToolController.detail(1L, model);

        assertEquals("redirect:/mcp-tools", viewName);
    }

    /**
     * Test detail - success
     */
    @Test
    void testDetail_Success() {
        McpTool tool = new McpTool();
        tool.setId(1L);
        tool.setToolName("Test Tool");
        tool.setConvertType("http");

        when(mcpToolService.getById(1L)).thenReturn(tool);

        String viewName = mcpToolController.detail(1L, model);

        assertEquals("mcp-tools/detail", viewName);
        verify(model).addAttribute("tool", tool);
    }

    // ========== Save API Tests ==========

    /**
     * Test save API - user not logged in
     */
    @Test
    void testSaveApi_NotLoggedIn_ShouldReturn401() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        McpToolEditDto dto = new McpToolEditDto();
        ResponseEntity<String> response = mcpToolController.saveApi(dto, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("用户未登录", response.getBody());
    }

    /**
     * Test save API - create new tool success
     */
    @Test
    void testSaveApi_CreateNewTool_Success() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(mcpToolService.saveOrUpdateWithValidation(any(McpTool.class))).thenReturn(true);

        McpToolEditDto dto = new McpToolEditDto();
        dto.setToolName("New Tool");
        dto.setConvertType("http");

        ResponseEntity<String> response = mcpToolController.saveApi(dto, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("保存成功", response.getBody());
        verify(mcpToolService).saveOrUpdateWithValidation(any(McpTool.class));
    }

    /**
     * Test save API - update existing tool without permission
     */
    @Test
    void testSaveApi_UpdateWithoutPermission_ShouldReturn403() {
        Provider currentProvider = new Provider();
        currentProvider.setId(1L);
        currentProvider.setUsername("testuser");

        McpTool existingTool = new McpTool();
        existingTool.setId(1L);
        existingTool.setProviderId(999L); // Different provider

        when(session.getAttribute("currentProvider")).thenReturn(currentProvider);
        when(mcpToolService.getById(1L)).thenReturn(existingTool);

        McpToolEditDto dto = new McpToolEditDto();
        dto.setId(1L);
        dto.setToolName("Updated Tool");

        ResponseEntity<String> response = mcpToolController.saveApi(dto, session);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("无权限修改此工具", response.getBody());
    }

    /**
     * Test save API - update existing tool success
     */
    @Test
    void testSaveApi_UpdateExistingTool_Success() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        McpTool existingTool = new McpTool();
        existingTool.setId(1L);
        existingTool.setProviderId(1L);
        existingTool.setToolNum(12345L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(mcpToolService.getById(1L)).thenReturn(existingTool);
        when(mcpToolService.saveOrUpdateWithValidation(any(McpTool.class))).thenReturn(true);

        McpToolEditDto dto = new McpToolEditDto();
        dto.setId(1L);
        dto.setToolName("Updated Tool");

        ResponseEntity<String> response = mcpToolController.saveApi(dto, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("保存成功", response.getBody());
    }

    /**
     * Test save API - exception handling
     */
    @Test
    void testSaveApi_ExceptionHandling() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        doThrow(new RuntimeException("Validation error")).when(mcpToolService)
            .saveOrUpdateWithValidation(any(McpTool.class));

        McpToolEditDto dto = new McpToolEditDto();
        dto.setToolName("Test Tool");

        ResponseEntity<String> response = mcpToolController.saveApi(dto, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("保存失败"));
    }

    // ========== List API Tests ==========

    /**
     * Test list API - user not logged in
     */
    @Test
    void testListApi_NotLoggedIn_ShouldReturn401() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        ResponseEntity<java.util.List<McpTool>> response = mcpToolController.listApi(1, 10, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Test list API - success
     */
    @Test
    void testListApi_Success() {
        Provider provider = new Provider();
        provider.setId(1L);
        when(session.getAttribute("currentProvider")).thenReturn(provider);

        McpTool tool = new McpTool();
        tool.setId(1L);

        @SuppressWarnings("unchecked")
        Page<McpTool> toolPage = mock(Page.class);
        when(toolPage.getRecords()).thenReturn(Collections.singletonList(tool));
        when(mcpToolService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(toolPage);

        ResponseEntity<java.util.List<McpTool>> response = mcpToolController.listApi(1, 10, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    // ========== Delete Tests ==========

    /**
     * Test delete - tool not found
     */
    @Test
    void testDelete_ToolNotFound_ShouldRedirect() {
        when(mcpToolService.getById(1L)).thenReturn(null);

        String result = mcpToolController.delete(1L, session);

        assertTrue(result.contains("redirect:/mcp-tools"));
        assertTrue(result.contains("error"));
    }

    /**
     * Test delete - no permission
     */
    @Test
    void testDelete_NoPermission_ShouldRedirect() {
        McpTool tool = new McpTool();
        tool.setId(1L);
        tool.setProviderId(999L);

        Provider currentProvider = new Provider();
        currentProvider.setId(1L);

        when(mcpToolService.getById(1L)).thenReturn(tool);
        when(session.getAttribute("currentProvider")).thenReturn(currentProvider);

        String result = mcpToolController.delete(1L, session);

        assertTrue(result.contains("redirect:/mcp-tools"));
        assertTrue(result.contains("error"));
    }

    /**
     * Test delete - success
     */
    @Test
    void testDelete_Success() {
        McpTool tool = new McpTool();
        tool.setId(1L);
        tool.setProviderId(1L);

        Provider currentProvider = new Provider();
        currentProvider.setId(1L);

        when(mcpToolService.getById(1L)).thenReturn(tool);
        when(session.getAttribute("currentProvider")).thenReturn(currentProvider);
        when(mcpToolService.removeById(1L)).thenReturn(true);

        String result = mcpToolController.delete(1L, session);

        assertTrue(result.contains("redirect:/mcp-tools"));
        assertTrue(result.contains("success"));
        verify(mcpToolService).removeById(1L);
    }

    // ========== Search Tests ==========

    /**
     * Test search - user not logged in
     */
    @Test
    void testSearch_NotLoggedIn_ShouldReturnEmptyList() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        @SuppressWarnings("unchecked")
        Page<McpTool> emptyPage = mock(Page.class);
        when(emptyPage.getRecords()).thenReturn(Collections.emptyList());
        when(emptyPage.getPages()).thenReturn(0L);
        when(emptyPage.getTotal()).thenReturn(0L);
        when(mcpToolService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(emptyPage);

        String viewName = mcpToolController.search("test", model, 1, 10, session);

        assertEquals("mcp-tools/list", viewName);
        verify(model).addAttribute("keyword", "test");
    }

    /**
     * Test search - success
     */
    @Test
    void testSearch_Success() {
        Provider provider = new Provider();
        provider.setId(1L);
        when(session.getAttribute("currentProvider")).thenReturn(provider);

        McpTool tool = new McpTool();
        tool.setId(1L);
        tool.setToolName("Test Tool");

        @SuppressWarnings("unchecked")
        Page<McpTool> toolPage = mock(Page.class);
        when(toolPage.getRecords()).thenReturn(Collections.singletonList(tool));
        when(toolPage.getPages()).thenReturn(1L);
        when(toolPage.getTotal()).thenReturn(1L);
        when(mcpToolService.page(any(Page.class), any(QueryWrapper.class))).thenReturn(toolPage);

        String viewName = mcpToolController.search("test", model, 1, 10, session);

        assertEquals("mcp-tools/list", viewName);
        verify(model).addAttribute(eq("tools"), anyList());
        verify(model).addAttribute("keyword", "test");
    }

    // ========== Find By Unique ID Tests ==========

    /**
     * Test find by unique ID - success
     */
    @Test
    void testFindByUniqueId_Success() {
        McpTool tool = new McpTool();
        tool.setId(1L);
        tool.setToolName("Test Tool");

        when(mcpToolService.findByUniqueId("unique-123")).thenReturn(tool);

        ResponseEntity<McpTool> response = mcpToolController.findByUniqueId("unique-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Tool", response.getBody().getToolName());
    }

    /**
     * Test find by unique ID - not found
     */
    @Test
    void testFindByUniqueId_NotFound() {
        when(mcpToolService.findByUniqueId("nonexistent")).thenReturn(null);

        ResponseEntity<McpTool> response = mcpToolController.findByUniqueId("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Test find by unique ID - exception handling
     */
    @Test
    void testFindByUniqueId_ExceptionHandling() {
        when(mcpToolService.findByUniqueId(anyString())).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<McpTool> response = mcpToolController.findByUniqueId("unique-123");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
