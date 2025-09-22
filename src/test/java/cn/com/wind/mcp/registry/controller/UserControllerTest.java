package cn.com.wind.mcp.registry.controller;

import java.util.Arrays;
import java.util.List;

import cn.com.wind.mcp.registry.entity.User;
import cn.com.wind.mcp.registry.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserController单元测试
 * 使用Mockito模拟UserService层
 *
 * @author Wind
 * @date 2025-09-22
 */
@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试获取所有用户
     */
    @Test
    public void testGetAllUsers() throws Exception {
        // 准备测试数据
        List<User> users = Arrays.asList(
            createTestUser(1L, "user1"),
            createTestUser(2L, "user2")
        );

        // 模拟Service层行为
        when(userService.list()).thenReturn(users);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // 执行请求并验证结果
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("user1"))
            .andExpect(jsonPath("$[1].name").value("user2"));

        // 验证Service方法被调用
        verify(userService, times(1)).list();
    }

    /**
     * 测试根据ID获取用户
     */
    @Test
    public void testGetUserById() throws Exception {
        // 准备测试数据
        User user = createTestUser(1L, "testuser");

        // 模拟Service层行为
        when(userService.getById(1L)).thenReturn(user);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // 执行请求并验证结果
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("testuser"));

        // 验证Service方法被调用
        verify(userService, times(1)).getById(1L);
    }

    /**
     * 测试创建用户
     */
    @Test
    public void testCreateUser() throws Exception {
        // 准备测试数据
        User user = createTestUser(null, "newuser");
        User savedUser = createTestUser(1L, "newuser");

        // 模拟Service层行为
        when(userService.save(any(User.class))).thenReturn(true);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // 执行请求并验证结果
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk());

        // 验证Service方法被调用
        verify(userService, times(1)).save(any(User.class));
    }

    /**
     * 测试更新用户
     */
    @Test
    public void testUpdateUser() throws Exception {
        // 准备测试数据
        User user = createTestUser(1L, "updateduser");

        // 模拟Service层行为
        when(userService.updateById(any(User.class))).thenReturn(true);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // 执行请求并验证结果
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk());

        // 验证Service方法被调用
        verify(userService, times(1)).updateById(any(User.class));
    }

    /**
     * 测试删除用户
     */
    @Test
    public void testDeleteUser() throws Exception {
        // 模拟Service层行为
        when(userService.removeById(1L)).thenReturn(true);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // 执行请求并验证结果
        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isOk());

        // 验证Service方法被调用
        verify(userService, times(1)).removeById(1L);
    }

    /**
     * 测试分页查询用户
     */
    @Test
    public void testGetUsersPage() throws Exception {
        // 准备测试数据
        List<User> users = Arrays.asList(
            createTestUser(1L, "user1"),
            createTestUser(2L, "user2")
        );

        Page<User> page = new Page<>(1, 2);
        page.setRecords(users);
        page.setTotal(2);

        // 模拟Service层行为
        when(userService.page(any(Page.class))).thenReturn(page);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // 执行请求并验证结果
        mockMvc.perform(get("/api/users/page")
                .param("page", "1")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records.length()").value(2))
            .andExpect(jsonPath("$.total").value(2));

        // 验证Service方法被调用
        verify(userService, times(1)).page(any(Page.class));
    }

    /**
     * 测试根据用户名查询用户
     */
    @Test
    public void testGetUserByUsername() throws Exception {
        // 准备测试数据
        User user = createTestUser(1L, "testuser");

        // 模拟Service层行为
        when(userService.getOne(any())).thenReturn(user);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // 执行请求并验证结果
        mockMvc.perform(get("/api/users/username/testuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("testuser"));

        // 验证Service方法被调用
        verify(userService, times(1)).getOne(any());
    }

    /**
     * 创建测试用户对象
     *
     * @param id       用户ID
     * @param username 用户名
     * @return 用户对象
     */
    private User createTestUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }
}