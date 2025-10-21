package cn.com.wind.mcp.registry.interceptor;

import cn.com.wind.mcp.registry.entity.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LoginInterceptor 单元测试
 *
 * @author system
 * @date 2025-10-17
 */
class LoginInterceptorTest {

    private LoginInterceptor interceptor;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private HttpSession mockSession;
    private Provider testProvider;

    @BeforeEach
    void setUp() {
        interceptor = new LoginInterceptor();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockSession = mock(HttpSession.class);

        testProvider = new Provider();
        testProvider.setId(1L);
        testProvider.setUsername("testuser");
    }

    /**
     * 测试preHandle - 访问登录页面 (允许)
     */
    @Test
    void testPreHandle_LoginPage() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/provider/login");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 访问注册页面 (允许)
     */
    @Test
    void testPreHandle_RegisterPage() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/provider/register");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 访问API工具接口 (允许)
     */
    @Test
    void testPreHandle_ApiToolsPath() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/api/tools/list");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 访问法律文档页面 (允许)
     */
    @Test
    void testPreHandle_LegalPath() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/legal/privacy");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 访问CSS静态资源 (允许)
     */
    @Test
    void testPreHandle_CssPath() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/css/style.css");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 访问JS静态资源 (允许)
     */
    @Test
    void testPreHandle_JsPath() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/js/main.js");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 访问第三方库资源 (允许)
     */
    @Test
    void testPreHandle_VendorPath() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/vendor/bootstrap.min.js");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 访问字体资源 (允许)
     */
    @Test
    void testPreHandle_WebfontsPath() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/webfonts/fa-solid.woff2");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 访问图片资源 (允许)
     */
    @Test
    void testPreHandle_ImagesPath() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/images/logo.png");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 访问favicon.ico (允许)
     */
    @Test
    void testPreHandle_Favicon() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/favicon.ico");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 已登录用户访问受保护页面 (允许)
     */
    @Test
    void testPreHandle_LoggedInUser() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/mcp-tools/list");
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("currentProvider")).thenReturn(testProvider);

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertTrue(result);
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - 未登录用户访问受保护页面 (重定向到登录页)
     */
    @Test
    void testPreHandle_NotLoggedInUser_Redirect() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/mcp-tools/list");
        when(mockRequest.getSession(false)).thenReturn(null);
        when(mockRequest.getHeader("X-Requested-With")).thenReturn(null);
        when(mockRequest.getHeader("Content-Type")).thenReturn(null);
        when(mockRequest.getHeader("Accept")).thenReturn("text/html");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertFalse(result);
        verify(mockResponse).sendRedirect("/provider/login");
    }

    /**
     * 测试preHandle - 未登录用户Ajax请求受保护接口 (返回JSON)
     */
    @Test
    void testPreHandle_NotLoggedInUser_AjaxRequest() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/mcp-tools/api/list");
        when(mockRequest.getSession(false)).thenReturn(null);
        when(mockRequest.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(mockResponse.getWriter()).thenReturn(writer);

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertFalse(result);
        verify(mockResponse).setContentType("application/json;charset=UTF-8");
        assertTrue(stringWriter.toString().contains("请先登录"));
        verify(mockResponse, never()).sendRedirect(anyString());
    }

    /**
     * 测试preHandle - Ajax请求 (通过Content-Type判断)
     */
    @Test
    void testPreHandle_AjaxRequest_ByContentType() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/mcp-tools/api/save");
        when(mockRequest.getSession(false)).thenReturn(null);
        when(mockRequest.getHeader("X-Requested-With")).thenReturn(null);
        when(mockRequest.getHeader("Content-Type")).thenReturn("application/json");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(mockResponse.getWriter()).thenReturn(writer);

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertFalse(result);
        verify(mockResponse).setContentType("application/json;charset=UTF-8");
        assertTrue(stringWriter.toString().contains("请先登录"));
    }

    /**
     * 测试preHandle - Ajax请求 (通过Accept判断)
     */
    @Test
    void testPreHandle_AjaxRequest_ByAccept() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/mcp-tools/api/delete");
        when(mockRequest.getSession(false)).thenReturn(null);
        when(mockRequest.getHeader("X-Requested-With")).thenReturn(null);
        when(mockRequest.getHeader("Content-Type")).thenReturn(null);
        when(mockRequest.getHeader("Accept")).thenReturn("application/json");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(mockResponse.getWriter()).thenReturn(writer);

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertFalse(result);
        verify(mockResponse).setContentType("application/json;charset=UTF-8");
        assertTrue(stringWriter.toString().contains("请先登录"));
    }

    /**
     * 测试preHandle - Session存在但无Provider (重定向)
     */
    @Test
    void testPreHandle_SessionExistsButNoProvider() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/mcp-tools/list");
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("currentProvider")).thenReturn(null);
        when(mockRequest.getHeader("X-Requested-With")).thenReturn(null);
        when(mockRequest.getHeader("Content-Type")).thenReturn(null);
        when(mockRequest.getHeader("Accept")).thenReturn("text/html");

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertFalse(result);
        verify(mockResponse).sendRedirect("/provider/login");
    }

    /**
     * 测试postHandle - ModelAndView为null
     */
    @Test
    void testPostHandle_NullModelAndView() throws Exception {
        assertDoesNotThrow(() ->
                interceptor.postHandle(mockRequest, mockResponse, new Object(), null)
        );
    }

    /**
     * 测试postHandle - ModelAndView无视图名
     */
    @Test
    void testPostHandle_NoViewName() throws Exception {
        ModelAndView modelAndView = new ModelAndView();

        assertDoesNotThrow(() ->
                interceptor.postHandle(mockRequest, mockResponse, new Object(), modelAndView)
        );
    }

    /**
     * 测试postHandle - 重定向视图 (不添加currentPath)
     */
    @Test
    void testPostHandle_RedirectView() throws Exception {
        ModelAndView modelAndView = new ModelAndView("redirect:/provider/login");

        interceptor.postHandle(mockRequest, mockResponse, new Object(), modelAndView);

        assertFalse(modelAndView.getModel().containsKey("currentPath"));
    }

    /**
     * 测试postHandle - 正常视图 (添加currentPath)
     */
    @Test
    void testPostHandle_NormalView() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/mcp-tools/list");
        ModelAndView modelAndView = new ModelAndView("mcp-tools/list");

        interceptor.postHandle(mockRequest, mockResponse, new Object(), modelAndView);

        assertTrue(modelAndView.getModel().containsKey("currentPath"));
        assertEquals("/mcp-tools/list", modelAndView.getModel().get("currentPath"));
    }

    /**
     * 测试postHandle - ModelAndView未设置视图 (hasView返回false)
     */
    @Test
    void testPostHandle_HasNoView() throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.clear();

        assertDoesNotThrow(() ->
                interceptor.postHandle(mockRequest, mockResponse, new Object(), modelAndView)
        );
    }

    /**
     * 边界条件测试 - Content-Type包含json
     */
    @Test
    void testPreHandle_ContentTypeContainsJson() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/mcp-tools/api/update");
        when(mockRequest.getSession(false)).thenReturn(null);
        when(mockRequest.getHeader("X-Requested-With")).thenReturn(null);
        when(mockRequest.getHeader("Content-Type")).thenReturn("application/json;charset=UTF-8");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(mockResponse.getWriter()).thenReturn(writer);

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertFalse(result);
        verify(mockResponse).setContentType("application/json;charset=UTF-8");
    }

    /**
     * 边界条件测试 - Accept包含json
     */
    @Test
    void testPreHandle_AcceptContainsJson() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/mcp-tools/api/search");
        when(mockRequest.getSession(false)).thenReturn(null);
        when(mockRequest.getHeader("X-Requested-With")).thenReturn(null);
        when(mockRequest.getHeader("Content-Type")).thenReturn(null);
        when(mockRequest.getHeader("Accept")).thenReturn("text/html, application/json");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(mockResponse.getWriter()).thenReturn(writer);

        boolean result = interceptor.preHandle(mockRequest, mockResponse, new Object());

        assertFalse(result);
        verify(mockResponse).setContentType("application/json;charset=UTF-8");
    }
}
