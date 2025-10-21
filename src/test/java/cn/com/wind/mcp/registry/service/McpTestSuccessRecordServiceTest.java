package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.entity.*;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * McpTestSuccessRecordService集成测试类
 * 测试测试成功记录Service层的业务逻辑
 *
 * @author system
 * @date 2025-10-20
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
@ActiveProfiles("jenkins")
@DisplayName("MCP测试成功记录Service集成测试")
class McpTestSuccessRecordServiceTest {

    @Autowired
    private McpTestSuccessRecordService mcpTestSuccessRecordService;

    @Autowired
    private McpToolService mcpToolService;

    @Autowired
    private HttpTemplateConverterService httpTemplateConverterService;

    @Autowired
    private OriginToolHttpService originToolHttpService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private ExpoTemplateConverterService expoTemplateConverterService;

    @Autowired
    private OriginToolExpoService originToolExpoService;

    private McpTool testTool;
    private Provider testProvider;
    private Long testToolId;
    private String testParameters;
    private String testResult;

    /**
     * 测试前的数据准备
     * 创建测试用的MCP工具、转换器和提供者数据
     */
    @BeforeEach
    void setUp() {
        // 创建测试提供者
        testProvider = new Provider();
        testProvider.setUsername("test_user_" + System.currentTimeMillis());
        testProvider.setPassword("test_password");
        testProvider.setSalt("test_salt");
        testProvider.setEmail("test@example.com");
        testProvider.setStatus(1);
        providerService.save(testProvider);

        // 创建测试MCP工具
        testTool = new McpTool();
        long toolNum = System.currentTimeMillis();
        testTool.setToolNum(toolNum);
        testTool.setToolVersion(1L);
        testTool.setValid("1");
        testTool.setToolName("test_tool_" + toolNum);
        testTool.setToolDescription("测试工具描述");
        testTool.setNameDisplay("{\"zh\": \"测试工具\", \"en\": \"Test Tool\"}");
        testTool.setDescriptionDisplay("{\"zh\": \"这是一个测试工具\"}");
        testTool.setInputSchema("{\"type\": \"object\", \"properties\": {\"name\": {\"type\": \"string\"}}}");
        testTool.setOutputSchema("{\"type\": \"object\"}");
        testTool.setConvertType("http");
        testTool.setToolType("1");
        testTool.setProviderId(testProvider.getId());
        testTool.setCreateBy("test_user");
        mcpToolService.save(testTool);
        testToolId = testTool.getId();

        // 创建原始HTTP工具
        OriginToolHttp originToolHttp = new OriginToolHttp();
        Long providerToolNum = System.currentTimeMillis();
        originToolHttp.setProviderToolNum(providerToolNum);
        originToolHttp.setNameDisplay("测试HTTP工具");
        originToolHttp.setDescDisplay("测试HTTP工具描述");
        originToolHttp.setReqUrl("http://example.com/api/test");
        originToolHttp.setReqMethod("POST");
        originToolHttp.setProviderId(testProvider.getId());
        originToolHttpService.save(originToolHttp);

        // 创建HTTP转换器
        HttpTemplateConverter httpConverter = new HttpTemplateConverter();
        httpConverter.setToolNum(toolNum);
        httpConverter.setToolVersion(1L);
        httpConverter.setReqUrl("http://example.com/api/test");
        httpConverter.setReqMethod("POST");
        httpConverter.setReqHeaders("{\"Content-Type\": \"application/json\"}");
        httpConverter.setReqBody("{\"name\": \"{{ name }}\"}");
        httpConverter.setRespBody("{{ response }}");
        httpConverter.setProviderToolNum(providerToolNum);
        httpTemplateConverterService.save(httpConverter);

        // 准备测试参数和结果
        testParameters = "{\"name\": \"test\"}";
        testResult
                = "{\"mcp_tool_error_code\": 0, \"mcp_tool_error_msg\": \"success\", \"data\": {\"result\": \"ok\"}}";
    }

