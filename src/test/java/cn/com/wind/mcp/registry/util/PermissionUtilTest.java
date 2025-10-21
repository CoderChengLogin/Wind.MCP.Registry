package cn.com.wind.mcp.registry.util;

import cn.com.wind.mcp.registry.entity.Provider;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PermissionUtil单元测试
 * 测试权限控制工具类的所有静态方法
 *
 * @author system
 * @date Created in 2025-10-14
 */
class PermissionUtilTest {

    /**
     * 测试getCurrentProvider - session为null的情况
     */
    @Test
    void testGetCurrentProvider_NullSession_ShouldReturnNull() {
        Provider provider = PermissionUtil.getCurrentProvider(null);
        assertNull(provider);
    }

    /**
     * 测试getCurrentProvider - session中没有currentProvider属性
     */
    @Test
    void testGetCurrentProvider_NoProviderInSession_ShouldReturnNull() {
        MockHttpSession session = new MockHttpSession();
        Provider provider = PermissionUtil.getCurrentProvider(session);
        assertNull(provider);
    }

    /**
     * 测试getCurrentProvider - session中有有效的currentProvider
     */
    @Test
    void testGetCurrentProvider_ValidSession_ShouldReturnProvider() {
        MockHttpSession session = new MockHttpSession();
        Provider expectedProvider = new Provider();
        expectedProvider.setId(1L);
        expectedProvider.setUsername("testuser");
        session.setAttribute("currentProvider", expectedProvider);

        Provider actualProvider = PermissionUtil.getCurrentProvider(session);

        assertNotNull(actualProvider);
        assertEquals(expectedProvider.getId(), actualProvider.getId());
        assertEquals(expectedProvider.getUsername(), actualProvider.getUsername());
    }

    /**
     * 测试hasPermission - session为null
     */
    @Test
    void testHasPermission_NullSession_ShouldReturnFalse() {
        boolean hasPermission = PermissionUtil.hasPermission(null, 1L);
        assertFalse(hasPermission);
    }

    /**
     * 测试hasPermission - toolProviderId为null
     */
    @Test
    void testHasPermission_NullToolProviderId_ShouldReturnFalse() {
        MockHttpSession session = new MockHttpSession();
        Provider provider = new Provider();
        provider.setId(1L);
        session.setAttribute("currentProvider", provider);

        boolean hasPermission = PermissionUtil.hasPermission(session, null);
        assertFalse(hasPermission);
    }

    /**
     * 测试hasPermission - 未登录用户
     */
    @Test
    void testHasPermission_NotLoggedIn_ShouldReturnFalse() {
        MockHttpSession session = new MockHttpSession();
        boolean hasPermission = PermissionUtil.hasPermission(session, 1L);
        assertFalse(hasPermission);
    }

    /**
     * 测试hasPermission - 用户ID匹配,有权限
     */
    @Test
    void testHasPermission_MatchingProviderId_ShouldReturnTrue() {
        MockHttpSession session = new MockHttpSession();
        Provider provider = new Provider();
        provider.setId(100L);
        session.setAttribute("currentProvider", provider);

        boolean hasPermission = PermissionUtil.hasPermission(session, 100L);
        assertTrue(hasPermission);
    }

    /**
     * 测试hasPermission - 用户ID不匹配,无权限
     */
    @Test
    void testHasPermission_DifferentProviderId_ShouldReturnFalse() {
        MockHttpSession session = new MockHttpSession();
        Provider provider = new Provider();
        provider.setId(100L);
        session.setAttribute("currentProvider", provider);

        boolean hasPermission = PermissionUtil.hasPermission(session, 200L);
        assertFalse(hasPermission);
    }

    /**
     * 测试isLoggedIn - session为null
     */
    @Test
    void testIsLoggedIn_NullSession_ShouldReturnFalse() {
        boolean isLoggedIn = PermissionUtil.isLoggedIn(null);
        assertFalse(isLoggedIn);
    }

    /**
     * 测试isLoggedIn - session中没有用户信息
     */
    @Test
    void testIsLoggedIn_NoProviderInSession_ShouldReturnFalse() {
        MockHttpSession session = new MockHttpSession();
        boolean isLoggedIn = PermissionUtil.isLoggedIn(session);
        assertFalse(isLoggedIn);
    }

    /**
     * 测试isLoggedIn - 用户已登录
     */
    @Test
    void testIsLoggedIn_ValidProvider_ShouldReturnTrue() {
        MockHttpSession session = new MockHttpSession();
        Provider provider = new Provider();
        provider.setId(1L);
        session.setAttribute("currentProvider", provider);

        boolean isLoggedIn = PermissionUtil.isLoggedIn(session);
        assertTrue(isLoggedIn);
    }

    /**
     * 测试getCurrentProviderId - session为null
     */
    @Test
    void testGetCurrentProviderId_NullSession_ShouldReturnNull() {
        Long providerId = PermissionUtil.getCurrentProviderId(null);
        assertNull(providerId);
    }

    /**
     * 测试getCurrentProviderId - session中没有用户信息
     */
    @Test
    void testGetCurrentProviderId_NoProviderInSession_ShouldReturnNull() {
        MockHttpSession session = new MockHttpSession();
        Long providerId = PermissionUtil.getCurrentProviderId(session);
        assertNull(providerId);
    }

    /**
     * 测试getCurrentProviderId - 正常获取用户ID
     */
    @Test
    void testGetCurrentProviderId_ValidProvider_ShouldReturnId() {
        MockHttpSession session = new MockHttpSession();
        Provider provider = new Provider();
        provider.setId(999L);
        session.setAttribute("currentProvider", provider);

        Long providerId = PermissionUtil.getCurrentProviderId(session);

        assertNotNull(providerId);
        assertEquals(999L, providerId);
    }
}
