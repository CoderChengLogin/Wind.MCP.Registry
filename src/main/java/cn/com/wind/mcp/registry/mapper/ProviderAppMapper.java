package cn.com.wind.mcp.registry.mapper;

import java.util.List;

import cn.com.wind.mcp.registry.entity.ProviderApp;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 提供者应用服务节点Mapper接口
 * <p>
 * 提供对provider_app表的数据访问功能
 * </p>
 *
 * @author system
 * @date Created in 2025-10-11
 */
@Mapper
public interface ProviderAppMapper extends BaseMapper<ProviderApp> {

    /**
     * 根据提供者ID查询所有应用节点
     *
     * @param providerId 提供者ID
     * @return 应用节点列表
     */
    List<ProviderApp> selectByProviderId(@Param("providerId") Long providerId);

    /**
     * 根据提供者ID和应用名称查询应用节点
     *
     * @param providerId 提供者ID
     * @param appName    应用名称
     * @return 应用节点列表
     */
    List<ProviderApp> selectByProviderIdAndAppName(@Param("providerId") Long providerId,
        @Param("appName") String appName);

    /**
     * 查询已启用的应用节点
     *
     * @param providerId 提供者ID
     * @return 已启用的应用节点列表
     */
    List<ProviderApp> selectEnabledByProviderId(@Param("providerId") Long providerId);
}
