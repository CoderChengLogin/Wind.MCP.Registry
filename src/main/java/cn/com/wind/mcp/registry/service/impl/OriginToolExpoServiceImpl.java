package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.OriginToolExpo;
import cn.com.wind.mcp.registry.mapper.OriginToolExpoMapper;
import cn.com.wind.mcp.registry.service.OriginToolExpoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 原始Expo接口Service实现类
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Service
public class OriginToolExpoServiceImpl extends ServiceImpl<OriginToolExpoMapper, OriginToolExpo>
    implements OriginToolExpoService {

    /**
     * 统计指定用户创建的接口数量
     *
     * @param createBy 创建人
     * @return 接口数量
     */
    @Override
    public long countByCreateBy(String createBy) {
        return count(new QueryWrapper<OriginToolExpo>().eq("create_by", createBy));
    }

    /**
     * 统计指定提供者的接口数量
     *
     * @param providerId 提供者ID
     * @return 接口数量
     */
    @Override
    public long countByProviderId(Long providerId) {
        return count(new QueryWrapper<OriginToolExpo>().eq("provider_id", providerId));
    }
}