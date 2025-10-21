package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.mapper.ProviderMapper;
import cn.com.wind.mcp.registry.service.ProviderService;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 提供者服务实现类
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Slf4j
@Service
public class ProviderServiceImpl extends ServiceImpl<ProviderMapper, Provider> implements ProviderService {

    @Override
    public boolean register(String username, String password, String email, String phoneNumber) {
        try {
            // 检查用户名是否已存在
            Provider existingProvider = findByUsername(username);
            if (existingProvider != null) {
                log.warn("用户名已存在: {}", username);
                return false;
            }

            // 检查邮箱是否已存在
            if (StrUtil.isNotBlank(email)) {
                QueryWrapper<Provider> emailQuery = new QueryWrapper<>();
                emailQuery.eq("email", email).eq("status", 1);
                Provider existingEmailProvider = getOne(emailQuery);
                if (existingEmailProvider != null) {
                    log.warn("邮箱已存在: {}", email);
                    return false;
                }
            }

            // 生成盐值和加密密码
            String salt = generateSalt();
            String encodedPassword = encodePassword(password, salt);

            // 创建提供者
            Provider provider = new Provider();
            provider.setUsername(username);
            provider.setPassword(encodedPassword);
            provider.setSalt(salt);
            provider.setEmail(email);
            provider.setPhoneNumber(phoneNumber);
            provider.setApiKey(generateApiKey(username));
            provider.setApiSecret(generateApiSecret(username));
            provider.setStatus(1);
            provider.setCreateTime(LocalDateTime.now());
            provider.setLastUpdateTime(LocalDateTime.now());

            return save(provider);
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return false;
        }
    }

    @Override
    public Provider login(String username, String password) {
        try {
            Provider provider = findByUsername(username);
            if (provider == null) {
                log.warn("用户不存在: {}", username);
                return null;
            }

            if (verifyPassword(password, provider.getPassword(), provider.getSalt())) {
                // 更新最后登录时间
                provider.setLastLoginTime(LocalDateTime.now());
                updateById(provider);
                return provider;
            } else {
                log.warn("密码错误: {}", username);
                return null;
            }
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return null;
        }
    }

    @Override
    public Provider findByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return null;
        }
        QueryWrapper<Provider> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("status", 1);
        return getOne(queryWrapper);
    }

    @Override
    public String generateSalt() {
        return RandomUtil.randomString(16);
    }

    @Override
    public String encodePassword(String rawPassword, String salt) {
        return DigestUtil.md5Hex(rawPassword + salt);
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword, String salt) {
        String computedHash = encodePassword(rawPassword, salt);
        return computedHash.equals(encodedPassword);
    }

    @Override
    public String generateApiKey(String username) {
        String content = username + "_" + System.currentTimeMillis() + "_" + RandomUtil.randomString(8);
        return "mcp_" + DigestUtil.md5Hex(content).substring(0, 24);
    }

    @Override
    public String generateApiSecret(String username) {
        String content = username + "_secret_" + System.currentTimeMillis() + "_" + RandomUtil.randomString(16);
        return DigestUtil.sha256Hex(content).substring(0, 32);
    }

    @Override
    public boolean updateProfile(Long providerId, String email, String phoneNumber, String companyName,
                                 String contactPerson) {
        try {
            Provider provider = getById(providerId);
            if (provider == null) {
                log.warn("用户不存在: {}", providerId);
                return false;
            }

            // 检查邮箱是否被其他用户使用
            if (StrUtil.isNotBlank(email) && !email.equals(provider.getEmail())) {
                QueryWrapper<Provider> emailQuery = new QueryWrapper<>();
                emailQuery.eq("email", email).eq("status", 1).ne("id", providerId);
                Provider existingEmailProvider = getOne(emailQuery);
                if (existingEmailProvider != null) {
                    log.warn("邮箱已被其他用户使用: {}", email);
                    return false;
                }
            }

            // 更新字段
            provider.setEmail(email);
            provider.setPhoneNumber(phoneNumber);
            provider.setCompanyName(companyName);
            provider.setContactPerson(contactPerson);
            provider.setLastUpdateTime(LocalDateTime.now());

            return updateById(provider);
        } catch (Exception e) {
            log.error("更新用户资料失败", e);
            return false;
        }
    }

    @Override
    public String regenerateApiKey(Long providerId) {
        try {
            Provider provider = getById(providerId);
            if (provider == null) {
                log.warn("用户不存在: {}", providerId);
                return null;
            }

            // 生成新的API密钥和密钥对
            String newApiKey = generateApiKey(provider.getUsername());
            String newApiSecret = generateApiSecret(provider.getUsername());

            provider.setApiKey(newApiKey);
            provider.setApiSecret(newApiSecret);
            provider.setLastUpdateTime(LocalDateTime.now());

            boolean success = updateById(provider);
            return success ? newApiKey : null;
        } catch (Exception e) {
            log.error("重新生成API密钥失败", e);
            return null;
        }
    }
}