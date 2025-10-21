package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.entity.ProviderApp;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 提供者应用服务节点服务接口
 * <p>
 * 提供应用节点的增删改查和业务操作功能
 * </p>
 *
 * @author system
 * @date Created in 2025-10-11
 */
public interface ProviderAppService extends IService<ProviderApp> {

    /**
     * 根据提供者ID查询所有应用节点
     *
     * @param providerId 提供者ID
     * @return 应用节点列表
     */
    List<ProviderApp> listByProviderId(Long providerId);

    /**
     * 根据提供者ID和应用名称查询应用节点
     *
     * @param providerId 提供者ID
     * @param appName    应用名称
     * @return 应用节点列表
     */
    List<ProviderApp> listByProviderIdAndAppName(Long providerId, String appName);

    /**
     * 查询已启用的应用节点
     *
     * @param providerId 提供者ID
     * @return 已启用的应用节点列表
     */
    List<ProviderApp> listEnabledByProviderId(Long providerId);

    /**
     * 创建应用节点
     *
     * @param providerApp 应用节点信息
     * @return 是否创建成功
     */
    boolean createApp(ProviderApp providerApp);

    /**
     * 更新应用节点
     *
     * @param providerApp 应用节点信息
     * @return 是否更新成功
     */
    boolean updateApp(ProviderApp providerApp);

    /**
     * 启用/禁用应用节点
     *
     * @param id        应用节点ID
     * @param isEnabled 是否启用
     * @return 是否操作成功
     */
    boolean toggleEnable(Long id, Boolean isEnabled);

    /**
     * 删除应用节点（逻辑删除）
     *
     * @param id 应用节点ID
     * @return 是否删除成功
     */
    boolean deleteApp(Long id);

    /**
     * 批量删除应用节点
     *
     * @param ids 应用节点ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteApps(List<Long> ids);

    /**
     * 验证应用节点配置的有效性
     *
     * @param providerApp 应用节点信息
     * @return 是否有效
     */
    boolean validateAppConfig(ProviderApp providerApp);

    /**
     * 检查健康状态
     *
     * @param id 应用节点ID
     * @return 健康状态信息
     */
    String checkHealth(Long id);
}