    /**
     * 测试保存测试成功记录 - 完整流程
     */
    @Test
    @DisplayName("保存测试成功记录 - 完整流程")
    void testSaveTestRecord() {
        // When: 保存测试记录
        boolean saved = mcpTestSuccessRecordService.saveTestRecord(
                testToolId,
                testParameters,
                testResult,
                testProvider.getId(),
                testProvider.getUsername()
        );

        // Then: 验证结果
        assertTrue(saved, "测试记录应该保存成功");

        // 验证记录是否真的保存到数据库
        List<McpTestSuccessRecord> records = mcpTestSuccessRecordService.lambdaQuery()
                .eq(McpTestSuccessRecord::getToolId, testToolId)
                .list();

        assertNotNull(records, "查询结果不应为null");
        assertEquals(1, records.size(), "应该有一条测试记录");

        McpTestSuccessRecord record = records.get(0);
        assertEquals(testToolId, record.getToolId(), "工具ID应该匹配");
        assertEquals(String.valueOf(testTool.getToolNum()), record.getToolNum(), "工具编号应该匹配");
        assertEquals(testTool.getToolName(), record.getToolName(), "工具名称应该匹配");
        assertEquals(String.valueOf(testTool.getToolVersion()), record.getToolVersion(), "工具版本应该匹配");
        assertEquals(testProvider.getId(), record.getOperatorId(), "操作者ID应该匹配");
        assertEquals(testProvider.getUsername(), record.getOperatorUsername(), "操作者用户名应该匹配");
        assertNotNull(record.getToolSnapshot(), "工具快照不应为null");
        assertNotNull(record.getTestTimestamp(), "测试时间戳不应为null");
    }

    /**
     * 测试保存测试成功记录 - 工具不存在
     */
    @Test
    @DisplayName("保存测试成功记录 - 工具不存在")
    void testSaveTestRecordWithNonExistentTool() {
        // When: 尝试为不存在的工具保存测试记录
        boolean saved = mcpTestSuccessRecordService.saveTestRecord(
                99999L, // 不存在的工具ID
                testParameters,
                testResult,
                testProvider.getId(),
                testProvider.getUsername()
        );

        // Then: 验证结果
        assertFalse(saved, "不存在的工具应该保存失败");
    }

    /**
     * 测试查询测试记录 - 按工具ID查询
     */
    @Test
    @DisplayName("查询测试记录 - 按工具ID查询")
    void testQueryRecordsByToolId() {
        // Given: 保存测试记录
        mcpTestSuccessRecordService.saveTestRecord(
                testToolId,
                testParameters,
                testResult,
                testProvider.getId(),
                testProvider.getUsername()
        );

        // When: 按工具ID查询
        List<McpTestSuccessRecord> records = mcpTestSuccessRecordService.lambdaQuery()
                .eq(McpTestSuccessRecord::getToolId, testToolId)
                .list();

        // Then: 验证结果
        assertNotNull(records, "查询结果不应为null");
        assertEquals(1, records.size(), "应该查询到一条记录");
    }

    /**
     * 测试查询测试记录 - 按操作者ID查询
     */
    @Test
    @DisplayName("查询测试记录 - 按操作者ID查询")
    void testQueryRecordsByOperatorId() {
        // Given: 保存测试记录
        mcpTestSuccessRecordService.saveTestRecord(
                testToolId,
                testParameters,
                testResult,
                testProvider.getId(),
                testProvider.getUsername()
        );

        // When: 按操作者ID查询
        List<McpTestSuccessRecord> records = mcpTestSuccessRecordService.lambdaQuery()
                .eq(McpTestSuccessRecord::getOperatorId, testProvider.getId())
                .list();

        // Then: 验证结果
        assertNotNull(records, "查询结果不应为null");
        assertTrue(records.size() > 0, "应该至少有一条记录");
    }

    /**
     * 测试保存多条测试记录
     */
    @Test
    @DisplayName("保存多条测试记录")
    void testSaveMultipleTestRecords() {
        // When: 保存多条测试记录
        boolean saved1 = mcpTestSuccessRecordService.saveTestRecord(
                testToolId,
                "{\"name\": \"test1\"}",
                testResult,
                testProvider.getId(),
                testProvider.getUsername()
        );

        boolean saved2 = mcpTestSuccessRecordService.saveTestRecord(
                testToolId,
                "{\"name\": \"test2\"}",
                testResult,
                testProvider.getId(),
                testProvider.getUsername()
        );

        // Then: 验证结果
        assertTrue(saved1, "第一条记录应该保存成功");
        assertTrue(saved2, "第二条记录应该保存成功");

        // 验证数据库中有两条记录
        List<McpTestSuccessRecord> records = mcpTestSuccessRecordService.lambdaQuery()
                .eq(McpTestSuccessRecord::getToolId, testToolId)
                .list();

        assertEquals(2, records.size(), "应该有两条测试记录");
    }

