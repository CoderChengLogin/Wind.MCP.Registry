package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.entity.Provider;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 提供者服务接口
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
public interface ProviderService extends IService<Provider> {

    /**
     * 用户注册
     */
    boolean register(String username, String password, String email, String phoneNumber);

    /**
     * 用户登录
     */
    Provider login(String username, String password);

    /**
     * 根据用户名查找用户
     */
    Provider findByUsername(String username);

    /**
     * 生成盐值
     */
    String generateSalt();

    /**
     * 密码加密
     */
    String encodePassword(String rawPassword, String salt);

    /**
     * 验证密码
     */
    boolean verifyPassword(String rawPassword, String encodedPassword, String salt);

    /**
     * 生成API密钥
     */
    String generateApiKey(String username);

    /**
     * 生成API密钥对
     */
    String generateApiSecret(String username);
}