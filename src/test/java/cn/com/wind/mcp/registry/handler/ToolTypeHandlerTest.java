package cn.com.wind.mcp.registry.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ToolTypeHandler 单元测试
 * 测试数据库CHAR(1)值和业务逻辑String值之间的转换
 *
 * @author system
 * @date 2025-10-17
 */
class ToolTypeHandlerTest {

    private ToolTypeHandler handler;
    private PreparedStatement mockPs;
    private ResultSet mockRs;
    private CallableStatement mockCs;

    @BeforeEach
    void setUp() {
        handler = new ToolTypeHandler();
        mockPs = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);
        mockCs = mock(CallableStatement.class);
    }

    /**
     * 测试setNonNullParameter - 业务值"1"保持为"1"
     */
    @Test
    void testSetNonNullParameter_1() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "1", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试setNonNullParameter - 业务值"2"保持为"2"
     */
    @Test
    void testSetNonNullParameter_2() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "2", JdbcType.CHAR);

        verify(mockPs).setString(1, "2");
    }

    /**
     * 测试setNonNullParameter - 业务值"tool"转换为"1"
     */
    @Test
    void testSetNonNullParameter_Tool() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "tool", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试setNonNullParameter - 业务值"agent"转换为"2"
     */
    @Test
    void testSetNonNullParameter_Agent() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "agent", JdbcType.CHAR);

        verify(mockPs).setString(1, "2");
    }

    /**
     * 测试setNonNullParameter - 大写"TOOL"转换为"1"
     */
    @Test
    void testSetNonNullParameter_UpperCaseTool() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "TOOL", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试setNonNullParameter - 混合大小写"AgEnT"转换为"2"
     */
    @Test
    void testSetNonNullParameter_MixedCaseAgent() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "AgEnT", JdbcType.CHAR);

        verify(mockPs).setString(1, "2");
    }

    /**
     * 测试setNonNullParameter - 带空格的"  tool  "转换为"1"
     */
    @Test
    void testSetNonNullParameter_WithSpaces() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "  tool  ", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试setNonNullParameter - 未知值默认转换为"1"
     */
    @Test
    void testSetNonNullParameter_UnknownValue() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "unknown", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试setNonNullParameter - 空字符串默认转换为"1"
     */
    @Test
    void testSetNonNullParameter_EmptyString() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试setNonNullParameter - 只有空格的字符串默认转换为"1"
     */
    @Test
    void testSetNonNullParameter_OnlySpaces() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "   ", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试getNullableResult (按列名) - "1"保持为"1"
     */
    @Test
    void testGetNullableResult_ByColumnName_1() throws SQLException {
        when(mockRs.getString("tool_type")).thenReturn("1");

        String result = handler.getNullableResult(mockRs, "tool_type");

        assertEquals("1", result);
    }

    /**
     * 测试getNullableResult (按列名) - "2"保持为"2"
     */
    @Test
    void testGetNullableResult_ByColumnName_2() throws SQLException {
        when(mockRs.getString("tool_type")).thenReturn("2");

        String result = handler.getNullableResult(mockRs, "tool_type");

        assertEquals("2", result);
    }

    /**
     * 测试getNullableResult (按列名) - null返回null
     */
    @Test
    void testGetNullableResult_ByColumnName_Null() throws SQLException {
        when(mockRs.getString("tool_type")).thenReturn(null);

        String result = handler.getNullableResult(mockRs, "tool_type");

        assertNull(result);
    }

    /**
     * 测试getNullableResult (按列名) - 兼容旧数据"tool"转换为"1"
     */
    @Test
    void testGetNullableResult_ByColumnName_OldDataTool() throws SQLException {
        when(mockRs.getString("tool_type")).thenReturn("tool");

        String result = handler.getNullableResult(mockRs, "tool_type");

        assertEquals("1", result);
    }

    /**
     * 测试getNullableResult (按列名) - 兼容旧数据"agent"转换为"2"
     */
    @Test
    void testGetNullableResult_ByColumnName_OldDataAgent() throws SQLException {
        when(mockRs.getString("tool_type")).thenReturn("agent");

        String result = handler.getNullableResult(mockRs, "tool_type");

        assertEquals("2", result);
    }

    /**
     * 测试getNullableResult (按列名) - 兼容旧数据"TOOL"转换为"1"
     */
    @Test
    void testGetNullableResult_ByColumnName_OldDataToolUpperCase() throws SQLException {
        when(mockRs.getString("tool_type")).thenReturn("TOOL");

        String result = handler.getNullableResult(mockRs, "tool_type");

        assertEquals("1", result);
    }

    /**
     * 测试getNullableResult (按列名) - 兼容旧数据"AGENT"转换为"2"
     */
    @Test
    void testGetNullableResult_ByColumnName_OldDataAgentUpperCase() throws SQLException {
        when(mockRs.getString("tool_type")).thenReturn("AGENT");

        String result = handler.getNullableResult(mockRs, "tool_type");

        assertEquals("2", result);
    }

    /**
     * 测试getNullableResult (按列名) - 未知值直接返回
     */
    @Test
    void testGetNullableResult_ByColumnName_UnknownValue() throws SQLException {
        when(mockRs.getString("tool_type")).thenReturn("unknown");

        String result = handler.getNullableResult(mockRs, "tool_type");

        assertEquals("unknown", result);
    }

    /**
     * 测试getNullableResult (按列索引) - "1"保持为"1"
     */
    @Test
    void testGetNullableResult_ByColumnIndex_1() throws SQLException {
        when(mockRs.getString(1)).thenReturn("1");

        String result = handler.getNullableResult(mockRs, 1);

        assertEquals("1", result);
    }

    /**
     * 测试getNullableResult (按列索引) - "2"保持为"2"
     */
    @Test
    void testGetNullableResult_ByColumnIndex_2() throws SQLException {
        when(mockRs.getString(1)).thenReturn("2");

        String result = handler.getNullableResult(mockRs, 1);

        assertEquals("2", result);
    }

    /**
     * 测试getNullableResult (按列索引) - null返回null
     */
    @Test
    void testGetNullableResult_ByColumnIndex_Null() throws SQLException {
        when(mockRs.getString(1)).thenReturn(null);

        String result = handler.getNullableResult(mockRs, 1);

        assertNull(result);
    }

    /**
     * 测试getNullableResult (CallableStatement) - "1"保持为"1"
     */
    @Test
    void testGetNullableResult_CallableStatement_1() throws SQLException {
        when(mockCs.getString(1)).thenReturn("1");

        String result = handler.getNullableResult(mockCs, 1);

        assertEquals("1", result);
    }

    /**
     * 测试getNullableResult (CallableStatement) - "2"保持为"2"
     */
    @Test
    void testGetNullableResult_CallableStatement_2() throws SQLException {
        when(mockCs.getString(1)).thenReturn("2");

        String result = handler.getNullableResult(mockCs, 1);

        assertEquals("2", result);
    }

    /**
     * 测试getNullableResult (CallableStatement) - null返回null
     */
    @Test
    void testGetNullableResult_CallableStatement_Null() throws SQLException {
        when(mockCs.getString(1)).thenReturn(null);

        String result = handler.getNullableResult(mockCs, 1);

        assertNull(result);
    }

    /**
     * 测试getNullableResult (CallableStatement) - 兼容旧数据"tool"转换为"1"
     */
    @Test
    void testGetNullableResult_CallableStatement_OldDataTool() throws SQLException {
        when(mockCs.getString(1)).thenReturn("tool");

        String result = handler.getNullableResult(mockCs, 1);

        assertEquals("1", result);
    }

    /**
     * 测试getNullableResult (CallableStatement) - 兼容旧数据"agent"转换为"2"
     */
    @Test
    void testGetNullableResult_CallableStatement_OldDataAgent() throws SQLException {
        when(mockCs.getString(1)).thenReturn("agent");

        String result = handler.getNullableResult(mockCs, 1);

        assertEquals("2", result);
    }
}