    /**
     * 测试HTTP工具快照内容 - 验证包含HTTP转换器和原始工具信息
     */
    @Test
    @DisplayName("HTTP工具快照应包含转换器和原始工具信息")
    void testSnapshotContainsHttpConverterInfo() {
        // When: 保存测试记录 (testTool的convertType='http')
        boolean saved = mcpTestSuccessRecordService.saveTestRecord(
                testToolId,
                testParameters,
                testResult,
                testProvider.getId(),
                testProvider.getUsername()
        );

        assertTrue(saved, "测试记录应该保存成功");

        // Then: 验证快照内容
        List<McpTestSuccessRecord> records = mcpTestSuccessRecordService.lambdaQuery()
                .eq(McpTestSuccessRecord::getToolId, testToolId)
                .list();

        assertEquals(1, records.size(), "应该有一条测试记录");

        McpTestSuccessRecord record = records.get(0);
        String snapshotJson = record.getToolSnapshot();
        assertNotNull(snapshotJson, "工具快照不应为null");

        // 解析快照JSON
        Map<String, Object> snapshot = JSONUtil.toBean(snapshotJson, Map.class);

        // 验证包含MCP工具信息
        assertTrue(snapshot.containsKey("mcpTool"), "快照应包含mcpTool");

        // 验证包含HTTP转换器信息 (关键验证点)
        assertTrue(snapshot.containsKey("httpConverter"),
                "HTTP工具快照应包含httpConverter信息");

        Map<String, Object> httpConverter = (Map<String, Object>) snapshot.get("httpConverter");
        assertNotNull(httpConverter, "httpConverter不应为null");
        assertTrue(httpConverter.containsKey("reqUrl"), "httpConverter应包含reqUrl");
        assertTrue(httpConverter.containsKey("reqMethod"), "httpConverter应包含reqMethod");
        assertTrue(httpConverter.containsKey("reqBody"), "httpConverter应包含reqBody");

        // 验证包含原始HTTP工具信息
        assertTrue(snapshot.containsKey("originHttp"),
                "HTTP工具快照应包含originHttp信息");

        System.out.println("=== HTTP工具快照验证成功 ===");
        System.out.println("快照包含的键: " + snapshot.keySet());
        System.out.println("httpConverter.reqUrl: " + httpConverter.get("reqUrl"));
        System.out.println("httpConverter.reqMethod: " + httpConverter.get("reqMethod"));
    }

