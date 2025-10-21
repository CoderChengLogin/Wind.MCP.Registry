package cn.com.wind.mcp.registry.mapper;

import cn.com.wind.mcp.registry.entity.McpTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * McpTool Mapper 测试
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
@ActiveProfiles("jenkins")
public class McpToolMapperTest {

    @Autowired
    private McpToolMapper mcpToolMapper;

    @Test
    void testInsertMcpTool() {
        McpTool tool = new McpTool();
        // 使用时间戳生成唯一的tool_num，避免uk_tool_num_valid_version约束冲突
        tool.setToolNum(System.currentTimeMillis());
        tool.setToolName("test_tool_" + System.currentTimeMillis());
        tool.setToolDescription("This is a test tool");
        tool.setToolType("1");
        tool.setToolVersion(1L);
        tool.setValid("1");
        tool.setNameDisplay("Test Tool");
        tool.setDescriptionDisplay("测试工具");
        tool.setInputSchema("{\"type\":\"object\"}");
        tool.setOutputSchema("{\"type\":\"object\"}");
        tool.setStreamOutput("0");
        tool.setConvertType("1");
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        int result = mcpToolMapper.insert(tool);
        assertEquals(1, result);
        assertNotNull(tool.getId());
    }

    @Test
    void testSelectById() {
        // 先插入一个工具
        McpTool tool = new McpTool();
        tool.setToolNum(System.currentTimeMillis());
        tool.setToolName("test_select_" + System.currentTimeMillis());
        tool.setToolDescription("Test select tool");
        tool.setToolType("1");
        tool.setToolVersion(1L);
        tool.setValid("1");
        tool.setConvertType("1");
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        mcpToolMapper.insert(tool);
        Long id = tool.getId();

        // 查询
        McpTool selected = mcpToolMapper.selectById(id);
        assertNotNull(selected);
        assertEquals(tool.getToolName(), selected.getToolName());
        assertEquals("Test select tool", selected.getToolDescription());
        assertEquals("1", selected.getToolType());
        assertEquals(1L, selected.getToolVersion());
        assertEquals("1", selected.getValid());
    }

    @Test
    void testUpdateById() {
        // 先插入一个工具
        McpTool tool = new McpTool();
        tool.setToolNum(System.currentTimeMillis());
        tool.setToolName("test_update_" + System.currentTimeMillis());
        tool.setToolDescription("Test update tool");
        tool.setToolType("1");
        tool.setToolVersion(1L);
        tool.setValid("1");
        tool.setConvertType("1");
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        mcpToolMapper.insert(tool);
        Long id = tool.getId();

        // 更新
        tool.setToolDescription("Updated test tool");
        tool.setNameDisplay("Updated Tool");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("updater");

        int result = mcpToolMapper.updateById(tool);
        assertEquals(1, result);

        // 验证更新
        McpTool updated = mcpToolMapper.selectById(id);
        assertEquals("Updated test tool", updated.getToolDescription());
        assertEquals("Updated Tool", updated.getNameDisplay());
        assertEquals("updater", updated.getUpdateBy());
    }

    @Test
    void testDeleteById() {
        // 先插入一个工具
        McpTool tool = new McpTool();
        tool.setToolNum(System.currentTimeMillis());
        tool.setToolName("test_delete_" + System.currentTimeMillis());
        tool.setToolDescription("Test delete tool");
        tool.setToolType("1");
        tool.setToolVersion(1L);
        tool.setValid("1");
        tool.setConvertType("1");
        tool.setCreateTime(LocalDateTime.now());
        tool.setCreateBy("system");
        tool.setUpdateTime(LocalDateTime.now());
        tool.setUpdateBy("system");

        mcpToolMapper.insert(tool);
        Long id = tool.getId();

        // 删除
        int result = mcpToolMapper.deleteById(id);
        assertEquals(1, result);

        // 验证删除
        McpTool deleted = mcpToolMapper.selectById(id);
        assertNull(deleted);
    }
}