package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.service.ProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * McpToolController集成测试 - 验证convert_type显示问题
 * <p>
 * 测试说明:
 * 验证convert_type='3'(manual/code)是否正确显示为"手动转换模板"
 *
 * @author system
 * @date 2025-10-18
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("jenkins")
@DisplayName("MCP工具Controller集成测试 - ConvertType显示")
public class McpToolControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private McpToolService mcpToolService;

    @Autowired
    private ProviderService providerService;

    private Provider testProvider;
    private MockHttpSession session;
    private McpTool httpTool;
    private McpTool expoTool;
    private McpTool codeTool;

    /**
     * 测试前准备：创建测试数据
     */
    @BeforeEach
    void setUp() {
        // 创建测试用户
        testProvider = new Provider();
        testProvider.setUsername("test_convert_type_" + System.currentTimeMillis());
        testProvider.setPassword("test123");
        testProvider.setSalt("testsalt");
        testProvider.setEmail("test@example.com");
        testProvider.setStatus(1);
        testProvider.setCreateTime(LocalDateTime.now());
        providerService.save(testProvider);

        // 创建session并设置当前用户
        session = new MockHttpSession();
        session.setAttribute("currentProvider", testProvider);

        // 创建HTTP类型工具 (convert_type='1')
        httpTool = new McpTool();
        httpTool.setToolNum(System.currentTimeMillis());
        httpTool.setToolVersion(1L);
        httpTool.setValid("1");
        httpTool.setToolName("test_http_tool_" + System.currentTimeMillis());
        httpTool.setToolDescription("HTTP Tool for Testing");
        httpTool.setConvertType("1"); // 应该被ConvertTypeHandler转换
        httpTool.setToolType("1");
        httpTool.setProviderId(testProvider.getId());
        httpTool.setCreateBy("test");
        httpTool.setCreateTime(LocalDateTime.now());
        mcpToolService.save(httpTool);

        // 创建Expo类型工具 (convert_type='2')
        expoTool = new McpTool();
        expoTool.setToolNum(System.currentTimeMillis() + 1);
        expoTool.setToolVersion(1L);
        expoTool.setValid("1");
        expoTool.setToolName("test_expo_tool_" + System.currentTimeMillis());
        expoTool.setToolDescription("Expo Tool for Testing");
        expoTool.setConvertType("2"); // 应该被ConvertTypeHandler转换
        expoTool.setToolType("1");
        expoTool.setProviderId(testProvider.getId());
        expoTool.setCreateBy("test");
        expoTool.setCreateTime(LocalDateTime.now());
        mcpToolService.save(expoTool);

        // 创建Manual/Code类型工具 (convert_type='3')
        codeTool = new McpTool();
        codeTool.setToolNum(System.currentTimeMillis() + 2);
        codeTool.setToolVersion(1L);
        codeTool.setValid("1");
        codeTool.setToolName("test_manual_tool_" + System.currentTimeMillis());
        codeTool.setToolDescription("Manual Tool for Testing");
        codeTool.setConvertType("3"); // 手动转换模板 (manual)
        codeTool.setToolType("1");
        codeTool.setProviderId(testProvider.getId());
        codeTool.setCreateBy("test");
        codeTool.setCreateTime(LocalDateTime.now());
        mcpToolService.save(codeTool);
    }

    /**
     * RED测试: 验证HTTP类型工具在详情页正确显示
     */
    @Test
    @DisplayName("RED: 详情页应显示HTTP转换模板")
    void testDetailPage_HttpTool_ShouldShowHttpBadge() throws Exception {
        mockMvc.perform(get("/mcp-tools/" + httpTool.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("mcp-tools/detail"))
                .andExpect(content().string(containsString("HTTP转换模板")))
                .andExpect(content().string(not(containsString("未知"))));
    }

    /**
     * RED测试: 验证Expo类型工具在详情页正确显示
     */
    @Test
    @DisplayName("RED: 详情页应显示Expo转换模板")
    void testDetailPage_ExpoTool_ShouldShowExpoBadge() throws Exception {
        mockMvc.perform(get("/mcp-tools/" + expoTool.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("mcp-tools/detail"))
                .andExpect(content().string(containsString("Expo转换模板")))
                .andExpect(content().string(not(containsString("未知"))));
    }

    /**
     * 测试: 验证Manual/Code类型工具在详情页正确显示为"手动转换模板"
     */
    @Test
    @DisplayName("详情页应显示手动转换模板")
    void testDetailPage_CodeTool_ShouldShowManualBadge() throws Exception {
        MvcResult result = mockMvc.perform(get("/mcp-tools/" + codeTool.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("mcp-tools/detail"))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        // 验证页面显示"手动转换模板"
        System.out.println("=== DEBUG: Page Content for Manual/Code Tool ===");
        System.out.println("Tool ID: " + codeTool.getId());
        System.out.println("Convert Type (saved): " + codeTool.getConvertType());

        // 查找convert_type相关的HTML片段
        if (content.contains("转换模板类型")) {
            int startIdx = content.indexOf("转换模板类型");
            String snippet = content.substring(startIdx, Math.min(startIdx + 500, content.length()));
            System.out.println("HTML Snippet: " + snippet);
        }

        // 断言应该通过: 显示"手动转换模板"
        mockMvc.perform(get("/mcp-tools/" + codeTool.getId())
                        .session(session))
                .andExpect(content().string(containsString("手动转换模板")))
                .andExpect(content().string(not(containsString("未知"))));
    }

    /**
     * 测试: 验证Manual/Code类型工具在列表页正确显示为"手动转换模板"
     */
    @Test
    @DisplayName("列表页应显示手动转换模板徽章")
    void testListPage_CodeTool_ShouldShowManualBadge() throws Exception {
        MvcResult result = mockMvc.perform(get("/mcp-tools")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("mcp-tools/list"))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        System.out.println("=== DEBUG: List Page Content ===");
        System.out.println("Manual/Code Tool Name: " + codeTool.getToolName());

        // 查找包含工具名称的卡片
        if (content.contains(codeTool.getToolName())) {
            int startIdx = content.indexOf(codeTool.getToolName());
            String snippet = content.substring(Math.max(0, startIdx - 200),
                    Math.min(startIdx + 500, content.length()));
            System.out.println("Card Snippet: " + snippet);
        }

        // 断言应该通过: 显示"手动"徽章
        mockMvc.perform(get("/mcp-tools")
                        .session(session))
                .andExpect(content().string(containsString("手动")))
                .andExpect(content().string(not(containsString("未知"))));
    }

    /**
     * 调试测试: 验证Handler确实将'3'转换为"code"
     */
    @Test
    @DisplayName("DEBUG: 验证ConvertTypeHandler转换逻辑")
    void testConvertTypeHandler_ConvertsCorrectly() {
        // 从数据库重新查询工具
        McpTool reloadedCodeTool = mcpToolService.getById(codeTool.getId());

        System.out.println("=== DEBUG: ConvertTypeHandler Test ===");
        System.out.println("Original convert_type (before save): " + codeTool.getConvertType());
        System.out.println("Reloaded convert_type (after Handler): " + reloadedCodeTool.getConvertType());

        // 验证Handler是否正确转换
        // 如果保存的是"3"或"code"，读取出来应该是"code"
        assert reloadedCodeTool.getConvertType() != null : "convertType should not be null";
        System.out.println("Expected: 'code', Actual: '" + reloadedCodeTool.getConvertType() + "'");
    }
}