    /**
     * 测试Expo工具快照内容 - 验证包含Expo转换器信息
     */
    @Test
    @DisplayName("Expo工具快照应包含转换器信息")
    void testSnapshotContainsExpoConverterInfo() {
        // Given: 创建Expo类型的MCP工具
        McpTool expoTool = new McpTool();
        long toolNum = System.currentTimeMillis() + 1000;
        expoTool.setToolNum(toolNum);
        expoTool.setToolVersion(1L);
        expoTool.setValid("1");
        expoTool.setToolName("test_expo_tool_" + toolNum);
        expoTool.setToolDescription("测试Expo工具");
        expoTool.setConvertType("2"); // Expo类型
        expoTool.setToolType("1");
        expoTool.setProviderId(testProvider.getId());
        expoTool.setCreateBy("test_user");
        mcpToolService.save(expoTool);

        // 创建Expo原始工具
        OriginToolExpo originToolExpo = new OriginToolExpo();
        Long providerToolNum = System.currentTimeMillis() + 2000;
        originToolExpo.setProviderToolNum(providerToolNum);
        originToolExpo.setNameDisplay("测试Expo工具");
        originToolExpo.setProviderId(testProvider.getId());
        originToolExpoService.save(originToolExpo);

        // 创建Expo转换器
        ExpoTemplateConverter expoConverter = new ExpoTemplateConverter();
        expoConverter.setToolNum(toolNum);
        expoConverter.setToolVersion(1L);
        expoConverter.setProviderToolNum(providerToolNum);
        expoTemplateConverterService.save(expoConverter);

        // When: 保存测试记录
        boolean saved = mcpTestSuccessRecordService.saveTestRecord(
                expoTool.getId(),
                testParameters,
                testResult,
                testProvider.getId(),
                testProvider.getUsername()
        );

        assertTrue(saved, "测试记录应该保存成功");

        // Then: 验证快照内容
        List<McpTestSuccessRecord> records = mcpTestSuccessRecordService.lambdaQuery()
                .eq(McpTestSuccessRecord::getToolId, expoTool.getId())
                .list();

        assertEquals(1, records.size(), "应该有一条测试记录");

        McpTestSuccessRecord record = records.get(0);
        String snapshotJson = record.getToolSnapshot();
        assertNotNull(snapshotJson, "工具快照不应为null");

        // 解析快照JSON
        Map<String, Object> snapshot = JSONUtil.toBean(snapshotJson, Map.class);

        // 验证包含MCP工具信息
        assertTrue(snapshot.containsKey("mcpTool"), "快照应包含mcpTool");

        // 验证包含Expo转换器信息 (关键验证点)
        assertTrue(snapshot.containsKey("expoConverter"),
                "Expo工具快照应包含expoConverter信息");

        Map<String, Object> expoConv = (Map<String, Object>) snapshot.get("expoConverter");
        assertNotNull(expoConv, "expoConverter不应为null");

        // 验证包含原始Expo工具信息
        assertTrue(snapshot.containsKey("originExpo"),
                "Expo工具快照应包含originExpo信息");

        System.out.println("=== Expo工具快照验证成功 ===");
        System.out.println("快照包含的键: " + snapshot.keySet());
    }

    /**
     * 测试Manual工具快照内容 - 验证不包含转换器信息
     */
    @Test
    @DisplayName("Manual工具快照应只包含MCP工具信息")
    void testSnapshotForManualTool() {
        // Given: 创建Manual类型的MCP工具
        McpTool manualTool = new McpTool();
        long toolNum = System.currentTimeMillis() + 3000;
        manualTool.setToolNum(toolNum);
        manualTool.setToolVersion(1L);
        manualTool.setValid("1");
        manualTool.setToolName("test_manual_tool_" + toolNum);
        manualTool.setToolDescription("测试Manual工具");
        manualTool.setConvertType("3"); // Manual类型
        manualTool.setToolType("1");
        manualTool.setProviderId(testProvider.getId());
        manualTool.setCreateBy("test_user");
        mcpToolService.save(manualTool);

        // When: 保存测试记录
        boolean saved = mcpTestSuccessRecordService.saveTestRecord(
                manualTool.getId(),
                testParameters,
                testResult,
                testProvider.getId(),
                testProvider.getUsername()
        );

        assertTrue(saved, "测试记录应该保存成功");

        // Then: 验证快照内容
        List<McpTestSuccessRecord> records = mcpTestSuccessRecordService.lambdaQuery()
                .eq(McpTestSuccessRecord::getToolId, manualTool.getId())
                .list();

        assertEquals(1, records.size(), "应该有一条测试记录");

        McpTestSuccessRecord record = records.get(0);
        String snapshotJson = record.getToolSnapshot();
        assertNotNull(snapshotJson, "工具快照不应为null");

        // 解析快照JSON
        Map<String, Object> snapshot = JSONUtil.toBean(snapshotJson, Map.class);

        // 验证包含MCP工具信息
        assertTrue(snapshot.containsKey("mcpTool"), "快照应包含mcpTool");

        // 验证不包含转换器信息 (Manual类型不需要转换器)
        assertFalse(snapshot.containsKey("httpConverter"),
                "Manual工具快照不应包含httpConverter");
        assertFalse(snapshot.containsKey("expoConverter"),
                "Manual工具快照不应包含expoConverter");

        System.out.println("=== Manual工具快照验证成功 ===");
        System.out.println("快照包含的键: " + snapshot.keySet());
        System.out.println("Manual工具正确地只包含mcpTool信息,不包含转换器信息");
    }
}
