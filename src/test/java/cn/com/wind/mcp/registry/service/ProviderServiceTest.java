package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.entity.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Provider服务测试
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
@ActiveProfiles("jenkins")
public class ProviderServiceTest {

    @Autowired
    private ProviderService providerService;

    private String testUsername;
    private String testPassword;
    private String testEmail;
    private String testPhone;

    @BeforeEach
    void setUp() {
        testUsername = "testprovider_" + System.currentTimeMillis();
        testPassword = "testpass123";
        testEmail = "test_" + System.currentTimeMillis() + "@example.com";
        testPhone = "13800138000";
    }

    @Test
    void testProviderRegistration_Success() {
        // 测试成功注册
        boolean result = providerService.register(testUsername, testPassword, testEmail, testPhone);
        assertTrue(result, "提供者注册应该成功");

        // 验证提供者确实被创建
        Provider provider = providerService.findByUsername(testUsername);
        assertNotNull(provider, "注册后应该能找到提供者");
        assertEquals(testUsername, provider.getUsername(), "用户名应该匹配");
        assertEquals(testEmail, provider.getEmail(), "邮箱应该匹配");
        assertEquals(testPhone, provider.getPhoneNumber(), "手机号应该匹配");
        assertEquals(1, provider.getStatus(), "新提供者状态应该是启用的");
        assertNotNull(provider.getApiKey(), "应该生成API密钥");
        assertNotNull(provider.getApiSecret(), "应该生成API密钥对");
    }

    @Test
    void testProviderRegistration_DuplicateUsername() {
        // 先注册一个提供者
        providerService.register(testUsername, testPassword, testEmail, testPhone);

        // 尝试用相同用户名再次注册
        boolean result = providerService.register(testUsername, "newpass123", "new@example.com", "13900139000");
        assertFalse(result, "相同用户名注册应该失败");
    }

    @Test
    void testProviderRegistration_DuplicateEmail() {
        // 先注册一个提供者
        providerService.register(testUsername, testPassword, testEmail, testPhone);

        // 尝试用相同邮箱注册不同用户名
        boolean result = providerService.register("differentprovider", "newpass123", testEmail, "13900139000");
        assertFalse(result, "相同邮箱注册应该失败");
    }

    @Test
    void testProviderLogin_Success() {
        // 先注册提供者
        providerService.register(testUsername, testPassword, testEmail, testPhone);

        // 测试登录
        Provider provider = providerService.login(testUsername, testPassword);
        assertNotNull(provider, "正确的用户名和密码应该登录成功");
        assertEquals(testUsername, provider.getUsername(), "返回的提供者信息应该正确");
        assertNotNull(provider.getLastLoginTime(), "应该更新最后登录时间");
    }

    @Test
    void testProviderLogin_WrongPassword() {
        // 先注册提供者
        providerService.register(testUsername, testPassword, testEmail, testPhone);

        // 测试错误密码登录
        Provider provider = providerService.login(testUsername, "wrongpassword");
        assertNull(provider, "错误密码应该登录失败");
    }

    @Test
    void testProviderLogin_NonexistentUser() {
        // 测试不存在的提供者登录
        Provider provider = providerService.login("nonexistent", "password");
        assertNull(provider, "不存在的提供者应该登录失败");
    }

    @Test
    void testPasswordEncryption() {
        String salt = providerService.generateSalt();
        assertNotNull(salt, "盐值不应该为空");
        assertEquals(16, salt.length(), "盐值长度应该是16");

        String rawPassword = "testpassword123";
        String encodedPassword = providerService.encodePassword(rawPassword, salt);
        assertNotNull(encodedPassword, "加密后的密码不应该为空");
        assertNotEquals(rawPassword, encodedPassword, "加密后的密码不应该等于原始密码");

        // 验证密码
        assertTrue(providerService.verifyPassword(rawPassword, encodedPassword, salt), "密码验证应该成功");
        assertFalse(providerService.verifyPassword("wrongpassword", encodedPassword, salt), "错误密码验证应该失败");
    }

    @Test
    void testFindByUsername() {
        // 先注册提供者
        providerService.register(testUsername, testPassword, testEmail, testPhone);

        // 测试查找提供者
        Provider provider = providerService.findByUsername(testUsername);
        assertNotNull(provider, "应该能找到注册的提供者");
        assertEquals(testUsername, provider.getUsername(), "找到的用户名应该匹配");

        // 测试查找不存在的提供者
        Provider nonexistentProvider = providerService.findByUsername("nonexistent");
        assertNull(nonexistentProvider, "不存在的提供者应该返回null");
    }

    @Test
    void testApiKeyGeneration() {
        String apiKey1 = providerService.generateApiKey("test1");
        String apiKey2 = providerService.generateApiKey("test2");
        String apiSecret1 = providerService.generateApiSecret("test1");
        String apiSecret2 = providerService.generateApiSecret("test2");

        assertNotNull(apiKey1, "应该生成API密钥");
        assertNotNull(apiKey2, "应该生成API密钥");
        assertNotNull(apiSecret1, "应该生成API密钥对");
        assertNotNull(apiSecret2, "应该生成API密钥对");

        assertNotEquals(apiKey1, apiKey2, "不同用户的API密钥应该不同");
        assertNotEquals(apiSecret1, apiSecret2, "不同用户的API密钥对应该不同");

        assertTrue(apiKey1.startsWith("mcp_"), "API密钥应该以'mcp_'开头");
        assertEquals(28, apiKey1.length(), "API密钥长度应该是28");
        assertEquals(32, apiSecret1.length(), "API密钥对长度应该是32");
    }

    @Test
    void testProviderRegistrationWithMinimalInfo() {
        // 测试只提供必要信息的注册
        boolean result = providerService.register(testUsername, testPassword, "", null);
        assertTrue(result, "只提供用户名和密码的注册应该成功");

        Provider provider = providerService.findByUsername(testUsername);
        assertNotNull(provider, "应该能找到注册的提供者");
        assertEquals("", provider.getEmail(), "邮箱应该是空字符串");
        assertNull(provider.getPhoneNumber(), "手机号应该为空");
        assertNotNull(provider.getApiKey(), "应该生成API密钥");
        assertNotNull(provider.getApiSecret(), "应该生成API密钥对");
    }
}