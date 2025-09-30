package cn.com.wind.mcp.registry.controller;

import java.time.LocalDateTime;

import javax.servlet.http.HttpSession;

import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.service.OriginToolHttpService;
import cn.com.wind.mcp.registry.util.PermissionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 * 原始工具Controller - 处理/origin-tools路径
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Slf4j
@Controller
@RequestMapping("/origin-tools")
public class OriginToolsController {

    @Autowired
    private OriginToolHttpService originToolHttpService;

    /**
     * 工具列表页面 - 仅显示当前用户创建的工具
     */
    @GetMapping
    public String list(Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String method,
        @RequestParam(required = false) String url,
        HttpSession session) {
        log.info("查询原始工具列表: page={}, size={}, name={}, method={}, url={}", page, size, name, method, url);

        Page<OriginToolHttp> toolPage = new Page<>(page, size);
        QueryWrapper<OriginToolHttp> queryWrapper = new QueryWrapper<>();

        // 获取当前登录用户ID，实现权限隔离
        Long currentProviderId = PermissionUtil.getCurrentProviderId(session);
        if (currentProviderId == null) {
            // 用户未登录，返回空结果
            queryWrapper.eq("1", "0");
        } else {
            // 只查询当前用户创建的工具
            queryWrapper.eq("provider_id", currentProviderId);

            // 构建搜索条件
            if (StringUtils.hasText(name)) {
                queryWrapper.like("name_display", name);
            }
            if (StringUtils.hasText(method)) {
                queryWrapper.eq("req_method", method);
            }
            if (StringUtils.hasText(url)) {
                queryWrapper.like("req_url", url);
            }
        }

        IPage<OriginToolHttp> result = originToolHttpService.page(toolPage, queryWrapper);

        model.addAttribute("tools", result.getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getPages());
        model.addAttribute("totalRecords", result.getTotal());
        model.addAttribute("name", name);
        model.addAttribute("method", method);
        model.addAttribute("url", url);

        return "origin-tools/list";
    }

    /**
     * 工具详情页面 - 仅允许查看自己创建的工具
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        log.info("查询原始工具详情: id={}", id);

        OriginToolHttp tool = originToolHttpService.getById(id);
        if (tool == null) {
            return "redirect:/origin-tools?error=工具不存在";
        }

        // 检查权限：只有工具的创建者可以查看详情
        if (!PermissionUtil.hasPermission(session, tool.getProviderId())) {
            return "redirect:/origin-tools?error=无权限查看此工具";
        }

        model.addAttribute("tool", tool);
        return "origin-tools/detail";
    }

    /**
     * 新增工具页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("tool", new OriginToolHttp());
        return "origin-tools/form";
    }

    /**
     * 编辑工具页面 - 仅允许编辑自己创建的工具
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        log.info("编辑原始工具: id={}", id);

        OriginToolHttp tool = originToolHttpService.getById(id);
        if (tool == null) {
            return "redirect:/origin-tools?error=工具不存在";
        }

        // 检查权限：只有工具的创建者可以编辑
        if (!PermissionUtil.hasPermission(session, tool.getProviderId())) {
            return "redirect:/origin-tools?error=无权限编辑此工具";
        }

        model.addAttribute("tool", tool);
        return "origin-tools/form";
    }

    /**
     * 保存工具 - 确保用户只能保存自己的工具
     */
    @PostMapping("/save")
    public String save(@ModelAttribute OriginToolHttp tool, HttpSession session) {
        log.info("保存原始工具: {}", tool);

        // 获取当前登录用户
        Long currentProviderId = PermissionUtil.getCurrentProviderId(session);
        if (currentProviderId == null) {
            return "redirect:/provider/login?error=用户未登录";
        }

        String currentUser = PermissionUtil.getCurrentProvider(session).getUsername();

        try {
            if (tool.getId() == null) {
                // 新增 - 设置为当前用户
                tool.setProviderId(currentProviderId);
                tool.setProviderToolNum(System.currentTimeMillis()); // 生成工具编号
                tool.setCreateTime(LocalDateTime.now());
                tool.setCreateBy(currentUser);
                tool.setUpdateTime(LocalDateTime.now());
                tool.setUpdateBy(currentUser);
            } else {
                // 更新 - 检查权限
                OriginToolHttp existingTool = originToolHttpService.getById(tool.getId());
                if (existingTool == null) {
                    return "redirect:/origin-tools?error=工具不存在";
                }
                if (!PermissionUtil.hasPermission(session, existingTool.getProviderId())) {
                    return "redirect:/origin-tools?error=无权限修改此工具";
                }
                // 保持原有的providerId，不允许修改
                tool.setProviderId(existingTool.getProviderId());
                tool.setUpdateTime(LocalDateTime.now());
                tool.setUpdateBy(currentUser);
            }

            originToolHttpService.saveOrUpdate(tool);
            return "redirect:/origin-tools?success=保存成功";
        } catch (Exception e) {
            log.error("保存原始工具失败", e);
            return "redirect:/origin-tools?error=" + e.getMessage();
        }
    }

    /**
     * 删除工具 - 仅允许删除自己创建的工具
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        log.info("删除原始工具: id={}", id);

        // 检查工具是否存在
        OriginToolHttp tool = originToolHttpService.getById(id);
        if (tool == null) {
            return "redirect:/origin-tools?error=工具不存在";
        }

        // 检查权限：只有工具的创建者可以删除
        if (!PermissionUtil.hasPermission(session, tool.getProviderId())) {
            return "redirect:/origin-tools?error=无权限删除此工具";
        }

        try {
            originToolHttpService.removeById(id);
            return "redirect:/origin-tools?success=删除成功";
        } catch (Exception e) {
            log.error("删除原始工具失败", e);
            return "redirect:/origin-tools?error=" + e.getMessage();
        }
    }

    /**
     * 搜索工具 - 仅搜索当前用户创建的工具
     */
    @GetMapping("/search")
    public String search(Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String method,
        @RequestParam(required = false) String url,
        HttpSession session) {
        log.info("搜索原始工具: name={}, method={}, url={}", name, method, url);

        // 重定向到list方法处理搜索，传递session参数
        return list(model, page, size, name, method, url, session);
    }
}