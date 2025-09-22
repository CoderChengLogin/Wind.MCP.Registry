package cn.com.wind.mcp.registry.controller;

import java.util.List;

import cn.com.wind.mcp.registry.entity.Role;
import cn.com.wind.mcp.registry.service.RoleService;
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
 * 角色控制器
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019-09-14 14:20
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取所有角色
     *
     * @return 角色列表
     */
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("获取所有角色");
        List<Role> roles = roleService.list();
        return ResponseEntity.ok(roles);
    }

    /**
     * 根据ID获取角色
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        log.info("根据ID获取角色: {}", id);
        Role role = roleService.getById(id);
        if (role != null) {
            return ResponseEntity.ok(role);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 创建新角色
     *
     * @param role 角色信息
     * @return 创建的角色
     */
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        log.info("创建新角色: {}", role.getName());

        boolean saved = roleService.save(role);
        if (saved) {
            return ResponseEntity.ok(role);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 更新角色信息
     *
     * @param id   角色ID
     * @param role 更新的角色信息
     * @return 更新后的角色
     */
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        log.info("更新角色信息: {}", id);

        Role existingRole = roleService.getById(id);
        if (existingRole == null) {
            return ResponseEntity.notFound().build();
        }

        role.setId(id);
        boolean updated = roleService.updateById(role);
        if (updated) {
            return ResponseEntity.ok(roleService.getById(id));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("删除角色: {}", id);
        boolean deleted = roleService.removeById(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 分页查询角色
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页角色列表
     */
    @GetMapping("/page")
    public ResponseEntity<com.baomidou.mybatisplus.core.metadata.IPage<Role>> getRolesPage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询角色: page={}, size={}", page, size);

        Page<Role> rolePage = new Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<Role> result = roleService.page(rolePage);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据角色名查询角色
     *
     * @param name 角色名
     * @return 角色信息
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Role> getRoleByName(@PathVariable String name) {
        log.info("根据角色名查询角色: {}", name);

        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        Role role = roleService.getOne(queryWrapper);

        if (role != null) {
            return ResponseEntity.ok(role);
        }
        return ResponseEntity.notFound().build();
    }
}