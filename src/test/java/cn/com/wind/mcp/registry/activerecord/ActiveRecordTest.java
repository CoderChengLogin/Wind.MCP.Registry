package cn.com.wind.mcp.registry.activerecord;

import java.util.List;

import cn.com.wind.mcp.registry.WindMcpRegistryApplicationTests;
import cn.com.wind.mcp.registry.entity.Role;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * Role ActiveRecord 测试类
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019-09-14 14:19
 */
@Slf4j
public class ActiveRecordTest extends WindMcpRegistryApplicationTests {

    /**
     * 测试 ActiveRecord 插入数据
     */
    @Test
    public void testActiveRecordInsert() {
        Role role = new Role();
        role.setName("VIP");
        Assert.assertTrue(role.insert());
        // 成功直接拿会写的 ID
        log.debug("【role】= {}", role);
    }

    /**
     * 测试 ActiveRecord 更新数据
     */
    @Test
    public void testActiveRecordUpdate() {
        Assert.assertTrue(new Role().setId(1L).setName("管理员-1").updateById());
        Assert.assertTrue(
            new Role().update(new UpdateWrapper<Role>().lambda().set(Role::getName, "普通用户-1").eq(Role::getId, 2)));
    }

    /**
     * 测试 ActiveRecord 查询数据
     */
    @Test
    public void testActiveRecordSelect() {
        Assert.assertEquals("管理员", new Role().setId(1L).selectById().getName());
        Role role = new Role().selectOne(new QueryWrapper<Role>().lambda().eq(Role::getId, 2));
        Assert.assertEquals("普通用户", role.getName());
        List<Role> roles = new Role().selectAll();
        Assert.assertTrue(roles.size() > 0);
        log.debug("【roles】= {}", roles);
    }

    /**
     * 测试 ActiveRecord 删除数据
     */
    @Test
    public void testActiveRecordDelete() {
        Assert.assertTrue(new Role().setId(1L).deleteById());
        Assert.assertTrue(new Role().delete(new QueryWrapper<Role>().lambda().eq(Role::getId, 2)));
    }
}