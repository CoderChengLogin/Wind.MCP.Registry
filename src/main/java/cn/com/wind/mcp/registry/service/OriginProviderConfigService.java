package cn.com.wind.mcp.registry.service;

import java.util.List;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 提供商应用配置服务接口
 * <p>
 * 提供origin_provider_config表的业务逻辑操作
 * </p>
 *
 * @author Claude Code
 * @date 2025-10-11
 */
public interface OriginProviderConfigService extends IService<OriginProviderConfig> {

    /**
     * 根据应用编号查询配置
     *
     * @param appNum 应用编号
     * @return 应用配置信息
     */
    OriginProviderConfig getByAppNum(Long appNum);

    /**
     * 根据提供商ID查询所有应用配置
     *
     * @param providerId 提供商ID
     * @return 应用配置列表
     */
    List<OriginProviderConfig> getByProviderId(Long providerId);

    /**
     * 根据站点类型查询应用配置
     *
     * @param siteType 站点类型
     * @return 应用配置列表
     */
    List<OriginProviderConfig> getBySiteType(String siteType);

    /**
     * 查询所有启用的应用配置
     *
     * @return 启用的应用配置列表
     */
    List<OriginProviderConfig> getEnabledConfigs();

    /**
     * 根据应用编号和环境查询配置项
     *
     * @param appNum 应用编号
     * @param env    环境标识
     * @return 配置项列表
     */
    List<OriginProviderConfig> getConfigsByAppNumAndEnv(Long appNum, String env);

    /**
     * 创建新的应用配置
     *
     * @param config 应用配置信息
     * @return 是否成功
     */
    boolean createConfig(OriginProviderConfig config);

    /**
     * 更新应用配置
     *
     * @param config 应用配置信息
     * @return 是否成功
     */
    boolean updateConfig(OriginProviderConfig config);

    /**
     * 删除应用配置（逻辑删除）
     *
     * @param id 配置ID
     * @return 是否成功
     */
    boolean deleteConfig(Long id);

    /**
     * 启用应用配置
     *
     * @param id 配置ID
     * @return 是否成功
     */
    boolean enableConfig(Long id);

    /**
     * 禁用应用配置
     *
     * @param id 配置ID
     * @return 是否成功
     */
    boolean disableConfig(Long id);
}
