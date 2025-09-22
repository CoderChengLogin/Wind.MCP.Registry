package cn.com.wind.mcp.registry.controller;

import java.util.Arrays;
import java.util.List;

import cn.com.wind.mcp.registry.entity.Role;
import cn.com.wind.mcp.registry.service.RoleService;
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
 * RoleController单元测试
 * 使用Mockito模拟RoleService层
 *
 * @author Wind
 * @date 2025-09-22
 */
@RunWith(MockitoJUnitRunner.class)
public class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试获取所有角色
     */
    @Test
    public void testGetAllRoles() throws Exception {
        // 准备测试数据
        List<Role> roles = Arrays.asList(
            createTestRole(1L, "admin"),
            createTestRole(2L, "user")
        );

        // 模拟Service层行为
        when(roleService.list()).thenReturn(roles);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

        // 执行请求并验证结果
        mockMvc.perform(get("/api/roles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("admin"))
            .andExpect(jsonPath("$[1].name").value("user"));

        // 验证Service方法被调用
        verify(roleService, times(1)).list();
    }

    /**
     * 测试根据ID获取角色
     */
    @Test
    public void testGetRoleById() throws Exception {
        // 准备测试数据
        Role role = createTestRole(1L, "admin");

        // 模拟Service层行为
        when(roleService.getById(1L)).thenReturn(role);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

        // 执行请求并验证结果
        mockMvc.perform(get("/api/roles/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("admin"));

        // 验证Service方法被调用
        verify(roleService, times(1)).getById(1L);
    }

    /**
     * 测试创建角色
     */
    @Test
    public void testCreateRole() throws Exception {
        // 准备测试数据
        Role role = createTestRole(null, "newrole");

        // 模拟Service层行为
        when(roleService.save(any(Role.class))).thenReturn(true);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

        // 执行请求并验证结果
        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
            .andExpect(status().isOk());

        // 验证Service方法被调用
        verify(roleService, times(1)).save(any(Role.class));
    }

    /**
     * 测试更新角色
     */
    @Test
    public void testUpdateRole() throws Exception {
        // 准备测试数据
        Role role = createTestRole(1L, "updatedrole");

        // 模拟Service层行为
        when(roleService.updateById(any(Role.class))).thenReturn(true);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

        // 执行请求并验证结果
        mockMvc.perform(put("/api/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
            .andExpect(status().isOk());

        // 验证Service方法被调用
        verify(roleService, times(1)).updateById(any(Role.class));
    }

    /**
     * 测试删除角色
     */
    @Test
    public void testDeleteRole() throws Exception {
        // 模拟Service层行为
        when(roleService.removeById(1L)).thenReturn(true);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

        // 执行请求并验证结果
        mockMvc.perform(delete("/api/roles/1"))
            .andExpect(status().isOk());

        // 验证Service方法被调用
        verify(roleService, times(1)).removeById(1L);
    }

    /**
     * 测试分页查询角色
     */
    @Test
    public void testGetRolesPage() throws Exception {
        // 准备测试数据
        List<Role> roles = Arrays.asList(
            createTestRole(1L, "admin"),
            createTestRole(2L, "user")
        );

        Page<Role> page = new Page<>(1, 2);
        page.setRecords(roles);
        page.setTotal(2);

        // 模拟Service层行为
        when(roleService.page(any(Page.class))).thenReturn(page);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

        // 执行请求并验证结果
        mockMvc.perform(get("/api/roles/page")
                .param("page", "1")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records.length()").value(2))
            .andExpect(jsonPath("$.total").value(2));

        // 验证Service方法被调用
        verify(roleService, times(1)).page(any(Page.class));
    }

    /**
     * 测试根据角色名查询角色
     */
    @Test
    public void testGetRoleByName() throws Exception {
        // 准备测试数据
        Role role = createTestRole(1L, "admin");

        // 模拟Service层行为
        when(roleService.getOne(any())).thenReturn(role);

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

        // 执行请求并验证结果
        mockMvc.perform(get("/api/roles/name/admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("admin"));

        // 验证Service方法被调用
        verify(roleService, times(1)).getOne(any());
    }

    /**
     * 创建测试角色对象
     *
     * @param id   角色ID
     * @param name 角色名
     * @return 角色对象
     */
    private Role createTestRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }
}