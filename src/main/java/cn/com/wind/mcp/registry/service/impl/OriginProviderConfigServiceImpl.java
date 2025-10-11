package cn.com.wind.mcp.registry.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.mapper.OriginProviderConfigMapper;
import cn.com.wind.mcp.registry.service.OriginProviderConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 提供商应用配置服务实现类
 * <p>
 * 实现origin_provider_config表的业务逻辑操作
 * </p>
 *
 * @author Claude Code
 * @date 2025-10-11
 */
@Slf4j
@Service
public class OriginProviderConfigServiceImpl extends ServiceImpl<OriginProviderConfigMapper, OriginProviderConfig>
    implements OriginProviderConfigService {

    /**
     * 根据应用编号查询配置
     *
     * @param appNum 应用编号
     * @return 应用配置信息
     */
    @Override
    public OriginProviderConfig getByAppNum(Long appNum) {
        log.debug("查询应用编号为 {} 的配置", appNum);
        return baseMapper.selectByAppNum(appNum);
    }

    /**
     * 根据提供商ID查询所有应用配置
     *
     * @param providerId 提供商ID
     * @return 应用配置列表
     */
    @Override
    public List<OriginProviderConfig> getByProviderId(Long providerId) {
        log.debug("查询提供商ID为 {} 的所有应用配置", providerId);
        return baseMapper.selectByProviderId(providerId);
    }

    /**
     * 根据站点类型查询应用配置
     *
     * @param siteType 站点类型
     * @return 应用配置列表
     */
    @Override
    public List<OriginProviderConfig> getBySiteType(String siteType) {
        log.debug("查询站点类型为 {} 的应用配置", siteType);
        return baseMapper.selectBySiteType(siteType);
    }

    /**
     * 查询所有启用的应用配置
     *
     * @return 启用的应用配置列表
     */
    @Override
    public List<OriginProviderConfig> getEnabledConfigs() {
        log.debug("查询所有启用的应用配置");
        return baseMapper.selectEnabledConfigs();
    }

    /**
     * 根据应用编号和环境查询配置项
     *
     * @param appNum 应用编号
     * @param env    环境标识
     * @return 配置项列表
     */
    @Override
    public List<OriginProviderConfig> getConfigsByAppNumAndEnv(Long appNum, String env) {
        log.debug("查询应用编号 {} 在环境 {} 下的配置项", appNum, env);
        return baseMapper.selectConfigsByAppNumAndEnv(appNum, env);
    }

    /**
     * 创建新的应用配置
     *
     * @param config 应用配置信息
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createConfig(OriginProviderConfig config) {
        try {
            log.info("创建新的应用配置: {}", config.getAppName());
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            if (config.getStatus() == null) {
                config.setStatus(1);
            }
            if (config.getIsEnabled() == null) {
                config.setIsEnabled(true);
            }
            return save(config);
        } catch (Exception e) {
            log.error("创建应用配置失败", e);
            throw e;
        }
    }

    /**
     * 更新应用配置
     *
     * @param config 应用配置信息
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(OriginProviderConfig config) {
        try {
            log.info("更新应用配置: ID={}", config.getId());
            config.setUpdateTime(LocalDateTime.now());
            return updateById(config);
        } catch (Exception e) {
            log.error("更新应用配置失败", e);
            throw e;
        }
    }

    /**
     * 删除应用配置（逻辑删除）
     *
     * @param id 配置ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(Long id) {
        try {
            log.info("逻辑删除应用配置: ID={}", id);
            OriginProviderConfig config = getById(id);
            if (config != null) {
                config.setStatus(-1);
                config.setUpdateTime(LocalDateTime.now());
                return updateById(config);
            }
            return false;
        } catch (Exception e) {
            log.error("删除应用配置失败", e);
            throw e;
        }
    }

    /**
     * 启用应用配置
     *
     * @param id 配置ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableConfig(Long id) {
        try {
            log.info("启用应用配置: ID={}", id);
            OriginProviderConfig config = getById(id);
            if (config != null) {
                config.setIsEnabled(true);
                config.setStatus(1);
                config.setUpdateTime(LocalDateTime.now());
                return updateById(config);
            }
            return false;
        } catch (Exception e) {
            log.error("启用应用配置失败", e);
            throw e;
        }
    }

    /**
     * 禁用应用配置
     *
     * @param id 配置ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableConfig(Long id) {
        try {
            log.info("禁用应用配置: ID={}", id);
            OriginProviderConfig config = getById(id);
            if (config != null) {
                config.setIsEnabled(false);
                config.setStatus(0);
                config.setUpdateTime(LocalDateTime.now());
                return updateById(config);
            }
            return false;
        } catch (Exception e) {
            log.error("禁用应用配置失败", e);
            throw e;
        }
    }
}
