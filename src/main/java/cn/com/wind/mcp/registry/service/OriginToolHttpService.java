package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 原始HTTP接口Service接口
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
public interface OriginToolHttpService extends IService<OriginToolHttp> {

    /**
     * 统计指定用户创建的接口数量
     *
     * @param createBy 创建人
     * @return 接口数量
     */
    long countByCreateBy(String createBy);

    /**
     * 统计指定提供者的接口数量
     *
     * @param providerId 提供者ID
     * @return 接口数量
     */
    long countByProviderId(Long providerId);
}
