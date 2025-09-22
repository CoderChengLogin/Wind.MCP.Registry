package cn.com.wind.mcp.registry.controller;

import java.util.Date;
import java.util.List;

import cn.com.wind.mcp.registry.entity.User;
import cn.com.wind.mcp.registry.service.UserService;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户控制器
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-11-08 18:15
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户
     *
     * @return 用户列表
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("获取所有用户");
        List<User> users = userService.list();
        return ResponseEntity.ok(users);
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("根据ID获取用户: {}", id);
        User user = userService.getById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 创建新用户
     *
     * @param user 用户信息
     * @return 创建的用户
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("创建新用户: {}", user.getName());

        // 生成盐值和加密密码
        String salt = IdUtil.fastSimpleUUID();
        String encryptedPassword = SecureUtil.md5(user.getPassword() + salt);

        user.setSalt(salt);
        user.setPassword(encryptedPassword);
        user.setStatus(1); // 默认启用
        user.setCreateTime(new Date());
        user.setLastUpdateTime(new Date());

        boolean saved = userService.save(user);
        if (saved) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 更新用户信息
     *
     * @param id   用户ID
     * @param user 更新的用户信息
     * @return 更新后的用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("更新用户信息: {}", id);

        User existingUser = userService.getById(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }

        user.setId(id);
        user.setLastUpdateTime(new Date());

        // 如果密码有变化，重新加密
        if (user.getPassword() != null && !user.getPassword().equals(existingUser.getPassword())) {
            String salt = IdUtil.fastSimpleUUID();
            String encryptedPassword = SecureUtil.md5(user.getPassword() + salt);
            user.setSalt(salt);
            user.setPassword(encryptedPassword);
        }

        boolean updated = userService.updateById(user);
        if (updated) {
            return ResponseEntity.ok(userService.getById(id));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户: {}", id);
        boolean deleted = userService.removeById(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 分页查询用户
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页用户列表
     */
    @GetMapping("/page")
    public ResponseEntity<com.baomidou.mybatisplus.core.metadata.IPage<User>> getUsersPage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询用户: page={}, size={}", page, size);

        Page<User> userPage = new Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<User> result = userService.page(userPage);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据用户名查询用户
     *
     * @param name 用户名
     * @return 用户信息
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<User> getUserByName(@PathVariable String name) {
        log.info("根据用户名查询用户: {}", name);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        User user = userService.getOne(queryWrapper);

        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
}