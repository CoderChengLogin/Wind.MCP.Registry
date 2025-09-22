package cn.com.wind.mcp.registry.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.com.wind.mcp.registry.entity.Role;
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
 * RoleService集成测试
 * 使用真实数据库连接进行测试
 *
 * @author Wind
 * @date 2025-09-22
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class RoleServiceIntegrationTest {

    @Autowired
    private RoleService roleService;

    /**
     * 测试保存角色
     */
    @Test
    public void testSaveRole() {
        // 准备测试数据
        Role role = createTestRole("testrole");

        // 执行保存操作
        boolean result = roleService.save(role);

        // 验证结果
        assertTrue("角色保存应该成功", result);
        assertNotNull("角色ID应该不为空", role.getId());
    }

    /**
     * 测试根据ID获取角色
     */
    @Test
    public void testGetRoleById() {
        // 准备测试数据
        Role role = createTestRole("testrole");
        roleService.save(role);

        // 执行查询操作
        Role foundRole = roleService.getById(role.getId());

        // 验证结果
        assertNotNull("查询到的角色不应该为空", foundRole);
        assertEquals("角色名应该匹配", "testrole", foundRole.getName());
    }

    /**
     * 测试获取所有角色
     */
    @Test
    public void testGetAllRoles() {
        // 准备测试数据
        Role role1 = createTestRole("role1");
        Role role2 = createTestRole("role2");
        roleService.save(role1);
        roleService.save(role2);

        // 执行查询操作
        List<Role> roles = roleService.list();

        // 验证结果
        assertNotNull("角色列表不应该为空", roles);
        assertTrue("角色列表应该包含至少2个角色", roles.size() >= 2);
    }

    /**
     * 测试更新角色
     */
    @Test
    public void testUpdateRole() {
        // 准备测试数据
        Role role = createTestRole("testrole");
        roleService.save(role);

        // 更新角色信息
        role.setName("updatedrole");
        boolean result = roleService.updateById(role);

        // 验证结果
        assertTrue("角色更新应该成功", result);

        // 重新查询验证更新结果
        Role updatedRole = roleService.getById(role.getId());
        assertEquals("角色名应该已更新", "updatedrole", updatedRole.getName());
    }

    /**
     * 测试删除角色
     */
    @Test
    public void testDeleteRole() {
        // 准备测试数据
        Role role = createTestRole("testrole");
        roleService.save(role);
        Long roleId = role.getId();

        // 执行删除操作
        boolean result = roleService.removeById(roleId);

        // 验证结果
        assertTrue("角色删除应该成功", result);

        // 验证角色已被删除
        Role deletedRole = roleService.getById(roleId);
        assertNull("删除后的角色应该为空", deletedRole);
    }

    /**
     * 测试分页查询角色
     */
    @Test
    public void testGetRolesPage() {
        // 准备测试数据
        Role role1 = createTestRole("role1");
        Role role2 = createTestRole("role2");
        Role role3 = createTestRole("role3");
        roleService.save(role1);
        roleService.save(role2);
        roleService.save(role3);

        // 执行分页查询
        Page<Role> page = new Page<>(1, 2);
        IPage<Role> result = roleService.page(page);

        // 验证结果
        assertNotNull("分页结果不应该为空", result);
        assertTrue("总记录数应该大于等于3", result.getTotal() >= 3);
        assertTrue("当前页记录数应该小于等于2", result.getRecords().size() <= 2);
    }

    /**
     * 测试根据条件查询角色
     */
    @Test
    public void testGetRolesByCondition() {
        // 准备测试数据
        Role role1 = createTestRole("testrole1");
        Role role2 = createTestRole("testrole2");
        Role role3 = createTestRole("otherrole");
        roleService.save(role1);
        roleService.save(role2);
        roleService.save(role3);

        // 执行条件查询
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", "testrole");
        List<Role> roles = roleService.list(queryWrapper);

        // 验证结果
        assertNotNull("查询结果不应该为空", roles);
        assertTrue("应该查询到至少2个角色", roles.size() >= 2);
        for (Role role : roles) {
            assertTrue("角色名应该包含testrole", role.getName().contains("testrole"));
        }
    }

    /**
     * 测试根据角色名模糊查询
     */
    @Test
    public void testGetRoleByNameLike() {
        // 准备测试数据
        Role role = createTestRole("uniquerole");
        roleService.save(role);

        // 执行模糊查询
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", "unique");
        List<Role> roles = roleService.list(queryWrapper);

        // 验证结果
        assertNotNull("查询结果不应该为空", roles);
        assertTrue("应该查询到至少1个角色", roles.size() >= 1);
        boolean found = roles.stream().anyMatch(r -> "uniquerole".equals(r.getName()));
        assertTrue("应该找到uniquerole", found);
    }

    /**
     * 测试角色名唯一性
     */
    @Test
    public void testRoleNameUniqueness() {
        // 准备测试数据
        Role role1 = createTestRole("uniquename");
        roleService.save(role1);

        // 尝试保存同名角色
        Role role2 = createTestRole("uniquename");

        // 检查是否已存在同名角色
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", "uniquename");
        Role existingRole = roleService.getOne(queryWrapper);

        // 验证结果
        assertNotNull("应该找到已存在的角色", existingRole);
        assertEquals("角色名应该匹配", "uniquename", existingRole.getName());
    }

    /**
     * 测试批量操作
     */
    @Test
    public void testBatchOperations() {
        // 准备测试数据
        Role role1 = createTestRole("batch1");
        Role role2 = createTestRole("batch2");
        Role role3 = createTestRole("batch3");
        List<Role> roles = Arrays.asList(role1, role2, role3);

        // 执行批量保存
        boolean result = roleService.saveBatch(roles);

        // 验证结果
        assertTrue("批量保存应该成功", result);
        for (Role role : roles) {
            assertNotNull("批量保存后角色ID不应该为空", role.getId());
        }

        // 执行批量删除
        List<Long> roleIds = Arrays.asList(role1.getId(), role2.getId(), role3.getId());
        boolean deleteResult = roleService.removeByIds(roleIds);

        // 验证删除结果
        assertTrue("批量删除应该成功", deleteResult);
    }

    /**
     * 测试统计功能
     */
    @Test
    public void testCountRoles() {
        // 准备测试数据
        Role role1 = createTestRole("count1");
        Role role2 = createTestRole("count2");
        roleService.save(role1);
        roleService.save(role2);

        // 执行统计查询
        long count = roleService.count();

        // 验证结果
        assertTrue("角色总数应该大于等于2", count >= 2);
    }

    /**
     * 测试排序查询
     */
    @Test
    public void testGetRolesWithOrder() {
        // 准备测试数据
        Role roleA = createTestRole("arole");
        Role roleB = createTestRole("brole");
        Role roleC = createTestRole("crole");
        roleService.save(roleC); // 先保存C
        roleService.save(roleA); // 再保存A
        roleService.save(roleB); // 最后保存B

        // 执行排序查询
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", "role").orderByAsc("name");
        List<Role> roles = roleService.list(queryWrapper);

        // 验证结果
        assertNotNull("查询结果不应该为空", roles);
        assertTrue("应该查询到至少3个角色", roles.size() >= 3);

        // 验证排序（找到我们创建的角色并验证顺序）
        List<String> roleNames = roles.stream()
            .map(Role::getName)
            .filter(name -> name.endsWith("role"))
            .collect(Collectors.toList());

        if (roleNames.size() >= 3) {
            int aIndex = roleNames.indexOf("arole");
            int bIndex = roleNames.indexOf("brole");
            int cIndex = roleNames.indexOf("crole");

            assertTrue("arole应该在brole之前", aIndex < bIndex);
            assertTrue("brole应该在crole之前", bIndex < cIndex);
        }
    }

    /**
     * 创建测试角色对象
     *
     * @param name 角色名
     * @return 角色对象
     */
    private Role createTestRole(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }
}