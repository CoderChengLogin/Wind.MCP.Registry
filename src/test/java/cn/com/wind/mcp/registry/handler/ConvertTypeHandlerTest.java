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
 * ConvertTypeHandler 单元测试
 * 测试数据库CHAR(1)值和业务逻辑String值之间的转换
 *
 * @author system
 * @date 2025-10-17
 */
class ConvertTypeHandlerTest {

    private ConvertTypeHandler handler;
    private PreparedStatement mockPs;
    private ResultSet mockRs;
    private CallableStatement mockCs;

    @BeforeEach
    void setUp() {
        handler = new ConvertTypeHandler();
        mockPs = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);
        mockCs = mock(CallableStatement.class);
    }

    /**
     * 测试setNonNullParameter - 业务值"http"转换为"1"
     */
    @Test
    void testSetNonNullParameter_Http() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "http", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试setNonNullParameter - 业务值"expo"转换为"2"
     */
    @Test
    void testSetNonNullParameter_Expo() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "expo", JdbcType.CHAR);

        verify(mockPs).setString(1, "2");
    }

    /**
     * 测试setNonNullParameter - 业务值"code"转换为"3"
     */
    @Test
    void testSetNonNullParameter_Code() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "code", JdbcType.CHAR);

        verify(mockPs).setString(1, "3");
    }

    /**
     * 测试setNonNullParameter - 业务值"manual"转换为"3"
     */
    @Test
    void testSetNonNullParameter_Manual() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "manual", JdbcType.CHAR);

        verify(mockPs).setString(1, "3");
    }

    /**
     * 测试setNonNullParameter - 数字字符串"1"保持为"1"
     */
    @Test
    void testSetNonNullParameter_Numeric1() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "1", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试setNonNullParameter - 数字字符串"2"保持为"2"
     */
    @Test
    void testSetNonNullParameter_Numeric2() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "2", JdbcType.CHAR);

        verify(mockPs).setString(1, "2");
    }

    /**
     * 测试setNonNullParameter - 数字字符串"3"保持为"3"
     */
    @Test
    void testSetNonNullParameter_Numeric3() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "3", JdbcType.CHAR);

        verify(mockPs).setString(1, "3");
    }

    /**
     * 测试setNonNullParameter - 大写"HTTP"转换为"1"
     */
    @Test
    void testSetNonNullParameter_UpperCaseHttp() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "HTTP", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试setNonNullParameter - 混合大小写"ExPo"转换为"2"
     */
    @Test
    void testSetNonNullParameter_MixedCaseExpo() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "ExPo", JdbcType.CHAR);

        verify(mockPs).setString(1, "2");
    }

    /**
     * 测试setNonNullParameter - 带空格的"  http  "转换为"1"
     */
    @Test
    void testSetNonNullParameter_WithSpaces() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "  http  ", JdbcType.CHAR);

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
     * 测试setNonNullParameter - 包含http的复合字符串转换为"1"
     */
    @Test
    void testSetNonNullParameter_ContainsHttp() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "https", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 测试getNullableResult (按列名) - "1"转换为"http"
     */
    @Test
    void testGetNullableResult_ByColumnName_1() throws SQLException {
        when(mockRs.getString("convert_type")).thenReturn("1");

        String result = handler.getNullableResult(mockRs, "convert_type");

        assertEquals("http", result);
    }

    /**
     * 测试getNullableResult (按列名) - "2"转换为"expo"
     */
    @Test
    void testGetNullableResult_ByColumnName_2() throws SQLException {
        when(mockRs.getString("convert_type")).thenReturn("2");

        String result = handler.getNullableResult(mockRs, "convert_type");

        assertEquals("expo", result);
    }

    /**
     * 测试getNullableResult (按列名) - "3"转换为"code"
     */
    @Test
    void testGetNullableResult_ByColumnName_3() throws SQLException {
        when(mockRs.getString("convert_type")).thenReturn("3");

        String result = handler.getNullableResult(mockRs, "convert_type");

        assertEquals("code", result);
    }

    /**
     * 测试getNullableResult (按列名) - null返回null
     */
    @Test
    void testGetNullableResult_ByColumnName_Null() throws SQLException {
        when(mockRs.getString("convert_type")).thenReturn(null);

        String result = handler.getNullableResult(mockRs, "convert_type");

        assertNull(result);
    }

    /**
     * 测试getNullableResult (按列名) - 兼容旧数据"http"直接返回
     */
    @Test
    void testGetNullableResult_ByColumnName_OldDataHttp() throws SQLException {
        when(mockRs.getString("convert_type")).thenReturn("http");

        String result = handler.getNullableResult(mockRs, "convert_type");

        assertEquals("http", result);
    }

    /**
     * 测试getNullableResult (按列索引) - "1"转换为"http"
     */
    @Test
    void testGetNullableResult_ByColumnIndex_1() throws SQLException {
        when(mockRs.getString(1)).thenReturn("1");

        String result = handler.getNullableResult(mockRs, 1);

        assertEquals("http", result);
    }

    /**
     * 测试getNullableResult (按列索引) - "2"转换为"expo"
     */
    @Test
    void testGetNullableResult_ByColumnIndex_2() throws SQLException {
        when(mockRs.getString(1)).thenReturn("2");

        String result = handler.getNullableResult(mockRs, 1);

        assertEquals("expo", result);
    }

    /**
     * 测试getNullableResult (按列索引) - "3"转换为"code"
     */
    @Test
    void testGetNullableResult_ByColumnIndex_3() throws SQLException {
        when(mockRs.getString(1)).thenReturn("3");

        String result = handler.getNullableResult(mockRs, 1);

        assertEquals("code", result);
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
     * 测试getNullableResult (CallableStatement) - "1"转换为"http"
     */
    @Test
    void testGetNullableResult_CallableStatement_1() throws SQLException {
        when(mockCs.getString(1)).thenReturn("1");

        String result = handler.getNullableResult(mockCs, 1);

        assertEquals("http", result);
    }

    /**
     * 测试getNullableResult (CallableStatement) - "2"转换为"expo"
     */
    @Test
    void testGetNullableResult_CallableStatement_2() throws SQLException {
        when(mockCs.getString(1)).thenReturn("2");

        String result = handler.getNullableResult(mockCs, 1);

        assertEquals("expo", result);
    }

    /**
     * 测试getNullableResult (CallableStatement) - "3"转换为"code"
     */
    @Test
    void testGetNullableResult_CallableStatement_3() throws SQLException {
        when(mockCs.getString(1)).thenReturn("3");

        String result = handler.getNullableResult(mockCs, 1);

        assertEquals("code", result);
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
     * 边界条件测试 - 空字符串默认转换为"1"
     */
    @Test
    void testSetNonNullParameter_EmptyString() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }

    /**
     * 边界条件测试 - 只有空格的字符串默认转换为"1"
     */
    @Test
    void testSetNonNullParameter_OnlySpaces() throws SQLException {
        handler.setNonNullParameter(mockPs, 1, "   ", JdbcType.CHAR);

        verify(mockPs).setString(1, "1");
    }
}
