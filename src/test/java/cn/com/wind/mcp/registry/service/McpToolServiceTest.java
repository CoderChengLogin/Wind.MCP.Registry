package cn.com.wind.mcp.registry.service;

import java.util.List;

import cn.com.wind.mcp.registry.entity.McpTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * McpToolServiceImpl集成测试类
 * 测试MCP工具Service层的业务逻辑
 *
 * @author system
 * @date 2025-01-14
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
@ActiveProfiles("jenkins")
@DisplayName("MCP工具Service集成测试")
class McpToolServiceTest {

    @Autowired
    private McpToolService mcpToolService;

    private McpTool testTool;

    /**
     * 测试前的数据准备
     */
    @BeforeEach
    void setUp() {
        testTool = new McpTool();
        testTool.setToolNum(1001L);
        testTool.setToolVersion(1L);
        testTool.setValid("1");
        testTool.setToolName("test_tool");
        testTool.setToolDescription("测试工具描述");
        testTool.setNameDisplay("{\"zh\": \"测试工具\", \"en\": \"Test Tool\"}");
        testTool.setDescriptionDisplay("{\"zh\": \"这是一个测试工具\"}");
        testTool.setInputSchema("{\"type\": \"object\"}");
        testTool.setConvertType("1");
        testTool.setToolType("1");
        testTool.setProviderId(100L);
        testTool.setCreateBy("test_user");
    }

    /**
     * 测试搜索工具功能 - 按工具名称搜索
     */
    @Test
    @DisplayName("搜索工具 - 按工具名称")
    void testSearchToolsByName() {
        // Given: 保存测试工具
        mcpToolService.save(testTool);

        // When: 执行搜索
        List<McpTool> result = mcpToolService.searchTools("test");

        // Then: 验证结果
        assertNotNull(result, "搜索结果不应为null");
        assertTrue(result.size() > 0, "应该能搜索到工具");
    }

    /**
     * 测试搜索工具功能 - 空关键字返回所有工具
     */
    @Test
    @DisplayName("搜索工具 - 空关键字返回所有工具")
    void testSearchToolsWithEmptyKeyword() {
        // Given: 保存测试工具
        mcpToolService.save(testTool);

        // When: 使用空关键字搜索
        List<McpTool> result = mcpToolService.searchTools("");

        // Then: 验证结果
        assertNotNull(result, "搜索结果不应为null");
    }

    /**
     * 测试根据创建人统计工具数量
     */
    @Test
    @DisplayName("统计指定创建人的工具数量")
    void testCountByCreateBy() {
        // Given: 保存测试工具
        mcpToolService.save(testTool);

        // When: 执行统计
        long count = mcpToolService.countByCreateBy("test_user");

        // Then: 验证结果
        assertTrue(count > 0, "应该能统计到工具");
    }

    /**
     * 测试根据提供者ID统计工具数量
     */
    @Test
    @DisplayName("统计指定提供者的工具数量")
    void testCountByProviderId() {
        // Given: 保存测试工具
        mcpToolService.save(testTool);

        // When: 执行统计
        long count = mcpToolService.countByProviderId(100L);

        // Then: 验证结果
        assertTrue(count > 0, "应该能统计到工具");
    }

    /**
     * 测试保存工具
     */
    @Test
    @DisplayName("保存工具")
    void testSaveTool() {
        // When: 保存工具
        boolean saved = mcpToolService.save(testTool);

        // Then: 验证结果
        assertTrue(saved, "工具应该保存成功");
        assertNotNull(testTool.getId(), "保存后应该有ID");
    }

    /**
     * 测试查询所有工具
     */
    @Test
    @DisplayName("查询所有工具")
    void testListAllTools() {
        // Given: 保存测试工具
        mcpToolService.save(testTool);

        // When: 查询所有工具
        List<McpTool> tools = mcpToolService.list();

        // Then: 验证结果
        assertNotNull(tools, "工具列表不应为null");
        assertTrue(tools.size() > 0, "应该至少有一个工具");
    }
}
