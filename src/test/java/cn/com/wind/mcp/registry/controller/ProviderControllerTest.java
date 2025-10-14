package cn.com.wind.mcp.registry.controller;

import java.util.Map;

import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.ProviderService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProviderController unit test
 * Tests all provider-related endpoints including registration, login, logout, and profile management
 *
 * @author system
 * @date Created in 2025-10-14
 */
class ProviderControllerTest {

    @InjectMocks
    private ProviderController providerController;

    @Mock
    private ProviderService providerService;

    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ========== Page Methods Tests ==========

    /**
     * Test register page - should return register view
     */
    @Test
    void testRegisterPage_ShouldReturnRegisterView() {
        String viewName = providerController.registerPage();
        assertEquals("provider/register", viewName);
    }

    /**
     * Test login page - should return login view
     */
    @Test
    void testLoginPage_ShouldReturnLoginView() {
        String viewName = providerController.loginPage();
        assertEquals("provider/login", viewName);
    }

    /**
     * Test profile page - not logged in should redirect to login
     */
    @Test
    void testProfilePage_NotLoggedIn_ShouldRedirectToLogin() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        String viewName = providerController.profilePage(session);

        assertEquals("redirect:/provider/login", viewName);
    }

    /**
     * Test profile page - logged in should return profile view
     */
    @Test
    void testProfilePage_LoggedIn_ShouldReturnProfileView() {
        Provider provider = new Provider();
        provider.setId(1L);
        when(session.getAttribute("currentProvider")).thenReturn(provider);

        String viewName = providerController.profilePage(session);

        assertEquals("provider/profile", viewName);
    }

    // ========== Register API Tests ==========

    /**
     * Test register - successful registration
     */
    @Test
    void testRegister_Success() {
        when(providerService.register(eq("testuser"), eq("password123"), eq("test@example.com"), eq("1234567890")))
            .thenReturn(true);

        Map<String, Object> result = providerController.register("testuser", "password123", "password123",
            "test@example.com", "1234567890", null);

        assertTrue((Boolean)result.get("success"));
        assertEquals("注册成功", result.get("message"));
        verify(providerService).register("testuser", "password123", "test@example.com", "1234567890");
    }

    /**
     * Test register - username or password is blank
     */
    @Test
    void testRegister_BlankUsernameOrPassword() {
        Map<String, Object> result = providerController.register("", "password", "password", null, null, null);

        assertFalse((Boolean)result.get("success"));
        assertEquals("用户名和密码不能为空", result.get("message"));
        verify(providerService, never()).register(anyString(), anyString(), anyString(), anyString());
    }

    /**
     * Test register - passwords do not match
     */
    @Test
    void testRegister_PasswordsDoNotMatch() {
        Map<String, Object> result = providerController.register("testuser", "password123", "password456",
            null, null, null);

        assertFalse((Boolean)result.get("success"));
        assertEquals("两次输入的密码不一致", result.get("message"));
        verify(providerService, never()).register(anyString(), anyString(), anyString(), anyString());
    }

    /**
     * Test register - username length invalid (too short)
     */
    @Test
    void testRegister_UsernameTooShort() {
        Map<String, Object> result = providerController.register("ab", "password123", "password123",
            null, null, null);

        assertFalse((Boolean)result.get("success"));
        assertEquals("用户名长度应在3-20个字符之间", result.get("message"));
    }

    /**
     * Test register - username length invalid (too long)
     */
    @Test
    void testRegister_UsernameTooLong() {
        String longUsername = "a".repeat(21);
        Map<String, Object> result = providerController.register(longUsername, "password123", "password123",
            null, null, null);

        assertFalse((Boolean)result.get("success"));
        assertEquals("用户名长度应在3-20个字符之间", result.get("message"));
    }

    /**
     * Test register - password too short
     */
    @Test
    void testRegister_PasswordTooShort() {
        Map<String, Object> result = providerController.register("testuser", "12345", "12345",
            null, null, null);

        assertFalse((Boolean)result.get("success"));
        assertEquals("密码长度不能少于6个字符", result.get("message"));
    }

    /**
     * Test register - username already exists
     */
    @Test
    void testRegister_UsernameExists() {
        when(providerService.register(anyString(), anyString(), anyString(), anyString())).thenReturn(false);

        Map<String, Object> result = providerController.register("testuser", "password123", "password123",
            null, null, null);

        assertFalse((Boolean)result.get("success"));
        assertEquals("用户名或邮箱已存在", result.get("message"));
    }

    /**
     * Test register - exception handling
     */
    @Test
    void testRegister_ExceptionHandling() {
        when(providerService.register(eq("testuser"), eq("password123"), anyString(), anyString()))
            .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerController.register("testuser", "password123", "password123",
            "test@example.com", "1234567890", null);

        assertFalse((Boolean)result.get("success"));
        assertEquals("注册失败，请稍后重试", result.get("message"));
    }

    // ========== Login API Tests ==========

    /**
     * Test login - successful login
     */
    @Test
    void testLogin_Success() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");
        provider.setEmail("test@example.com");

        when(providerService.login("testuser", "password123")).thenReturn(provider);

        Map<String, Object> result = providerController.login("testuser", "password123", session);

        assertTrue((Boolean)result.get("success"));
        assertEquals("登录成功", result.get("message"));
        verify(session).setAttribute("currentProvider", provider);

        @SuppressWarnings("unchecked")
        Map<String, Object> providerInfo = (Map<String, Object>)result.get("provider");
        assertEquals(1L, providerInfo.get("id"));
        assertEquals("testuser", providerInfo.get("username"));
    }

    /**
     * Test login - blank username or password
     */
    @Test
    void testLogin_BlankCredentials() {
        Map<String, Object> result = providerController.login("", "password", session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("用户名和密码不能为空", result.get("message"));
        verify(providerService, never()).login(anyString(), anyString());
    }

    /**
     * Test login - invalid credentials
     */
    @Test
    void testLogin_InvalidCredentials() {
        when(providerService.login("testuser", "wrongpassword")).thenReturn(null);

        Map<String, Object> result = providerController.login("testuser", "wrongpassword", session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
        verify(session, never()).setAttribute(anyString(), any());
    }

    /**
     * Test login - exception handling
     */
    @Test
    void testLogin_ExceptionHandling() {
        when(providerService.login(anyString(), anyString())).thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerController.login("testuser", "password", session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("登录失败，请稍后重试", result.get("message"));
    }

    // ========== Get Current Provider API Tests ==========

    /**
     * Test get current provider - user logged in
     */
    @Test
    void testGetCurrentProvider_LoggedIn() {
        Provider provider = new Provider();
        provider.setId(1L);
        provider.setUsername("testuser");
        provider.setEmail("test@example.com");
        provider.setApiKey("test-api-key");

        when(session.getAttribute("currentProvider")).thenReturn(provider);

        Map<String, Object> result = providerController.getCurrentProvider(session);

        assertTrue((Boolean)result.get("success"));

        @SuppressWarnings("unchecked")
        Map<String, Object> providerInfo = (Map<String, Object>)result.get("provider");
        assertEquals(1L, providerInfo.get("id"));
        assertEquals("testuser", providerInfo.get("username"));
        assertEquals("test-api-key", providerInfo.get("apiKey"));
    }

    /**
     * Test get current provider - user not logged in
     */
    @Test
    void testGetCurrentProvider_NotLoggedIn() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        Map<String, Object> result = providerController.getCurrentProvider(session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("用户未登录", result.get("message"));
    }

    // ========== Logout API Tests ==========

    /**
     * Test logout - successful logout
     */
    @Test
    void testLogout_Success() {
        Map<String, Object> result = providerController.logout(session);

        assertTrue((Boolean)result.get("success"));
        assertEquals("退出登录成功", result.get("message"));
        verify(session).removeAttribute("currentProvider");
        verify(session).invalidate();
    }

    /**
     * Test logout - exception handling
     */
    @Test
    void testLogout_ExceptionHandling() {
        doThrow(new RuntimeException("Session error")).when(session).invalidate();

        Map<String, Object> result = providerController.logout(session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("退出登录失败", result.get("message"));
    }

    // ========== Update Profile API Tests ==========

    /**
     * Test update profile - successful update
     */
    @Test
    void testUpdateProfile_Success() {
        Provider currentProvider = new Provider();
        currentProvider.setId(1L);
        currentProvider.setUsername("testuser");

        Provider updatedProvider = new Provider();
        updatedProvider.setId(1L);
        updatedProvider.setUsername("testuser");
        updatedProvider.setEmail("newemail@example.com");

        when(session.getAttribute("currentProvider")).thenReturn(currentProvider);
        when(providerService.updateProfile(eq(1L), eq("newemail@example.com"), eq("1234567890"),
            eq("NewCompany"), eq("John Doe"))).thenReturn(true);
        when(providerService.getById(1L)).thenReturn(updatedProvider);

        Map<String, Object> result = providerController.updateProfile("newemail@example.com",
            "1234567890", "NewCompany", "John Doe", session);

        assertTrue((Boolean)result.get("success"));
        assertEquals("个人信息更新成功", result.get("message"));
        verify(session).setAttribute("currentProvider", updatedProvider);
    }

    /**
     * Test update profile - user not logged in
     */
    @Test
    void testUpdateProfile_NotLoggedIn() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        Map<String, Object> result = providerController.updateProfile("email@test.com",
            null, null, null, session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("用户未登录", result.get("message"));
        verify(providerService, never()).updateProfile(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    /**
     * Test update profile - update failed
     */
    @Test
    void testUpdateProfile_UpdateFailed() {
        Provider provider = new Provider();
        provider.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(providerService.updateProfile(anyLong(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(false);

        Map<String, Object> result = providerController.updateProfile("email@test.com",
            null, null, null, session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("更新失败", result.get("message"));
    }

    /**
     * Test update profile - exception handling
     */
    @Test
    void testUpdateProfile_ExceptionHandling() {
        Provider provider = new Provider();
        provider.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(providerService.updateProfile(eq(1L), eq("email@test.com"), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerController.updateProfile("email@test.com",
            "123456", "Company", "Person", session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("更新失败，请稍后重试", result.get("message"));
    }

    // ========== Regenerate API Key Tests ==========

    /**
     * Test regenerate API key - successful regeneration
     */
    @Test
    void testRegenerateApiKey_Success() {
        Provider currentProvider = new Provider();
        currentProvider.setId(1L);

        Provider updatedProvider = new Provider();
        updatedProvider.setId(1L);
        updatedProvider.setApiKey("new-api-key-12345");

        when(session.getAttribute("currentProvider")).thenReturn(currentProvider);
        when(providerService.regenerateApiKey(1L)).thenReturn("new-api-key-12345");
        when(providerService.getById(1L)).thenReturn(updatedProvider);

        Map<String, Object> result = providerController.regenerateApiKey(session);

        assertTrue((Boolean)result.get("success"));
        assertEquals("API密钥重新生成成功", result.get("message"));
        assertEquals("new-api-key-12345", result.get("apiKey"));
        verify(session).setAttribute("currentProvider", updatedProvider);
    }

    /**
     * Test regenerate API key - user not logged in
     */
    @Test
    void testRegenerateApiKey_NotLoggedIn() {
        when(session.getAttribute("currentProvider")).thenReturn(null);

        Map<String, Object> result = providerController.regenerateApiKey(session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("用户未登录", result.get("message"));
        verify(providerService, never()).regenerateApiKey(anyLong());
    }

    /**
     * Test regenerate API key - generation failed
     */
    @Test
    void testRegenerateApiKey_GenerationFailed() {
        Provider provider = new Provider();
        provider.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(providerService.regenerateApiKey(1L)).thenReturn("");

        Map<String, Object> result = providerController.regenerateApiKey(session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("生成失败", result.get("message"));
    }

    /**
     * Test regenerate API key - exception handling
     */
    @Test
    void testRegenerateApiKey_ExceptionHandling() {
        Provider provider = new Provider();
        provider.setId(1L);

        when(session.getAttribute("currentProvider")).thenReturn(provider);
        when(providerService.regenerateApiKey(1L)).thenThrow(new RuntimeException("Database error"));

        Map<String, Object> result = providerController.regenerateApiKey(session);

        assertFalse((Boolean)result.get("success"));
        assertEquals("生成失败，请稍后重试", result.get("message"));
    }
}
