package cn.com.wind.mcp.registry.mapper;

import java.time.LocalDateTime;

import cn.com.wind.mcp.registry.entity.Provider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Provider Mapper 测试
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
@ActiveProfiles("jenkins")
public class ProviderMapperTest {

    @Autowired
    private ProviderMapper providerMapper;

    @Test
    void testInsertProvider() {
        Provider provider = new Provider();
        provider.setUsername("test_provider");
        provider.setPassword("test_password");
        provider.setSalt("test_salt");
        provider.setEmail("test@example.com");
        provider.setPhoneNumber("13800138000");
        provider.setCompanyName("Test Company");
        provider.setContactPerson("Test User");
        provider.setApiKey("test_api_key");
        provider.setApiSecret("test_api_secret");
        provider.setStatus(1);
        provider.setCreateTime(LocalDateTime.now());
        provider.setLastUpdateTime(LocalDateTime.now());

        int result = providerMapper.insert(provider);
        assertEquals(1, result);
        assertNotNull(provider.getId());
    }

    @Test
    void testSelectById() {
        // 先插入一个provider
        Provider provider = new Provider();
        provider.setUsername("test_select");
        provider.setPassword("test_password");
        provider.setSalt("test_salt");
        provider.setStatus(1);
        provider.setCreateTime(LocalDateTime.now());
        provider.setLastUpdateTime(LocalDateTime.now());

        providerMapper.insert(provider);
        Long id = provider.getId();

        // 查询
        Provider selected = providerMapper.selectById(id);
        assertNotNull(selected);
        assertEquals("test_select", selected.getUsername());
        assertEquals("test_password", selected.getPassword());
        assertEquals("test_salt", selected.getSalt());
        assertEquals(1, selected.getStatus());
    }

    @Test
    void testUpdateById() {
        // 先插入一个provider
        Provider provider = new Provider();
        provider.setUsername("test_update");
        provider.setPassword("test_password");
        provider.setSalt("test_salt");
        provider.setStatus(1);
        provider.setCreateTime(LocalDateTime.now());
        provider.setLastUpdateTime(LocalDateTime.now());

        providerMapper.insert(provider);
        Long id = provider.getId();

        // 更新
        provider.setEmail("updated@example.com");
        provider.setCompanyName("Updated Company");
        provider.setLastUpdateTime(LocalDateTime.now());

        int result = providerMapper.updateById(provider);
        assertEquals(1, result);

        // 验证更新
        Provider updated = providerMapper.selectById(id);
        assertEquals("updated@example.com", updated.getEmail());
        assertEquals("Updated Company", updated.getCompanyName());
    }

    @Test
    void testDeleteById() {
        // 先插入一个provider
        Provider provider = new Provider();
        provider.setUsername("test_delete");
        provider.setPassword("test_password");
        provider.setSalt("test_salt");
        provider.setStatus(1);
        provider.setCreateTime(LocalDateTime.now());
        provider.setLastUpdateTime(LocalDateTime.now());

        providerMapper.insert(provider);
        Long id = provider.getId();

        // 删除
        int result = providerMapper.deleteById(id);
        assertEquals(1, result);

        // 验证删除
        Provider deleted = providerMapper.selectById(id);
        assertNull(deleted);
    }
}