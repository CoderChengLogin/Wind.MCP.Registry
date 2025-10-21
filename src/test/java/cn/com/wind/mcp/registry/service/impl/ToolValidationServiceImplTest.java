package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.mapper.McpToolMapper;
import cn.com.wind.mcp.registry.mapper.OriginToolHttpMapper;
import cn.com.wind.mcp.registry.service.ToolValidationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ToolValidationServiceImplTest {

    private ToolValidationServiceImpl service;
    private McpToolMapper mcpToolMapper;
    private OriginToolHttpMapper originToolHttpMapper;

    @BeforeEach
    void setUp() {
        mcpToolMapper = Mockito.mock(McpToolMapper.class);
        originToolHttpMapper = Mockito.mock(OriginToolHttpMapper.class);
        service = new ToolValidationServiceImpl();
        // 注入mock依赖
        service.mcpToolMapper = mcpToolMapper;
        service.originToolHttpMapper = originToolHttpMapper;
    }

    @Test
    void testValidateMcpTool_BasicCheck() {
        // 工具名称为空
        McpTool tool = new McpTool();
        tool.setToolName("");
        ToolValidationService.ValidationResult result = service.validateMcpTool(tool);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("不能为空"));

        // 工具名称非法
        tool.setToolName("$$$");
        result = service.validateMcpTool(tool);
        assertFalse(result.isValid());

        // 工具类型为空
        tool.setToolName("valid_name");
        tool.setToolType(null);
        result = service.validateMcpTool(tool);
        assertFalse(result.isValid());

        // 工具描述为空
        tool.setToolType("type");
        tool.setToolDescription("");
        result = service.validateMcpTool(tool);
        assertFalse(result.isValid());

        // 工具描述过短
        tool.setToolDescription("short");
        result = service.validateMcpTool(tool);
        assertFalse(result.isValid());

        // 工具描述过长
        tool.setToolDescription("a".repeat(2001));
        result = service.validateMcpTool(tool);
        assertFalse(result.isValid());
    }

    @Test
    void testValidateMcpTool_UniquenessAndSuccess() {
        McpTool tool = new McpTool();
        tool.setToolName("validname_123");
        tool.setToolType("someType");
        tool.setToolDescription("这是一个合法的工具描述，abcdefg123456789");
        tool.setToolVersion(1L);
        // 模拟Mapper未查到同名同版本
        Mockito.when(mcpToolMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        ToolValidationService.ValidationResult result = service.validateMcpTool(tool);
        assertTrue(result.isValid());
        assertEquals("验证通过", result.getMessage());
        assertNotNull(result.getUniqueId());
        assertTrue(result.getUniqueId().length() > 0);

        // 查询到已存在
        Mockito.when(mcpToolMapper.selectOne(any(QueryWrapper.class))).thenReturn(new McpTool());
        result = service.validateMcpTool(tool);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("已存在"));
    }

    @Test
    void testValidateHttpTool_BasicCheck() {
        OriginToolHttp tool = new OriginToolHttp();
        tool.setName("");
        ToolValidationService.ValidationResult vr = service.validateHttpTool(tool);
        assertFalse(vr.isValid());

        tool.setName("!!illegalName");
        vr = service.validateHttpTool(tool);
        assertFalse(vr.isValid());

        tool.setName("legalname");
        tool.setType(null);
        vr = service.validateHttpTool(tool);
        assertFalse(vr.isValid());

        tool.setType("http");
        tool.setDescription("");
        vr = service.validateHttpTool(tool);
        assertFalse(vr.isValid());

        tool.setDescription("short");
        vr = service.validateHttpTool(tool);
        assertFalse(vr.isValid());

        tool.setDescription("a".repeat(2001));
        vr = service.validateHttpTool(tool);
        assertFalse(vr.isValid());

        tool.setDescription("这是一个合格的描述内容1234567890");
        // HTTP方法/URL为空
        tool.setMethod("");
        vr = service.validateHttpTool(tool);
        assertFalse(vr.isValid());
        tool.setMethod("GET");
        tool.setUrl("");
        vr = service.validateHttpTool(tool);
        assertFalse(vr.isValid());

        // 算法不合法的URL
        tool.setUrl("wrong-url");
        vr = service.validateHttpTool(tool);
        assertFalse(vr.isValid());
    }

    @Test
    void testValidateHttpTool_UniquenessAndSuccess() {
        OriginToolHttp tool = new OriginToolHttp();
        tool.setName("legal_name1");
        tool.setType("type1");
        tool.setDescription("这是一个足够长的工具描述1111111111");
        tool.setMethod("POST");
        tool.setUrl("https://test.url/abc");
        Mockito.when(originToolHttpMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        ToolValidationService.ValidationResult vr = service.validateHttpTool(tool);
        assertTrue(vr.isValid());
        assertNotNull(vr.getUniqueId());

        // 查询到已存在
        Mockito.when(originToolHttpMapper.selectOne(any(QueryWrapper.class))).thenReturn(new OriginToolHttp());
        vr = service.validateHttpTool(tool);
        assertFalse(vr.isValid());
        assertTrue(vr.getMessage().contains("已存在"));
    }

    @Test
    void testGenerateUniqueIdentifier() {
        String id = service.generateUniqueIdentifier("toolA", "1.2.3", "typeT");
        assertNotNull(id);
        assertEquals(16, id.length());
        assertTrue(id.matches("[A-Z0-9]+"));
    }

    @Test
    void testValidateToolName() {
        assertFalse(service.validateToolName(null));
        assertFalse(service.validateToolName(""));
        assertFalse(service.validateToolName("!badname"));
        assertTrue(service.validateToolName("good_name-123"));
        assertFalse(service.validateToolName("ab")); // 少于3
        assertFalse(service.validateToolName("a".repeat(51))); // 多于50
    }

    @Test
    void testValidateVersion() {
        assertFalse(service.validateVersion(null));
        assertFalse(service.validateVersion(""));
        assertFalse(service.validateVersion("1.2"));
        assertTrue(service.validateVersion("1.0.0"));
        assertTrue(service.validateVersion("2.3.4-beta1"));
        assertFalse(service.validateVersion("x.y.z"));
    }
}