package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.service.ToolValidationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * McpToolServiceImpl的单元测试类
 * <p>
 * 使用轻量级Mock方式测试Service层业务逻辑
 * </p>
 *
 * @author system
 * @date 2025-10-14
 */
class McpToolServiceImplTest {

    private McpToolServiceImpl service;
    private ToolValidationService toolValidationService;

    @BeforeEach
    void setUp() {
        service = spy(new McpToolServiceImpl());
        toolValidationService = mock(ToolValidationService.class);
        // 使用反射注入private字段
        ReflectionTestUtils.setField(service, "toolValidationService", toolValidationService);
    }

    /**
     * 测试搜索工具 - 无关键字
     */
    @Test
    void testSearchTools_NoKeyword() {
        McpTool tool1 = new McpTool();
        tool1.setToolName("tool1");
        McpTool tool2 = new McpTool();
        tool2.setToolName("tool2");
        List<McpTool> expected = Arrays.asList(tool1, tool2);

        doReturn(expected).when(service).list(any(QueryWrapper.class));

        List<McpTool> result = service.searchTools(null);
        assertNotNull(result);
        assertEquals(2, result.size());

        result = service.searchTools("");
        assertNotNull(result);
    }

    /**
     * 测试搜索工具 - 有关键字
     */
    @Test
    void testSearchTools_WithKeyword() {
        McpTool tool = new McpTool();
        tool.setToolName("test_tool");
        List<McpTool> expected = Arrays.asList(tool);

        doReturn(expected).when(service).list(any(QueryWrapper.class));

        List<McpTool> result = service.searchTools("test");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test_tool", result.get(0).getToolName());
    }

    /**
     * 测试保存或更新工具 - 验证失败
     */
    @Test
    void testSaveOrUpdateWithValidation_ValidationFailed() {
        McpTool tool = new McpTool();
        tool.setToolName("invalid_tool");

        ToolValidationService.ValidationResult failResult =
                new ToolValidationService.ValidationResult(false, "工具名称无效", null);
        when(toolValidationService.validateMcpTool(any(McpTool.class))).thenReturn(failResult);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.saveOrUpdateWithValidation(tool);
        });

        assertTrue(exception.getMessage().contains("工具名称无效"));
    }

    /**
     * 测试保存或更新工具 - 验证成功,无uniqueId
     */
    @Test
    void testSaveOrUpdateWithValidation_Success_NoUniqueId() {
        McpTool tool = new McpTool();
        tool.setToolName("valid_tool");

        ToolValidationService.ValidationResult successResult =
                new ToolValidationService.ValidationResult(true, "验证通过", null);
        when(toolValidationService.validateMcpTool(any(McpTool.class))).thenReturn(successResult);
        doReturn(true).when(service).saveOrUpdate(any(McpTool.class));

        boolean result = service.saveOrUpdateWithValidation(tool);
        assertTrue(result);
    }

    /**
     * 测试保存或更新工具 - 验证成功,有uniqueId,schema为null
     */
    @Test
    void testSaveOrUpdateWithValidation_Success_WithUniqueId_NullSchema() {
        McpTool tool = new McpTool();
        tool.setToolName("valid_tool");
        tool.setInputSchema(null);

        ToolValidationService.ValidationResult successResult =
                new ToolValidationService.ValidationResult(true, "验证通过", "UNIQUE123");
        when(toolValidationService.validateMcpTool(any(McpTool.class))).thenReturn(successResult);
        doReturn(true).when(service).saveOrUpdate(any(McpTool.class));

        boolean result = service.saveOrUpdateWithValidation(tool);
        assertTrue(result);
        assertNotNull(tool.getInputSchema());
        assertTrue(tool.getInputSchema().contains("UNIQUE123"));
    }

    /**
     * 测试保存或更新工具 - 验证成功,有uniqueId,schema不含uniqueId
     */
    @Test
    void testSaveOrUpdateWithValidation_Success_WithUniqueId_SchemaWithoutUniqueId() {
        McpTool tool = new McpTool();
        tool.setToolName("valid_tool");
        tool.setInputSchema("{\"type\":\"object\"}");

        ToolValidationService.ValidationResult successResult =
                new ToolValidationService.ValidationResult(true, "验证通过", "UNIQUE456");
        when(toolValidationService.validateMcpTool(any(McpTool.class))).thenReturn(successResult);
        doReturn(true).when(service).saveOrUpdate(any(McpTool.class));

        boolean result = service.saveOrUpdateWithValidation(tool);
        assertTrue(result);
        assertTrue(tool.getInputSchema().contains("UNIQUE456"));
    }

    /**
     * 测试保存或更新工具 - 验证成功,schema已包含uniqueId
     */
    @Test
    void testSaveOrUpdateWithValidation_Success_SchemaAlreadyHasUniqueId() {
        McpTool tool = new McpTool();
        tool.setToolName("valid_tool");
        tool.setInputSchema("{\"uniqueId\":\"OLD123\"}");

        ToolValidationService.ValidationResult successResult =
                new ToolValidationService.ValidationResult(true, "验证通过", "NEW456");
        when(toolValidationService.validateMcpTool(any(McpTool.class))).thenReturn(successResult);
        doReturn(true).when(service).saveOrUpdate(any(McpTool.class));

        boolean result = service.saveOrUpdateWithValidation(tool);
        assertTrue(result);
        // schema已包含uniqueId,不会再添加
        assertTrue(tool.getInputSchema().contains("uniqueId"));
    }

    /**
     * 测试根据uniqueId查找工具 - 找到
     */
    @Test
    void testFindByUniqueId_Found() {
        McpTool expected = new McpTool();
        expected.setId(1L);
        expected.setToolName("found_tool");

        doReturn(expected).when(service).getOne(any(QueryWrapper.class));

        McpTool result = service.findByUniqueId("UNIQUE789");
        assertNotNull(result);
        assertEquals("found_tool", result.getToolName());
    }

    /**
     * 测试根据uniqueId查找工具 - 未找到
     */
    @Test
    void testFindByUniqueId_NotFound() {
        doReturn(null).when(service).getOne(any(QueryWrapper.class));

        McpTool result = service.findByUniqueId("NOTEXIST");
        assertNull(result);
    }

    /**
     * 测试统计用户创建的工具数量
     */
    @Test
    void testCountByCreateBy() {
        doReturn(5L).when(service).count(any(QueryWrapper.class));

        long count = service.countByCreateBy("testuser");
        assertEquals(5L, count);
    }

    /**
     * 测试统计提供者的工具数量
     */
    @Test
    void testCountByProviderId() {
        doReturn(10L).when(service).count(any(QueryWrapper.class));

        long count = service.countByProviderId(123L);
        assertEquals(10L, count);
    }
}
