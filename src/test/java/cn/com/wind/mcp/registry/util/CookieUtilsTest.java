package cn.com.wind.mcp.registry.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * CookieUtils 单元测试
 *
 * @author system
 * @date 2025-10-17
 */
class CookieUtilsTest {

    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = mock(HttpServletRequest.class);
    }

    /**
     * 测试getCookie - 成功获取Cookie
     */
    @Test
    void testGetCookie_Success() {
        Cookie cookie1 = new Cookie("sessionId", "abc123");
        Cookie cookie2 = new Cookie("userId", "user001");
        Cookie[] cookies = {cookie1, cookie2};

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result = CookieUtils.getCookie(mockRequest, "sessionId");

        assertNotNull(result);
        assertEquals("sessionId", result.getName());
        assertEquals("abc123", result.getValue());
    }

    /**
     * 测试getCookie - 获取第二个Cookie
     */
    @Test
    void testGetCookie_GetSecondCookie() {
        Cookie cookie1 = new Cookie("sessionId", "abc123");
        Cookie cookie2 = new Cookie("userId", "user001");
        Cookie cookie3 = new Cookie("theme", "dark");
        Cookie[] cookies = {cookie1, cookie2, cookie3};

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result = CookieUtils.getCookie(mockRequest, "userId");

        assertNotNull(result);
        assertEquals("userId", result.getName());
        assertEquals("user001", result.getValue());
    }

    /**
     * 测试getCookie - Cookie不存在
     */
    @Test
    void testGetCookie_NotFound() {
        Cookie cookie1 = new Cookie("sessionId", "abc123");
        Cookie[] cookies = {cookie1};

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result = CookieUtils.getCookie(mockRequest, "nonexistent");

        assertNull(result);
    }

    /**
     * 测试getCookie - Request为null
     */
    @Test
    void testGetCookie_NullRequest() {
        Cookie result = CookieUtils.getCookie(null, "sessionId");

        assertNull(result);
    }

    /**
     * 测试getCookie - Cookie名称为null
     */
    @Test
    void testGetCookie_NullName() {
        Cookie cookie1 = new Cookie("sessionId", "abc123");
        Cookie[] cookies = {cookie1};

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result = CookieUtils.getCookie(mockRequest, null);

        assertNull(result);
    }

    /**
     * 测试getCookie - Request中没有Cookie
     */
    @Test
    void testGetCookie_NoCookies() {
        when(mockRequest.getCookies()).thenReturn(null);

        Cookie result = CookieUtils.getCookie(mockRequest, "sessionId");

        assertNull(result);
    }

    /**
     * 测试getCookie - Cookie数组为空
     */
    @Test
    void testGetCookie_EmptyCookieArray() {
        Cookie[] cookies = {};

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result = CookieUtils.getCookie(mockRequest, "sessionId");

        assertNull(result);
    }

    /**
     * 边界条件测试 - 查找空字符串名称的Cookie (不会找到因为Cookie不允许空名称)
     */
    @Test
    void testGetCookie_SearchEmptyName() {
        Cookie cookie1 = new Cookie("sessionId", "abc123");
        Cookie[] cookies = {cookie1};

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result = CookieUtils.getCookie(mockRequest, "");

        // 空字符串不匹配任何Cookie
        assertNull(result);
    }

    /**
     * 边界条件测试 - 多个同名Cookie (返回第一个)
     */
    @Test
    void testGetCookie_DuplicateNames() {
        Cookie cookie1 = new Cookie("sessionId", "first");
        Cookie cookie2 = new Cookie("sessionId", "second");
        Cookie[] cookies = {cookie1, cookie2};

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result = CookieUtils.getCookie(mockRequest, "sessionId");

        assertNotNull(result);
        assertEquals("sessionId", result.getName());
        assertEquals("first", result.getValue());
    }

    /**
     * 边界条件测试 - Cookie值为null
     */
    @Test
    void testGetCookie_NullValue() {
        Cookie cookie1 = new Cookie("sessionId", null);
        Cookie[] cookies = {cookie1};

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result = CookieUtils.getCookie(mockRequest, "sessionId");

        assertNotNull(result);
        assertEquals("sessionId", result.getName());
        assertNull(result.getValue());
    }

    /**
     * 边界条件测试 - Cookie值为空字符串
     */
    @Test
    void testGetCookie_EmptyValue() {
        Cookie cookie1 = new Cookie("sessionId", "");
        Cookie[] cookies = {cookie1};

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result = CookieUtils.getCookie(mockRequest, "sessionId");

        assertNotNull(result);
        assertEquals("sessionId", result.getName());
        assertEquals("", result.getValue());
    }

    /**
     * 边界条件测试 - 大量Cookie中查找
     */
    @Test
    void testGetCookie_ManyCookies() {
        Cookie[] cookies = new Cookie[100];
        for (int i = 0; i < 100; i++) {
            cookies[i] = new Cookie("cookie" + i, "value" + i);
        }

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result = CookieUtils.getCookie(mockRequest, "cookie99");

        assertNotNull(result);
        assertEquals("cookie99", result.getName());
        assertEquals("value99", result.getValue());
    }

    /**
     * 边界条件测试 - 特殊字符在Cookie名称中
     */
    @Test
    void testGetCookie_SpecialCharactersInName() {
        Cookie cookie1 = new Cookie("session-id", "abc123");
        Cookie cookie2 = new Cookie("user_name", "john");
        Cookie[] cookies = {cookie1, cookie2};

        when(mockRequest.getCookies()).thenReturn(cookies);

        Cookie result1 = CookieUtils.getCookie(mockRequest, "session-id");
        Cookie result2 = CookieUtils.getCookie(mockRequest, "user_name");

        assertNotNull(result1);
        assertEquals("session-id", result1.getName());
        assertNotNull(result2);
        assertEquals("user_name", result2.getName());
    }
}
