package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.dto.McpToolExportDto;
import cn.com.wind.mcp.registry.dto.McpToolImportValidationResult;
import cn.com.wind.mcp.registry.dto.mcptool.McpToolDTO;
import cn.com.wind.mcp.registry.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Autowired
    private ProviderService providerService;

    private McpTool testTool;
    private Provider testProvider;
    private Long testProviderID;
    private String testUsername;

    /**
     * 测试前的数据准备
     * 使用时间戳生成唯一的tool_num，避免唯一约束冲突
     */
    @BeforeEach
    void setUp() {
        // 创建测试提供者
        testProvider = new Provider();
        testUsername = "test_mcp_" + System.currentTimeMillis();
        testProvider.setUsername(testUsername);
        testProvider.setPassword("test123");
        testProvider.setSalt("testsalt");
        testProvider.setEmail("test@example.com");
        testProvider.setStatus(1);
        testProvider.setCreateTime(LocalDateTime.now());
        providerService.save(testProvider);
        testProviderID = testProvider.getId();

        testTool = new McpTool();
        // 使用时间戳生成唯一的tool_num，避免uk_tool_num_valid_version约束冲突
        testTool.setToolNum(System.currentTimeMillis());
        testTool.setToolVersion(1L);
        testTool.setValid("1");
        testTool.setToolName("test_tool_" + System.currentTimeMillis());
        testTool.setToolDescription("测试工具描述");
        testTool.setNameDisplay("{\"zh\": \"测试工具\", \"en\": \"Test Tool\"}");
        testTool.setDescriptionDisplay("{\"zh\": \"这是一个测试工具\"}");
        testTool.setInputSchema("{\"type\": \"object\"}");
        testTool.setConvertType("1");
        testTool.setToolType("1");
        testTool.setProviderId(testProviderID);
        testTool.setCreateBy(testUsername);
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
        long count = mcpToolService.countByCreateBy(testUsername);

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
        long count = mcpToolService.countByProviderId(testProviderID);

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

    // ========== 新增测试: DTO转换和导入功能 ==========

    /**
     * 测试: getMcpToolById - 成功获取
     */
    @Test
    @DisplayName("根据ID获取MCP工具DTO - 成功")
    void testGetMcpToolById_Success() {
        // Given: 保存测试数据
        mcpToolService.save(testTool);

        // When: 执行测试
        McpToolDTO result = mcpToolService.getMcpToolById(testTool.getId());

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(testTool.getToolName(), result.getToolName());
        assertEquals(testTool.getToolDescription(), result.getToolDescription());
    }

    /**
     * 测试: getMcpToolById - 工具不存在
     */
    @Test
    @DisplayName("根据ID获取MCP工具DTO - 不存在")
    void testGetMcpToolById_NotFound() {
        // When & Then: 验证异常抛出
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mcpToolService.getMcpToolById(99999L);
        });
        assertTrue(exception.getMessage().contains("找不到ID"));
    }

    /**
     * 测试: getMcpToolsByNumValid - 根据toolNum和valid查询
     */
    @Test
    @DisplayName("根据toolNum和valid查询工具列表")
    void testGetMcpToolsByNumValid() {
        // Given: 准备测试数据
        Long toolNum = System.currentTimeMillis();
        testTool.setToolNum(toolNum);
        testTool.setToolVersion(1L);
        mcpToolService.save(testTool);

        McpTool tool2 = new McpTool();
        tool2.setToolNum(toolNum);
        tool2.setToolVersion(2L);
        tool2.setValid("1");
        tool2.setToolName("test_v2_" + System.currentTimeMillis());
        tool2.setToolDescription("Version 2");
        tool2.setProviderId(testProviderID);
        tool2.setCreateBy(testUsername);
        tool2.setConvertType("1");
        tool2.setToolType("1");
        mcpToolService.save(tool2);

        // When: 执行测试
        List<McpToolDTO> results = mcpToolService.getMcpToolsByNumValid(toolNum, "1");

        // Then: 验证结果
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    /**
     * 测试: getAllMcpTools - 获取所有工具
     */
    @Test
    @DisplayName("获取所有MCP工具DTO")
    void testGetAllMcpTools() {
        // Given: 保存测试数据
        mcpToolService.save(testTool);

        // When: 执行测试
        List<McpToolDTO> results = mcpToolService.getAllMcpTools();

        // Then: 验证结果
        assertNotNull(results);
        assertTrue(results.size() >= 1);
    }

    /**
     * 测试: validateImportData - 验证成功(HTTP类型)
     */
    @Test
    @DisplayName("验证导入数据 - HTTP类型工具完整数据")
    void testValidateImportData_HttpTool_Success() {
        // Given: 准备测试数据
        McpToolExportDto exportDto = createHttpToolExportDto();

        // When: 执行验证
        McpToolImportValidationResult result = mcpToolService.validateImportData(exportDto, testProviderID);

        // Then: 验证结果
        assertTrue(result.isValid(), "HTTP工具数据应验证通过");
        assertTrue(result.getErrors().isEmpty(), "不应有错误信息");
    }

    /**
     * 测试: validateImportData - MCP工具名称为空
     */
    @Test
    @DisplayName("验证导入数据 - MCP工具名称为空")
    void testValidateImportData_EmptyToolName() {
        // Given
        McpToolExportDto exportDto = createHttpToolExportDto();
        exportDto.getMcpTool().setToolName("");

        // When
        McpToolImportValidationResult result = mcpToolService.validateImportData(exportDto, testProviderID);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("工具英文名不能为空")));
    }

    /**
     * 测试: validateImportData - MCP工具名称格式错误
     */
    @Test
    @DisplayName("验证导入数据 - 工具名称格式错误")
    void testValidateImportData_InvalidToolNameFormat() {
        // Given
        McpToolExportDto exportDto = createHttpToolExportDto();
        exportDto.getMcpTool().setToolName("invalid-tool-name"); // 包含非法字符'-'

        // When
        McpToolImportValidationResult result = mcpToolService.validateImportData(exportDto, testProviderID);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("格式不正确")));
    }

    /**
     * 测试: validateImportData - 工具名称已存在
     */
    @Test
    @DisplayName("验证导入数据 - 工具名称已存在")
    void testValidateImportData_DuplicateToolName() {
        // Given: 先保存一个工具
        testTool.setToolName("duplicate_tool_name");
        mcpToolService.save(testTool);

        // 尝试导入同名工具
        McpToolExportDto exportDto = createHttpToolExportDto();
        exportDto.getMcpTool().setToolName("duplicate_tool_name");

        // When
        McpToolImportValidationResult result = mcpToolService.validateImportData(exportDto, testProviderID);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("已存在")));
    }

    /**
     * 测试: validateImportData - 无效的convertType
     */
    @Test
    @DisplayName("验证导入数据 - 无效的convertType")
    void testValidateImportData_InvalidConvertType() {
        // Given
        McpToolExportDto exportDto = createHttpToolExportDto();
        exportDto.getMcpTool().setConvertType("invalid_type");

        // When
        McpToolImportValidationResult result = mcpToolService.validateImportData(exportDto, testProviderID);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("转换类型") && e.contains("无效")));
    }

    /**
     * 测试: validateImportData - HTTP工具缺少转换模板
     */
    @Test
    @DisplayName("验证导入数据 - HTTP工具缺少转换模板")
    void testValidateImportData_HttpToolMissingConverter() {
        // Given
        McpToolExportDto exportDto = createHttpToolExportDto();
        exportDto.setHttpTemplateConverter(null); // 移除转换模板

        // When
        McpToolImportValidationResult result = mcpToolService.validateImportData(exportDto, testProviderID);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("HTTP转换模板")));
    }

    /**
     * 测试: validateImportData - 无效的JSON Schema
     */
    @Test
    @DisplayName("验证导入数据 - 无效的JSON Schema")
    void testValidateImportData_InvalidJsonSchema() {
        // Given
        McpToolExportDto exportDto = createHttpToolExportDto();
        exportDto.getMcpTool().setInputSchema("{invalid json}"); // 无效的JSON

        // When
        McpToolImportValidationResult result = mcpToolService.validateImportData(exportDto, testProviderID);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("JSON格式")));
    }

    /**
     * 测试: importTool - 导入HTTP类型工具成功
     */
    @Test
    @DisplayName("导入工具 - HTTP类型完整数据")
    void testImportTool_HttpTool_Success() {
        // Given: 准备测试数据
        McpToolExportDto exportDto = createHttpToolExportDto();

        // When: 执行导入
        McpTool imported = mcpToolService.importTool(exportDto, testProviderID, testUsername);

        // Then: 验证结果
        assertNotNull(imported);
        assertNotNull(imported.getId());
        assertNotNull(imported.getToolNum());
        assertEquals(testProviderID, imported.getProviderId());
        assertEquals(testUsername, imported.getCreateBy());
        assertEquals("1", imported.getValid());
        assertEquals(1L, imported.getToolVersion());
    }

    /**
     * 测试: importTool - 导入Expo类型工具
     */
    @Test
    @DisplayName("导入工具 - Expo类型完整数据")
    void testImportTool_ExpoTool_Success() {
        // Given: 准备测试数据
        McpToolExportDto exportDto = createExpoToolExportDto();

        // When: 执行导入
        McpTool imported = mcpToolService.importTool(exportDto, testProviderID, testUsername);

        // Then: 验证结果
        assertNotNull(imported);
        assertNotNull(imported.getId());
        assertEquals("2", imported.getConvertType());
        assertEquals(testProviderID, imported.getProviderId());
    }

    /**
     * 测试: importTool - 导入Code类型工具(仅MCP工具,无转换器)
     */
    @Test
    @DisplayName("导入工具 - Code类型工具")
    void testImportTool_CodeTool_Success() {
        // Given: 准备测试数据
        McpToolExportDto exportDto = createCodeToolExportDto();

        // When: 执行导入
        McpTool imported = mcpToolService.importTool(exportDto, testProviderID, testUsername);

        // Then: 验证结果
        assertNotNull(imported);
        assertEquals("3", imported.getConvertType());
    }

    // ========== 辅助方法 ==========

    /**
     * 创建HTTP工具导出DTO
     */
    private McpToolExportDto createHttpToolExportDto() {
        McpToolExportDto dto = new McpToolExportDto();

        // MCP工具
        McpTool mcpTool = new McpTool();
        mcpTool.setToolName("import_http_tool_" + System.currentTimeMillis());
        mcpTool.setToolDescription("Imported HTTP tool");
        mcpTool.setConvertType("1"); // HTTP
        mcpTool.setToolType("1");
        mcpTool.setInputSchema("{\"type\":\"object\",\"properties\":{\"param\":\"string\"}}");
        mcpTool.setOutputSchema("{\"type\":\"object\"}");
        dto.setMcpTool(mcpTool);

        // 原始HTTP工具(可选)
        OriginToolHttp httpTool = new OriginToolHttp();
        httpTool.setNameDisplay("Test HTTP Tool");
        httpTool.setDescDisplay("Test description");
        httpTool.setReqUrl("http://example.com/api");
        httpTool.setReqMethod("POST");
        dto.setOriginToolHttp(httpTool);

        // HTTP转换模板(必需)
        HttpTemplateConverter httpConverter = new HttpTemplateConverter();
        httpConverter.setReqUrl("http://example.com/api");
        httpConverter.setReqMethod("POST");
        httpConverter.setReqHeaders("{\"Content-Type\":\"application/json\"}");
        httpConverter.setReqBody("{\"param\":\"{{param}}\"}");
        httpConverter.setRespBody("{{response}}");
        dto.setHttpTemplateConverter(httpConverter);

        return dto;
    }

    /**
     * 创建Expo工具导出DTO
     */
    private McpToolExportDto createExpoToolExportDto() {
        McpToolExportDto dto = new McpToolExportDto();

        // MCP工具
        McpTool mcpTool = new McpTool();
        mcpTool.setToolName("import_expo_tool_" + System.currentTimeMillis());
        mcpTool.setToolDescription("Imported Expo tool");
        mcpTool.setConvertType("2"); // Expo
        mcpTool.setToolType("1");
        mcpTool.setInputSchema("{\"type\":\"object\"}");
        dto.setMcpTool(mcpTool);

        // 原始Expo工具(可选)
        OriginToolExpo expoTool = new OriginToolExpo();
        expoTool.setNameDisplay("Test Expo Tool");
        expoTool.setDescDisplay("Test description");
        expoTool.setAppClass(1);
        expoTool.setCommandId(100);
        dto.setOriginToolExpo(expoTool);

        // Expo转换模板(必需)
        ExpoTemplateConverter expoConverter = new ExpoTemplateConverter();
        expoConverter.setAppClass(1);
        expoConverter.setCommandId(100);
        expoConverter.setInputArgs("{{input}}");
        expoConverter.setOutputArgs("{{output}}");
        dto.setExpoTemplateConverter(expoConverter);

        return dto;
    }

    /**
     * 创建Code工具导出DTO
     */
    private McpToolExportDto createCodeToolExportDto() {
        McpToolExportDto dto = new McpToolExportDto();

        // MCP工具
        McpTool mcpTool = new McpTool();
        mcpTool.setToolName("import_code_tool_" + System.currentTimeMillis());
        mcpTool.setToolDescription("Imported Code tool");
        mcpTool.setConvertType("3"); // Code
        mcpTool.setToolType("1");
        mcpTool.setInputSchema("{\"type\":\"object\"}");
        dto.setMcpTool(mcpTool);

        return dto;
    }
}
