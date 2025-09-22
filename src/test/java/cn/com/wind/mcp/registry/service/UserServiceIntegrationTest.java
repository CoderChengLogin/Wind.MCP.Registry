package cn.com.wind.mcp.registry.service;

import java.util.Arrays;
import java.util.List;

import cn.com.wind.mcp.registry.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * UserService集成测试
 * 使用真实数据库连接进行测试
 *
 * @author Wind
 * @date 2025-09-22
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    /**
     * 测试保存用户
     */
    @Test
    public void testSaveUser() {
        // 准备测试数据
        User user = createTestUser("testuser", "test@example.com");

        // 执行保存操作
        boolean result = userService.save(user);

        // 验证结果
        assertTrue("用户保存应该成功", result);
        assertNotNull("用户ID应该不为空", user.getId());
    }

    /**
     * 测试根据ID获取用户
     */
    @Test
    public void testGetUserById() {
        // 准备测试数据
        User user = createTestUser("testuser", "test@example.com");
        userService.save(user);

        // 执行查询操作
        User foundUser = userService.getById(user.getId());

        // 验证结果
        assertNotNull("查询到的用户不应该为空", foundUser);
        assertEquals("用户名应该匹配", "testuser", foundUser.getName());
        assertEquals("邮箱应该匹配", "test@example.com", foundUser.getEmail());
    }

    /**
     * 测试获取所有用户
     */
    @Test
    public void testGetAllUsers() {
        // 准备测试数据
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");
        userService.save(user1);
        userService.save(user2);

        // 执行查询操作
        List<User> users = userService.list();

        // 验证结果
        assertNotNull("用户列表不应该为空", users);
        assertTrue("用户列表应该包含至少2个用户", users.size() >= 2);
    }

    /**
     * 测试更新用户
     */
    @Test
    public void testUpdateUser() {
        // 准备测试数据
        User user = createTestUser("testuser", "test@example.com");
        userService.save(user);

        // 更新用户信息
        user.setName("updateduser");
        user.setEmail("updated@example.com");
        boolean result = userService.updateById(user);

        // 验证结果
        assertTrue("用户更新应该成功", result);

        // 重新查询验证更新结果
        User updatedUser = userService.getById(user.getId());
        assertEquals("用户名应该已更新", "updateduser", updatedUser.getName());
        assertEquals("邮箱应该已更新", "updated@example.com", updatedUser.getEmail());
    }

    /**
     * 测试删除用户
     */
    @Test
    public void testDeleteUser() {
        // 准备测试数据
        User user = createTestUser("testuser", "test@example.com");
        userService.save(user);
        Long userId = user.getId();

        // 执行删除操作
        boolean result = userService.removeById(userId);

        // 验证结果
        assertTrue("用户删除应该成功", result);

        // 验证用户已被删除
        User deletedUser = userService.getById(userId);
        assertNull("删除后的用户应该为空", deletedUser);
    }

    /**
     * 测试分页查询用户
     */
    @Test
    public void testGetUsersPage() {
        // 准备测试数据
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");
        User user3 = createTestUser("user3", "user3@example.com");
        userService.save(user1);
        userService.save(user2);
        userService.save(user3);

        // 执行分页查询
        Page<User> page = new Page<>(1, 2);
        IPage<User> result = userService.page(page);

        // 验证结果
        assertNotNull("分页结果不应该为空", result);
        assertTrue("总记录数应该大于等于3", result.getTotal() >= 3);
        assertTrue("当前页记录数应该小于等于2", result.getRecords().size() <= 2);
    }

    /**
     * 测试根据条件查询用户
     */
    @Test
    public void testGetUsersByCondition() {
        // 准备测试数据
        User user1 = createTestUser("testuser1", "test1@example.com");
        User user2 = createTestUser("testuser2", "test2@example.com");
        User user3 = createTestUser("otheruser", "other@example.com");
        userService.save(user1);
        userService.save(user2);
        userService.save(user3);

        // 执行条件查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", "testuser");
        List<User> users = userService.list(queryWrapper);

        // 验证结果
        assertNotNull("查询结果不应该为空", users);
        assertTrue("应该查询到至少2个用户", users.size() >= 2);
        for (User user : users) {
            assertTrue("用户名应该包含testuser", user.getName().contains("testuser"));
        }
    }

    /**
     * 测试根据邮箱查询用户
     */
    @Test
    public void testGetUserByEmail() {
        // 准备测试数据
        User user = createTestUser("testuser", "unique@example.com");
        userService.save(user);

        // 执行邮箱查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", "unique@example.com");
        User foundUser = userService.getOne(queryWrapper);

        // 验证结果
        assertNotNull("根据邮箱查询的用户不应该为空", foundUser);
        assertEquals("邮箱应该匹配", "unique@example.com", foundUser.getEmail());
        assertEquals("用户名应该匹配", "testuser", foundUser.getName());
    }

    /**
     * 测试用户状态更新
     */
    @Test
    public void testUpdateUserStatus() {
        // 准备测试数据
        User user = createTestUser("testuser", "test@example.com");
        user.setStatus(1); // 设置为活跃状态
        userService.save(user);

        // 更新用户状态
        user.setStatus(0); // 设置为非活跃状态
        boolean result = userService.updateById(user);

        // 验证结果
        assertTrue("状态更新应该成功", result);

        // 重新查询验证状态更新
        User updatedUser = userService.getById(user.getId());
        assertEquals("用户状态应该已更新", Integer.valueOf(0), updatedUser.getStatus());
    }

    /**
     * 测试批量操作
     */
    @Test
    public void testBatchOperations() {
        // 准备测试数据
        User user1 = createTestUser("batch1", "batch1@example.com");
        User user2 = createTestUser("batch2", "batch2@example.com");
        User user3 = createTestUser("batch3", "batch3@example.com");
        List<User> users = Arrays.asList(user1, user2, user3);

        // 执行批量保存
        boolean result = userService.saveBatch(users);

        // 验证结果
        assertTrue("批量保存应该成功", result);
        for (User user : users) {
            assertNotNull("批量保存后用户ID不应该为空", user.getId());
        }

        // 执行批量删除
        List<Long> userIds = Arrays.asList(user1.getId(), user2.getId(), user3.getId());
        boolean deleteResult = userService.removeByIds(userIds);

        // 验证删除结果
        assertTrue("批量删除应该成功", deleteResult);
    }

    /**
     * 创建测试用户对象
     *
     * @param username 用户名
     * @param email    邮箱
     * @return 用户对象
     */
    private User createTestUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password123");
        user.setStatus(1);
        return user;
    }
}