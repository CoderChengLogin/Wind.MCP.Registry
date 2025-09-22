package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.entity.User;
import cn.com.wind.mcp.registry.mapper.UserMapper;
import cn.com.wind.mcp.registry.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * User Service 实现类
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-11-08 18:10
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}