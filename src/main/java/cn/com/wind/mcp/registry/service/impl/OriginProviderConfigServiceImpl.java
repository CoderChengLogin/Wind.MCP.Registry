package cn.com.wind.mcp.registry.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.entity.OriginToolExpo;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.mapper.OriginProviderConfigMapper;
import cn.com.wind.mcp.registry.mapper.OriginToolExpoMapper;
import cn.com.wind.mcp.registry.mapper.OriginToolHttpMapper;
import cn.com.wind.mcp.registry.service.OriginProviderConfigService;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private OriginToolHttpMapper originToolHttpMapper;

    @Autowired
    private OriginToolExpoMapper originToolExpoMapper;

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
     * <p>
     * 使用Hutool雪花算法生成全局唯一的app_num
     * </p>
     *
     * @param config 应用配置信息
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createConfig(OriginProviderConfig config) {
        try {
            log.info("创建新的应用配置: {}", config.getAppName());

            // 使用Hutool雪花算法生成全局唯一的app_num
            if (config.getAppNum() == null) {
                Snowflake snowflake = IdUtil.getSnowflake(1, 1);
                long appNum = snowflake.nextId();
                config.setAppNum(appNum);
                log.info("为应用 {} 生成app_num: {}", config.getAppName(), appNum);
            }

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

    /**
     * 根据app_num查询关联的HTTP工具列表
     * <p>
     * 通过provider_app_num字段关联origin_tool_http表
     * </p>
     *
     * @param appNum 应用编号
     * @return HTTP工具列表
     */
    @Override
    public List<OriginToolHttp> getRelatedHttpTools(Long appNum) {
        log.info("查询app_num={} 关联的HTTP工具", appNum);
        QueryWrapper<OriginToolHttp> wrapper = new QueryWrapper<>();
        wrapper.eq("provider_app_num", appNum);
        List<OriginToolHttp> tools = originToolHttpMapper.selectList(wrapper);
        log.info("找到 {} 个关联的HTTP工具", tools.size());
        return tools;
    }

    /**
     * 根据app_num查询关联的Expo工具列表
     * <p>
     * 通过provider_app_num字段关联origin_tool_expo表
     * </p>
     *
     * @param appNum 应用编号
     * @return Expo工具列表
     */
    @Override
    public List<OriginToolExpo> getRelatedExpoTools(Long appNum) {
        log.info("查询app_num={} 关联的Expo工具", appNum);
        QueryWrapper<OriginToolExpo> wrapper = new QueryWrapper<>();
        wrapper.eq("provider_app_num", appNum);
        List<OriginToolExpo> tools = originToolExpoMapper.selectList(wrapper);
        log.info("找到 {} 个关联的Expo工具", tools.size());
        return tools;
    }

    /**
     * 获取所有负载均衡器列表(根据app_name聚合)
     * <p>
     * 将相同app_name的多条记录视为一个负载均衡器的多个服务节点
     * 通过DISTINCT查询获取所有唯一的app_name
     * </p>
     *
     * @return 负载均衡器名称列表(去重的app_name列表)
     */
    @Override
    public List<String> getAllLoadBalancerNames() {
        log.info("查询所有负载均衡器名称(app_name去重)");
        QueryWrapper<OriginProviderConfig> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT app_name");
        wrapper.eq("status", 1); // 只查询有效记录
        wrapper.isNotNull("app_name");
        wrapper.ne("app_name", ""); // 排除空字符串

        List<OriginProviderConfig> configs = list(wrapper);
        List<String> loadNames = configs.stream()
            .map(OriginProviderConfig::getAppName)
            .distinct()
            .sorted()
            .collect(java.util.stream.Collectors.toList());

        log.info("找到 {} 个负载均衡器", loadNames.size());
        return loadNames;
    }

    /**
     * 根据负载名称(app_name)查询该负载下的所有服务节点
     * <p>
     * 查询所有app_name等于指定负载名称的记录,每条记录代表一个服务节点
     * </p>
     *
     * @param loadName 负载名称(即app_name)
     * @return 该负载下的所有服务节点列表
     */
    @Override
    public List<OriginProviderConfig> getNodesByLoadName(String loadName) {
        log.info("查询负载 {} 下的所有服务节点", loadName);
        QueryWrapper<OriginProviderConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("app_name", loadName);
        wrapper.eq("status", 1); // 只查询有效记录
        wrapper.orderByAsc("site_type", "app_ip"); // 按站点类型和IP排序

        List<OriginProviderConfig> nodes = list(wrapper);
        log.info("负载 {} 共有 {} 个服务节点", loadName, nodes.size());
        return nodes;
    }

    /**
     * 根据负载名称查询节点并按站点类型分组
     * <p>
     * 先查询该负载下的所有节点,然后按site_type字段分组
     * 用于在前端按站点类型分组展示服务节点
     * </p>
     *
     * @param loadName 负载名称(即app_name)
     * @return Map<站点类型, 节点列表>
     */
    @Override
    public java.util.Map<String, List<OriginProviderConfig>> getNodesGroupBySiteType(String loadName) {
        log.info("查询负载 {} 的节点并按站点类型分组", loadName);
        List<OriginProviderConfig> nodes = getNodesByLoadName(loadName);

        // 使用Stream API按site_type分组
        java.util.Map<String, List<OriginProviderConfig>> groupedNodes = nodes.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                node -> node.getSiteType() != null ? node.getSiteType() : "未分类"
            ));

        log.info("负载 {} 共有 {} 个站点类型", loadName, groupedNodes.size());
        return groupedNodes;
    }
}
