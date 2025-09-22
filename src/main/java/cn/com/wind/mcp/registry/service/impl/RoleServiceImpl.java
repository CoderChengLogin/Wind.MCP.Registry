package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.Role;
import cn.com.wind.mcp.registry.mapper.RoleMapper;
import cn.com.wind.mcp.registry.service.RoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Role Service 实现类
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019-09-14 14:06
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}