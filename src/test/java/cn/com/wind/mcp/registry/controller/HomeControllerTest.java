package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.service.OriginToolExpoService;
import cn.com.wind.mcp.registry.service.OriginToolHttpService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * HomeController unit test
 * Using Mockito for dependency mocking
 *
 * @author system
 * @date Created in 2025-10-14
 */
class HomeControllerTest {

    @InjectMocks
    private HomeController homeController;

    @Mock
    private McpToolService mcpToolService;

    @Mock
    private OriginToolHttpService originToolHttpService;

    @Mock
    private OriginToolExpoService originToolExpoService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test index page - not logged in should redirect to login
     */
    @Test
    void testIndex_NotLoggedIn_ShouldRedirectToLogin() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        String viewName = homeController.index(model, session);

        assertEquals("redirect:/provider/login", viewName);
        verify(model, never()).addAttribute(anyString(), any());
    }

    /**
     * Test index page - logged in user should see dashboard with counts
     */
    @Test
    void testIndex_LoggedIn_ShouldShowIndexPage() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(mcpToolService.countByProviderId(1L)).thenReturn(5L);
        when(originToolHttpService.countByProviderId(1L)).thenReturn(3L);
        when(originToolExpoService.countByProviderId(1L)).thenReturn(2L);

        String viewName = homeController.index(model, session);

        assertEquals("index", viewName);
        verify(model).addAttribute(eq("mcpToolCount"), eq(5L));
        verify(model).addAttribute(eq("httpToolCount"), eq(3L));
        verify(model).addAttribute(eq("expoToolCount"), eq(2L));
        verify(model).addAttribute(eq("totalToolCount"), eq(10L));
    }

    /**
     * Test index page - logged in with zero counts
     */
    @Test
    void testIndex_LoggedIn_ZeroCounts() {
        Provider provider = new Provider();
        provider.setId(2L);
        provider.setUsername("newuser");

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(mcpToolService.countByProviderId(2L)).thenReturn(0L);
        when(originToolHttpService.countByProviderId(2L)).thenReturn(0L);
        when(originToolExpoService.countByProviderId(2L)).thenReturn(0L);

        String viewName = homeController.index(model, session);

        assertEquals("index", viewName);
        verify(model).addAttribute(eq("totalToolCount"), eq(0L));
    }

    /**
     * Test about page
     */
    @Test
    void testAbout_ShouldReturnAboutPage() {
        String viewName = homeController.about();
        assertEquals("about", viewName);
    }
}
