package cn.com.wind.mcp.registry.util;

import java.io.UnsupportedEncodingException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * WindSessionIdUtils 单元测试
 *
 * @author system
 * @date 2025-10-17
 */
class WindSessionIdUtilsTest {

    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = mock(HttpServletRequest.class);
    }

    /**
     * 测试getWindSessionId - 从wind.sessionid header获取
     */
    @Test
    void testGetWindSessionId_FromOldHeader() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn("session123");
        when(mockRequest.getHeader("windsessionid")).thenReturn(null);

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        assertEquals("session123", result);
    }

    /**
     * 测试getWindSessionId - 从windsessionid header获取 (优先级高)
     */
    @Test
    void testGetWindSessionId_FromNewHeader() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn("oldSession");
        when(mockRequest.getHeader("windsessionid")).thenReturn("newSession");

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        // 新header优先级更高
        assertEquals("newSession", result);
    }

    /**
     * 测试getWindSessionId - header为空,从参数获取
     */
    @Test
    void testGetWindSessionId_FromParameter() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn(null);
        when(mockRequest.getHeader("windsessionid")).thenReturn(null);
        when(mockRequest.getParameter("wind.sessionid")).thenReturn("paramSession");
        when(mockRequest.getParameter("windsessionid")).thenReturn(null);

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        assertEquals("paramSession", result);
    }

    /**
     * 测试getWindSessionId - 从新参数获取 (优先级高)
     */
    @Test
    void testGetWindSessionId_FromNewParameter() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn(null);
        when(mockRequest.getHeader("windsessionid")).thenReturn(null);
        when(mockRequest.getParameter("wind.sessionid")).thenReturn("oldParam");
        when(mockRequest.getParameter("windsessionid")).thenReturn("newParam");

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        assertEquals("newParam", result);
    }

    /**
     * 测试getWindSessionId - header和参数都为空,从Cookie获取
     */
    @Test
    void testGetWindSessionId_FromCookie() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn(null);
        when(mockRequest.getHeader("windsessionid")).thenReturn(null);
        when(mockRequest.getParameter("wind.sessionid")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid")).thenReturn(null);

        Cookie cookie = new Cookie("windsessionid", "cookieSession");
        Cookie[] cookies = {cookie};
        when(mockRequest.getCookies()).thenReturn(cookies);

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        assertEquals("cookieSession", result);
    }

    /**
     * 测试getWindSessionId - 从旧Cookie获取
     */
    @Test
    void testGetWindSessionId_FromOldCookie() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn(null);
        when(mockRequest.getHeader("windsessionid")).thenReturn(null);
        when(mockRequest.getParameter("wind.sessionid")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid")).thenReturn(null);

        Cookie cookie = new Cookie("wind.sessionid", "oldCookieSession");
        Cookie[] cookies = {cookie};
        when(mockRequest.getCookies()).thenReturn(cookies);

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        assertEquals("oldCookieSession", result);
    }

    /**
     * 测试getWindSessionId - 所有来源都为空
     */
    @Test
    void testGetWindSessionId_AllNull() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn(null);
        when(mockRequest.getHeader("windsessionid")).thenReturn(null);
        when(mockRequest.getParameter("wind.sessionid")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid")).thenReturn(null);
        when(mockRequest.getCookies()).thenReturn(null);

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        assertNull(result);
    }

    /**
     * 测试getWindSessionId - Cookie值需要URL解码
     */
    @Test
    void testGetWindSessionId_CookieUrlDecoded() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn(null);
        when(mockRequest.getHeader("windsessionid")).thenReturn(null);
        when(mockRequest.getParameter("wind.sessionid")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid")).thenReturn(null);

        Cookie cookie = new Cookie("windsessionid", "session%2B123");
        Cookie[] cookies = {cookie};
        when(mockRequest.getCookies()).thenReturn(cookies);

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        assertEquals("session+123", result);
    }

    /**
     * 测试getWindSessionID2 - 从header获取
     */
    @Test
    void testGetWindSessionID2_FromHeader() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("windsessionid2")).thenReturn("session2_123");

        String result = WindSessionIdUtils.getWindSessionID2(mockRequest);

        assertEquals("session2_123", result);
    }

    /**
     * 测试getWindSessionID2 - header为空,从参数获取
     */
    @Test
    void testGetWindSessionID2_FromParameter() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("windsessionid2")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid2")).thenReturn("paramSession2");

        String result = WindSessionIdUtils.getWindSessionID2(mockRequest);

        assertEquals("paramSession2", result);
    }

    /**
     * 测试getWindSessionID2 - header和参数都为空,从Cookie获取
     */
    @Test
    void testGetWindSessionID2_FromCookie() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("windsessionid2")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid2")).thenReturn(null);

        Cookie cookie = new Cookie("windsessionid2", "cookieSession2");
        Cookie[] cookies = {cookie};
        when(mockRequest.getCookies()).thenReturn(cookies);

        String result = WindSessionIdUtils.getWindSessionID2(mockRequest);

        assertEquals("cookieSession2", result);
    }

    /**
     * 测试getWindSessionID2 - 所有来源都为空
     */
    @Test
    void testGetWindSessionID2_AllNull() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("windsessionid2")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid2")).thenReturn(null);
        when(mockRequest.getCookies()).thenReturn(null);

        String result = WindSessionIdUtils.getWindSessionID2(mockRequest);

        assertNull(result);
    }

    /**
     * 测试getWindSessionID2 - Cookie值需要URL解码
     */
    @Test
    void testGetWindSessionID2_CookieUrlDecoded() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("windsessionid2")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid2")).thenReturn(null);

        Cookie cookie = new Cookie("windsessionid2", "session2%2B456");
        Cookie[] cookies = {cookie};
        when(mockRequest.getCookies()).thenReturn(cookies);

        String result = WindSessionIdUtils.getWindSessionID2(mockRequest);

        assertEquals("session2+456", result);
    }

    /**
     * 边界条件测试 - 空字符串被认为是空
     */
    @Test
    void testGetWindSessionId_EmptyStringIsBlank() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn("");
        when(mockRequest.getHeader("windsessionid")).thenReturn("");
        when(mockRequest.getParameter("wind.sessionid")).thenReturn("paramSession");

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        // 空字符串被认为是blank,应该fallback到参数
        assertEquals("paramSession", result);
    }

    /**
     * 边界条件测试 - 空格字符串被认为是空
     */
    @Test
    void testGetWindSessionId_WhitespaceIsBlank() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn("   ");
        when(mockRequest.getHeader("windsessionid")).thenReturn("  ");
        when(mockRequest.getParameter("wind.sessionid")).thenReturn("paramSession");

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        // 空格字符串被认为是blank,应该fallback到参数
        assertEquals("paramSession", result);
    }

    /**
     * 边界条件测试 - Cookie中有多个session id相关cookie
     */
    @Test
    void testGetWindSessionId_MultipleCookies() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn(null);
        when(mockRequest.getHeader("windsessionid")).thenReturn(null);
        when(mockRequest.getParameter("wind.sessionid")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid")).thenReturn(null);

        Cookie cookie1 = new Cookie("othercookie", "other");
        Cookie cookie2 = new Cookie("windsessionid", "newSession");
        Cookie cookie3 = new Cookie("wind.sessionid", "oldSession");
        Cookie[] cookies = {cookie1, cookie2, cookie3};
        when(mockRequest.getCookies()).thenReturn(cookies);

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        // 应该优先返回windsessionid
        assertEquals("newSession", result);
    }

    /**
     * 边界条件测试 - 只有wind.sessionid cookie存在
     */
    @Test
    void testGetWindSessionId_OnlyOldCookieExists() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn(null);
        when(mockRequest.getHeader("windsessionid")).thenReturn(null);
        when(mockRequest.getParameter("wind.sessionid")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid")).thenReturn(null);

        Cookie cookie1 = new Cookie("othercookie", "other");
        Cookie cookie2 = new Cookie("wind.sessionid", "oldSession");
        Cookie[] cookies = {cookie1, cookie2};
        when(mockRequest.getCookies()).thenReturn(cookies);

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        assertEquals("oldSession", result);
    }

    /**
     * 边界条件测试 - 特殊字符在session id中
     */
    @Test
    void testGetWindSessionId_SpecialCharacters() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("windsessionid")).thenReturn("session-123_abc.xyz");

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        assertEquals("session-123_abc.xyz", result);
    }

    /**
     * 边界条件测试 - 中文字符URL编码
     */
    @Test
    void testGetWindSessionId_ChineseCharactersUrlEncoded() throws UnsupportedEncodingException {
        when(mockRequest.getHeader("wind.sessionid")).thenReturn(null);
        when(mockRequest.getHeader("windsessionid")).thenReturn(null);
        when(mockRequest.getParameter("wind.sessionid")).thenReturn(null);
        when(mockRequest.getParameter("windsessionid")).thenReturn(null);

        Cookie cookie = new Cookie("windsessionid", "%E4%B8%AD%E6%96%87");
        Cookie[] cookies = {cookie};
        when(mockRequest.getCookies()).thenReturn(cookies);

        String result = WindSessionIdUtils.getWindSessionId(mockRequest);

        assertEquals("中文", result);
    }
}
