package cn.com.wind.mcp.registry.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SessionDataUtil 单元测试
 *
 * @author system
 * @date 2025-10-17
 */
class SessionDataUtilTest {

    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = mock(HttpServletRequest.class);
    }

    /**
     * 测试isNullOrEmpty - null值
     */
    @Test
    void testIsNullOrEmpty_Null() {
        assertTrue(SessionDataUtil.isNullOrEmpty(null));
    }

    /**
     * 测试isNullOrEmpty - 空字符串
     */
    @Test
    void testIsNullOrEmpty_EmptyString() {
        assertTrue(SessionDataUtil.isNullOrEmpty(""));
    }

    /**
     * 测试isNullOrEmpty - 空格字符串
     */
    @Test
    void testIsNullOrEmpty_WhitespaceString() {
        assertTrue(SessionDataUtil.isNullOrEmpty("   "));
    }

    /**
     * 测试isNullOrEmpty - "unknown"字符串
     */
    @Test
    void testIsNullOrEmpty_UnknownString() {
        assertTrue(SessionDataUtil.isNullOrEmpty("unknown"));
    }

    /**
     * 测试isNullOrEmpty - "unknown"字符串带空格
     */
    @Test
    void testIsNullOrEmpty_UnknownStringWithSpaces() {
        assertTrue(SessionDataUtil.isNullOrEmpty("  unknown  "));
    }

    /**
     * 测试isNullOrEmpty - 正常值
     */
    @Test
    void testIsNullOrEmpty_ValidValue() {
        assertFalse(SessionDataUtil.isNullOrEmpty("192.168.1.1"));
    }

    /**
     * 测试isValidIp - 有效IP地址
     */
    @Test
    void testIsValidIp_ValidIp() {
        assertTrue(SessionDataUtil.isValidIp("192.168.1.1"));
        assertTrue(SessionDataUtil.isValidIp("10.0.0.1"));
        assertTrue(SessionDataUtil.isValidIp("172.16.0.1"));
        assertTrue(SessionDataUtil.isValidIp("8.8.8.8"));
        assertTrue(SessionDataUtil.isValidIp("255.255.255.255"));
    }

    /**
     * 测试isValidIp - 无效IP地址
     */
    @Test
    void testIsValidIp_InvalidIp() {
        assertFalse(SessionDataUtil.isValidIp("256.1.1.1"));
        assertFalse(SessionDataUtil.isValidIp("192.168.1"));
        assertFalse(SessionDataUtil.isValidIp("192.168.1.1.1"));
        assertFalse(SessionDataUtil.isValidIp("abc.def.ghi.jkl"));
        assertFalse(SessionDataUtil.isValidIp(""));
    }

    /**
     * 测试getIpAddress - request为null
     */
    @Test
    void testGetIpAddress_NullRequest() {
        String ip = SessionDataUtil.getIpAddress(null);
        assertEquals("127.0.0.1", ip);
    }

    /**
     * 测试getIpAddress - 从HTTP_X_FORWARDED_FOR获取单个IP
     */
    @Test
    void testGetIpAddress_FromForwardedFor_SingleIp() {
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn("203.0.113.195");

        String ip = SessionDataUtil.getIpAddress(mockRequest);

        assertEquals("203.0.113.195", ip);
    }

    /**
     * 测试getIpAddress - 从HTTP_X_FORWARDED_FOR获取多个IP (逗号分隔)
     */
    @Test
    void testGetIpAddress_FromForwardedFor_MultipleIps() {
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR"))
            .thenReturn("10.0.0.1,203.0.113.195,192.168.1.1");

        String ip = SessionDataUtil.getIpAddress(mockRequest);

        // 应该返回第一个非内网IP
        assertEquals("203.0.113.195", ip);
    }

    /**
     * 测试getIpAddress - 从HTTP_X_FORWARDED_FOR获取多个IP (混合分隔符)
     */
    @Test
    void testGetIpAddress_FromForwardedFor_MixedSeparators() {
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR"))
            .thenReturn("10.0.0.1,203.0.113.195;192.168.1.1");

        String ip = SessionDataUtil.getIpAddress(mockRequest);

        // 逗号或分号都支持分隔
        assertEquals("203.0.113.195", ip);
    }

    /**
     * 测试getIpAddress - HTTP_X_FORWARDED_FOR包含单引号
     */
    @Test
    void testGetIpAddress_FromForwardedFor_WithQuotes() {
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR"))
            .thenReturn("'10.0.0.1','203.0.113.195'");

        String ip = SessionDataUtil.getIpAddress(mockRequest);

        assertEquals("203.0.113.195", ip);
    }

    /**
     * 测试getIpAddress - HTTP_X_FORWARDED_FOR只有内网IP
     */
    @Test
    void testGetIpAddress_FromForwardedFor_OnlyPrivateIps() {
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR"))
            .thenReturn("10.0.0.1,192.168.1.1,172.16.0.1");
        when(mockRequest.getRemoteAddr()).thenReturn("203.0.113.195");

        String ip = SessionDataUtil.getIpAddress(mockRequest);

        // 内网IP没有被过滤掉,返回整个字符串
        assertEquals("10.0.0.1,192.168.1.1,172.16.0.1", ip);
    }

    /**
     * 测试getIpAddress - HTTP_X_FORWARDED_FOR不包含点号
     */
    @Test
    void testGetIpAddress_FromForwardedFor_NoDot() {
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn("invalid");
        when(mockRequest.getHeader("REMOTE_ADDR")).thenReturn("203.0.113.195");

        String ip = SessionDataUtil.getIpAddress(mockRequest);

        assertEquals("203.0.113.195", ip);
    }

    /**
     * 测试getIpAddress - 从REMOTE_ADDR获取
     */
    @Test
    void testGetIpAddress_FromRemoteAddr() {
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(mockRequest.getHeader("REMOTE_ADDR")).thenReturn("203.0.113.195");

        String ip = SessionDataUtil.getIpAddress(mockRequest);

        assertEquals("203.0.113.195", ip);
    }

    /**
     * 测试getIpAddress - 从getRemoteAddr()获取
     */
    @Test
    void testGetIpAddress_FromGetRemoteAddr() {
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(mockRequest.getHeader("REMOTE_ADDR")).thenReturn(null);
        when(mockRequest.getRemoteAddr()).thenReturn("203.0.113.195");

        String ip = SessionDataUtil.getIpAddress(mockRequest);

        assertEquals("203.0.113.195", ip);
    }

    /**
     * 测试getIpAddress - 所有来源都是unknown
     */
    @Test
    void testGetIpAddress_AllUnknown() {
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn("unknown");
        when(mockRequest.getHeader("REMOTE_ADDR")).thenReturn("unknown");
        when(mockRequest.getRemoteAddr()).thenReturn("203.0.113.195");

        String ip = SessionDataUtil.getIpAddress(mockRequest);

        assertEquals("203.0.113.195", ip);
    }

    /**
     * 测试getLan - request为null
     */
    @Test
    void testGetLan_NullRequest() {
        String lan = SessionDataUtil.getLan(null);
        assertEquals("cn", lan);
    }

    /**
     * 测试getLan - 从wind-language header获取"cn"
     */
    @Test
    void testGetLan_FromHeader_Cn() {
        when(mockRequest.getHeader("wind-language")).thenReturn("cn");

        String lan = SessionDataUtil.getLan(mockRequest);

        assertEquals("cn", lan);
    }

    /**
     * 测试getLan - 从wind-language header获取"en"
     */
    @Test
    void testGetLan_FromHeader_En() {
        when(mockRequest.getHeader("wind-language")).thenReturn("en");

        String lan = SessionDataUtil.getLan(mockRequest);

        assertEquals("en", lan);
    }

    /**
     * 测试getLan - 从wind-language header获取"zh-CN"
     */
    @Test
    void testGetLan_FromHeader_ZhCN() {
        when(mockRequest.getHeader("wind-language")).thenReturn("zh-CN");

        String lan = SessionDataUtil.getLan(mockRequest);

        assertEquals("cn", lan);
    }

    /**
     * 测试getLan - 从wind-language header获取"zh"
     */
    @Test
    void testGetLan_FromHeader_Zh() {
        when(mockRequest.getHeader("wind-language")).thenReturn("zh");

        String lan = SessionDataUtil.getLan(mockRequest);

        assertEquals("cn", lan);
    }

    /**
     * 测试getLan - header为空,从参数获取
     */
    @Test
    void testGetLan_FromParameter() {
        when(mockRequest.getHeader("wind-language")).thenReturn(null);
        when(mockRequest.getParameter("lan")).thenReturn("en");

        String lan = SessionDataUtil.getLan(mockRequest);

        assertEquals("en", lan);
    }

    /**
     * 测试getLan - header和参数都为空
     */
    @Test
    void testGetLan_DefaultCn() {
        when(mockRequest.getHeader("wind-language")).thenReturn(null);
        when(mockRequest.getParameter("lan")).thenReturn(null);

        String lan = SessionDataUtil.getLan(mockRequest);

        assertEquals("cn", lan);
    }

    /**
     * 测试getLan - 未知语言默认为en
     */
    @Test
    void testGetLan_UnknownLanguage() {
        when(mockRequest.getHeader("wind-language")).thenReturn("fr");

        String lan = SessionDataUtil.getLan(mockRequest);

        assertEquals("en", lan);
    }

    /**
     * 测试getInternalUserId - request为null
     */
    @Test
    void testGetInternalUserId_NullRequest() {
        int userId = SessionDataUtil.getInternalUserId(null);
        assertEquals(0, userId);
    }

    /**
     * 测试getInternalUserId - session为null
     */
    @Test
    void testGetInternalUserId_NullSession() {
        when(mockRequest.getSession()).thenReturn(null);

        int userId = SessionDataUtil.getInternalUserId(mockRequest);

        assertEquals(0, userId);
    }

    /**
     * 边界条件测试 - isValidIp边界值
     */
    @Test
    void testIsValidIp_BoundaryValues() {
        assertTrue(SessionDataUtil.isValidIp("0.0.0.0"));
        assertTrue(SessionDataUtil.isValidIp("1.1.1.1"));
        assertTrue(SessionDataUtil.isValidIp("127.0.0.1"));
    }

    /**
     * 边界条件测试 - IP地址过滤内网地址
     */
    @Test
    void testGetIpAddress_FilterPrivateIps() {
        // 测试10.x.x.x被过滤
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR"))
            .thenReturn("10.1.2.3,203.0.113.195");
        assertEquals("203.0.113.195", SessionDataUtil.getIpAddress(mockRequest));

        // 测试192.168.x.x被过滤
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR"))
            .thenReturn("192.168.0.1,203.0.113.195");
        assertEquals("203.0.113.195", SessionDataUtil.getIpAddress(mockRequest));

        // 测试172.16.x.x被过滤
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR"))
            .thenReturn("172.16.0.1,203.0.113.195");
        assertEquals("203.0.113.195", SessionDataUtil.getIpAddress(mockRequest));
    }

    /**
     * 边界条件测试 - HTTP_X_FORWARDED_FOR无效IP回退
     */
    @Test
    void testGetIpAddress_InvalidIpFallback() {
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn("999.999.999.999");
        when(mockRequest.getRemoteAddr()).thenReturn("203.0.113.195");

        String ip = SessionDataUtil.getIpAddress(mockRequest);

        assertEquals("203.0.113.195", ip);
    }
}
