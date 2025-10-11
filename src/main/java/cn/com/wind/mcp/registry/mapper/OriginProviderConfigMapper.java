package cn.com.wind.mcp.registry.mapper;

import java.util.List;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 提供商应用配置Mapper接口
 * <p>
 * 提供origin_provider_config表的数据访问操作
 * </p>
 *
 * @author Claude Code
 * @date 2025-10-11
 */
@Mapper
public interface OriginProviderConfigMapper extends BaseMapper<OriginProviderConfig> {

    /**
     * 根据应用编号查询配置
     *
     * @param appNum 应用编号
     * @return 应用配置信息
     */
    OriginProviderConfig selectByAppNum(@Param("appNum") Long appNum);

    /**
     * 根据提供商ID查询所有应用配置
     *
     * @param providerId 提供商ID
     * @return 应用配置列表
     */
    List<OriginProviderConfig> selectByProviderId(@Param("providerId") Long providerId);

    /**
     * 根据站点类型查询应用配置
     *
     * @param siteType 站点类型
     * @return 应用配置列表
     */
    List<OriginProviderConfig> selectBySiteType(@Param("siteType") String siteType);

    /**
     * 查询所有启用的应用配置
     *
     * @return 启用的应用配置列表
     */
    List<OriginProviderConfig> selectEnabledConfigs();

    /**
     * 根据应用编号和环境查询配置项
     *
     * @param appNum 应用编号
     * @param env    环境标识
     * @return 配置项列表
     */
    List<OriginProviderConfig> selectConfigsByAppNumAndEnv(@Param("appNum") Long appNum, @Param("env") String env);
}
