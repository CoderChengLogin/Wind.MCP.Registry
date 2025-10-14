package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.Provider;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class ProviderServiceImplTest {

    private ProviderServiceImpl service; // 不注入baseMapper

    @BeforeEach
    void setUp() {
        service = spy(new ProviderServiceImpl());
    }

    @Test
    void testFindByUsername_Found() {
        Provider expected = new Provider();
        doReturn(expected).when(service).getOne(any(QueryWrapper.class));
        Provider result = service.findByUsername("hello123");
        assertSame(expected, result);
    }

    @Test
    void testFindByUsername_Blank_returnsNull() {
        assertNull(service.findByUsername(""));
        assertNull(service.findByUsername(null));
    }

    @Test
    void testRegister_UserExists() {
        doReturn(new Provider()).when(service).findByUsername(anyString());
        boolean result = service.register("user", "pwd", "email@abc.com", "123456");
        assertFalse(result);
    }

    @Test
    void testRegister_EmailExists() {
        doReturn(null).when(service).findByUsername(anyString());
        doReturn(new Provider()).when(service).getOne(any(QueryWrapper.class));
        boolean result = service.register("user", "pwd", "email@abc.com", "1111111");
        assertFalse(result);
    }

    @Test
    void testRegister_Success() {
        doReturn(null).when(service).findByUsername(anyString());
        doReturn(null).when(service).getOne(any(QueryWrapper.class));
        doReturn(true).when(service).save(any(Provider.class));
        boolean result = service.register("success", "secret", "aa@bb.com", "15500001111");
        assertTrue(result);
    }

    @Test
    void testRegister_Exception() {
        doReturn(null).when(service).findByUsername(anyString());
        doThrow(new RuntimeException("fail insert")).when(service).save(any(Provider.class));
        boolean result = service.register("error", "aaa", "err@err.com", "");
        assertFalse(result);
    }

    @Test
    void testLogin_NoUserOrWrongPwd() {
        doReturn(null).when(service).findByUsername(anyString());
        assertNull(service.login("nouser", "pwd"));

        Provider exist = new Provider();
        exist.setUsername("abc");
        exist.setPassword(service.encodePassword("right", "salt"));
        exist.setSalt("salt");
        doReturn(exist).when(service).findByUsername(eq("abc"));
        assertNull(service.login("abc", "wrong"));
    }

    @Test
    void testLogin_Success() {
        Provider exist = new Provider();
        exist.setId(1L);
        exist.setUsername("ttuser");
        exist.setSalt("sss");
        exist.setPassword(service.encodePassword("pass123", "sss"));
        doReturn(exist).when(service).findByUsername(eq("ttuser"));
        doReturn(true).when(service).updateById(any(Provider.class));
        Provider result = service.login("ttuser", "pass123");
        assertNotNull(result);
        assertEquals("ttuser", result.getUsername());
    }

    @Test
    void testApiKeySecret() {
        String key = service.generateApiKey("x");
        assertTrue(key.startsWith("mcp_"));
        String sec = service.generateApiSecret("x");
        assertEquals(32, sec.length());
    }

    @Test
    void testUpdateProfile_NoProvider() {
        doReturn(null).when(service).getById(anyLong());
        boolean res = service.updateProfile(1L, "e", "p", "c", "cp");
        assertFalse(res);
    }

    @Test
    void testUpdateProfile_EmailConflict() {
        Provider p = new Provider();
        p.setId(1L);
        p.setEmail("a@b.com");
        doReturn(p).when(service).getById(eq(1L));
        doReturn(new Provider()).when(service).getOne(any(QueryWrapper.class));
        boolean res = service.updateProfile(1L, "b@b.com", "p2", "c2", "cp2");
        assertFalse(res);
    }

    @Test
    void testUpdateProfile_Success() {
        Provider p = new Provider();
        p.setId(2L);
        p.setEmail("a@b.com");
        doReturn(p).when(service).getById(eq(2L));
        doReturn(null).when(service).getOne(any(QueryWrapper.class));
        doReturn(true).when(service).updateById(any(Provider.class));
        boolean res = service.updateProfile(2L, "a@b.com", "x", "c", "cp");
        assertTrue(res);
    }

    @Test
    void testRegenerateApiKey_NoUser() {
        doReturn(null).when(service).getById(eq(55L));
        assertNull(service.regenerateApiKey(55L));
    }

    @Test
    void testRegenerateApiKey_Success() {
        Provider p = new Provider();
        p.setId(66L);
        p.setUsername("uuu");
        doReturn(p).when(service).getById(eq(66L));
        doReturn(true).when(service).updateById(any(Provider.class));
        String k = service.regenerateApiKey(66L);
        assertNotNull(k);
        assertTrue(k.startsWith("mcp_"));
    }
}