package cn.com.wind.mcp.registry.service;

import java.util.List;
import java.util.stream.Collectors;

import cn.com.wind.mcp.registry.WindMcpRegistryApplicationTests;
import cn.com.wind.mcp.registry.entity.User;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * User Service 测试类
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-11-08 18:13
 */
@Slf4j
public class UserServiceTest extends WindMcpRegistryApplicationTests {

    @Autowired
    private UserService userService;

    /**
     * 测试Mybatis-Plus 新增
     */
    @Test
    public void testSave() {
        String salt = IdUtil.fastSimpleUUID();
        User testSave3 = User.builder()
            .name("testSave3")
            .password(SecureUtil.md5("123456" + salt))
            .salt(salt)
            .email("testSave3@wind.com")
            .phoneNumber("17300000003")
            .status(1)
            .lastLoginTime(new DateTime())
            .build();
        boolean save = userService.save(testSave3);
        Assert.assertTrue(save);
        log.debug("【测试id回显#testSave3.getId()】= {}", testSave3.getId());
    }

    /**
     * 测试Mybatis-Plus 批量新增
     */
    @Test
    public void testSaveList() {
        List<User> userList = Lists.newArrayList();
        for (int i = 4; i < 14; i++) {
            String salt = IdUtil.fastSimpleUUID();
            User user = User.builder()
                .name("testSave" + i)
                .password(SecureUtil.md5("123456" + salt))
                .salt(salt)
                .email("testSave" + i + "@wind.com")
                .phoneNumber("1730000000" + i)
                .status(1)
                .lastLoginTime(new DateTime())
                .build();
            userList.add(user);
        }
        boolean batch = userService.saveBatch(userList);
        Assert.assertTrue(batch);
        List<Long> ids = userList.stream().map(User::getId).collect(Collectors.toList());
        log.debug("【测试批量新增#ids】= {}", ids);
    }

    /**
     * 测试Mybatis-Plus 删除
     */
    @Test
    public void testDelete() {
        boolean remove = userService.removeById(1L);
        Assert.assertTrue(remove);
        User byId = userService.getById(1L);
        Assert.assertNull(byId);
    }

    /**
     * 测试Mybatis-Plus 修改
     */
    @Test
    public void testUpdate() {
        User user = userService.getById(1L);
        Assert.assertNotNull(user);
        user.setName("MybatisPlus修改名字");
        boolean b = userService.updateById(user);
        Assert.assertTrue(b);
        User update = userService.getById(1L);
        Assert.assertEquals("MybatisPlus修改名字", update.getName());
        log.debug("【测试修改#update】= {}", update);
    }

    /**
     * 测试Mybatis-Plus 查询单个
     */
    @Test
    public void testQueryOne() {
        User user = userService.getById(1L);
        Assert.assertNotNull(user);
        log.debug("【测试查询单个#user】= {}", user);
    }

    /**
     * 测试Mybatis-Plus 查询全部
     */
    @Test
    public void testQueryAll() {
        List<User> list = userService.list();
        Assert.assertTrue(CollUtil.isNotEmpty(list));
        log.debug("【测试查询全部#list】= {}", list);
    }

    /**
     * 测试Mybatis-Plus 分页排序查询
     */
    @Test
    public void testQueryByPageAndSort() {
        initData();
        int current = 1;
        int size = 5;
        Page<User> userPage = new Page<>(current, size);
        IPage<User> page = userService.page(userPage);
        Assert.assertEquals(5, page.getSize());
        Assert.assertEquals(1, page.getCurrent());
        log.debug("【测试分页排序查询#page.getRecords()】= {}", page.getRecords());
    }

    /**
     * 测试Mybatis-Plus 条件查询
     */
    @Test
    public void testQueryByCondition() {
        initData();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("name", "Save1").or().eq("phone_number", "17300000001").orderByDesc("id");
        int count = userService.count(wrapper);
        log.debug("【测试条件查询#count】= {}", count);

        List<User> list = userService.list(wrapper);
        Assert.assertTrue(CollUtil.isNotEmpty(list));
        log.debug("【测试条件查询#list】= {}", list);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        testSaveList();
    }
}