package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.mapper.OriginToolHttpMapper;
import cn.com.wind.mcp.registry.service.OriginToolHttpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 原始HTTP接口Service实现类
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Service
public class OriginToolHttpServiceImpl extends ServiceImpl<OriginToolHttpMapper, OriginToolHttp>
        implements OriginToolHttpService {

    /**
     * 统计指定用户创建的接口数量
     *
     * @param createBy 创建人
     * @return 接口数量
     */
    @Override
    public long countByCreateBy(String createBy) {
        return count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OriginToolHttp>().eq("create_by",
                createBy));
    }

    /**
     * 统计指定提供者的接口数量
     *
     * @param providerId 提供者ID
     * @return 接口数量
     */
    @Override
    public long countByProviderId(Long providerId) {
        return count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OriginToolHttp>().eq("provider_id",
                providerId));
    }
}