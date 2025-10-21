package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.ProviderApp;
import cn.com.wind.mcp.registry.mapper.ProviderAppMapper;
import cn.com.wind.mcp.registry.service.ProviderAppService;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 提供者应用服务节点服务实现类
 * <p>
 * 实现应用节点的增删改查和业务操作功能
 * </p>
 *
 * @author system
 * @date Created in 2025-10-11
 */
@Slf4j
@Service
public class ProviderAppServiceImpl extends ServiceImpl<ProviderAppMapper, ProviderApp> implements ProviderAppService {

    /**
     * IP地址正则表达式
     */
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    @Override
    public List<ProviderApp> listByProviderId(Long providerId) {
        if (providerId == null) {
            return new ArrayList<>();
        }
        return baseMapper.selectByProviderId(providerId);
    }

    @Override
    public List<ProviderApp> listByProviderIdAndAppName(Long providerId, String appName) {
        if (providerId == null || StrUtil.isBlank(appName)) {
            return new ArrayList<>();
        }
        return baseMapper.selectByProviderIdAndAppName(providerId, appName);
    }

    @Override
    public List<ProviderApp> listEnabledByProviderId(Long providerId) {
        if (providerId == null) {
            return new ArrayList<>();
        }
        return baseMapper.selectEnabledByProviderId(providerId);
    }

    @Override
    public boolean createApp(ProviderApp providerApp) {
        try {
            // 验证配置有效性
            if (!validateAppConfig(providerApp)) {
                log.warn("应用节点配置无效");
                return false;
            }

            // 设置默认值
            if (providerApp.getLoadFactor() == null) {
                providerApp.setLoadFactor(1);
            }
            if (providerApp.getRequestTimeout() == null) {
                providerApp.setRequestTimeout(60);
            }
            if (providerApp.getMaxFailCount() == null) {
                providerApp.setMaxFailCount(3);
            }
            if (providerApp.getIsEnabled() == null) {
                providerApp.setIsEnabled(true);
            }
            if (providerApp.getHealthCheckInterval() == null) {
                providerApp.setHealthCheckInterval(5000);
            }
            if (providerApp.getStatus() == null) {
                providerApp.setStatus(1);
            }

            providerApp.setCreateTime(LocalDateTime.now());
            providerApp.setLastUpdateTime(LocalDateTime.now());

            return save(providerApp);
        } catch (Exception e) {
            log.error("创建应用节点失败", e);
            return false;
        }
    }

    @Override
    public boolean updateApp(ProviderApp providerApp) {
        try {
            // 验证配置有效性
            if (!validateAppConfig(providerApp)) {
                log.warn("应用节点配置无效");
                return false;
            }

            providerApp.setLastUpdateTime(LocalDateTime.now());
            return updateById(providerApp);
        } catch (Exception e) {
            log.error("更新应用节点失败", e);
            return false;
        }
    }

    @Override
    public boolean toggleEnable(Long id, Boolean isEnabled) {
        try {
            ProviderApp providerApp = getById(id);
            if (providerApp == null) {
                log.warn("应用节点不存在: {}", id);
                return false;
            }

            providerApp.setIsEnabled(isEnabled);
            providerApp.setLastUpdateTime(LocalDateTime.now());
            return updateById(providerApp);
        } catch (Exception e) {
            log.error("启用/禁用应用节点失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteApp(Long id) {
        try {
            ProviderApp providerApp = getById(id);
            if (providerApp == null) {
                log.warn("应用节点不存在: {}", id);
                return false;
            }

            // 逻辑删除
            providerApp.setStatus(-1);
            providerApp.setLastUpdateTime(LocalDateTime.now());
            return updateById(providerApp);
        } catch (Exception e) {
            log.error("删除应用节点失败", e);
            return false;
        }
    }

    @Override
    public boolean batchDeleteApps(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return false;
            }

            for (Long id : ids) {
                deleteApp(id);
            }
            return true;
        } catch (Exception e) {
            log.error("批量删除应用节点失败", e);
            return false;
        }
    }

    @Override
    public boolean validateAppConfig(ProviderApp providerApp) {
        if (providerApp == null) {
            return false;
        }

        // 验证必填字段
        if (providerApp.getProviderId() == null) {
            log.warn("提供者ID不能为空");
            return false;
        }

        if (StrUtil.isBlank(providerApp.getAppName())) {
            log.warn("应用名称不能为空");
            return false;
        }

        if (StrUtil.isBlank(providerApp.getAppIp())) {
            log.warn("应用IP不能为空");
            return false;
        }

        // 验证IP格式
        if (!IP_PATTERN.matcher(providerApp.getAppIp()).matches()) {
            log.warn("应用IP格式无效: {}", providerApp.getAppIp());
            return false;
        }

        // 验证端口范围
        if (providerApp.getAppPort() == null || providerApp.getAppPort() < 1 || providerApp.getAppPort() > 65535) {
            log.warn("应用端口无效: {}", providerApp.getAppPort());
            return false;
        }

        // 验证负载因子
        if (providerApp.getLoadFactor() != null && providerApp.getLoadFactor() < 1) {
            log.warn("负载因子必须大于0");
            return false;
        }

        // 验证超时时间
        if (providerApp.getRequestTimeout() != null && providerApp.getRequestTimeout() < 1) {
            log.warn("请求超时时间必须大于0");
            return false;
        }

        // 验证最大失败次数
        if (providerApp.getMaxFailCount() != null && providerApp.getMaxFailCount() < 1) {
            log.warn("最大失败次数必须大于0");
            return false;
        }

        return true;
    }

    @Override
    public String checkHealth(Long id) {
        try {
            ProviderApp providerApp = getById(id);
            if (providerApp == null) {
                return "节点不存在";
            }

            if (!providerApp.getIsEnabled()) {
                return "节点已禁用";
            }

            // 如果配置了健康检查地址,则进行健康检查
            if (StrUtil.isNotBlank(providerApp.getHealthCheckUrl())) {
                try {
                    String url = providerApp.getHealthCheckUrl();
                    int timeout = providerApp.getRequestTimeout() * 1000; // 转换为毫秒
                    String response = HttpUtil.get(url, timeout);
                    return "健康检查成功: " + response;
                } catch (Exception e) {
                    log.error("健康检查失败: {}", e.getMessage());
                    return "健康检查失败: " + e.getMessage();
                }
            } else {
                // 如果没有配置健康检查地址,则简单检查IP和端口可达性
                return "未配置健康检查地址,无法进行健康检查";
            }
        } catch (Exception e) {
            log.error("检查健康状态失败", e);
            return "检查失败: " + e.getMessage();
        }
    }
}
