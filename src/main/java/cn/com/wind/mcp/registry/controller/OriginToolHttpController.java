package cn.com.wind.mcp.registry.controller;

import java.net.URLEncoder;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 * 原始HTTP接口Controller
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Slf4j
@Controller
@RequestMapping("/origin-http-tools")
public class OriginToolHttpController {

    @Autowired
    private OriginToolHttpService originToolHttpService;

    /**
     * HTTP接口列表页面
     */
    @GetMapping
    public String list(Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpSession session) {
        log.info("查询原始HTTP接口列表: page={}, size={}", page, size);

        Page<OriginToolHttp> toolPage = new Page<OriginToolHttp>(page, size);
        IPage<OriginToolHttp> result;

        // 获取当前登录用户ID
        Long currentProviderId = PermissionUtil.getCurrentProviderId(session);
        if (currentProviderId == null) {
            // 用户未登录，返回空结果
            result = originToolHttpService.page(toolPage, new QueryWrapper<OriginToolHttp>().eq("1", "0"));
        } else {
            // 查询用户自己的工具（默认行为）
            QueryWrapper<OriginToolHttp> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("provider_id", currentProviderId);
            result = originToolHttpService.page(toolPage, queryWrapper);
        }

        model.addAttribute("tools", result.getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getPages());
        model.addAttribute("totalRecords", result.getTotal());

        return "origin-tools/list";
    }

    /**
     * HTTP接口详情页面
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.info("查询原始HTTP接口详情: id={}", id);

        OriginToolHttp tool = originToolHttpService.getById(id);
        if (tool == null) {
            return "redirect:/origin-http-tools";
        }

        model.addAttribute("tool", tool);
        return "origin-tools/detail";
    }

    /**
     * 新增HTTP接口页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("tool", new OriginToolHttp());
        return "origin-tools/form";
    }

    /**
     * 新增HTTP接口页面 (new路径别名)
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("tool", new OriginToolHttp());
        return "origin-tools/form";
    }

    /**
     * 编辑HTTP接口页面
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        log.info("编辑原始HTTP接口: id={}", id);

        OriginToolHttp tool = originToolHttpService.getById(id);
        if (tool == null) {
            return "redirect:/origin-http-tools?error=工具不存在";
        }

        // 检查权限：只有工具的创建者可以编辑
        if (!PermissionUtil.hasPermission(session, tool.getProviderId())) {
            return "redirect:/origin-http-tools?error=无权限编辑此工具";
        }

        model.addAttribute("tool", tool);
        return "origin-tools/form";
    }

    /**
     * 保存HTTP接口
     */
    @PostMapping("/save")
    public String save(@ModelAttribute OriginToolHttp tool, HttpSession session) {
        log.info("保存原始HTTP接口: {}", tool);

        // 获取当前登录用户
        Long currentProviderId = PermissionUtil.getCurrentProviderId(session);
        if (currentProviderId == null) {
            return "redirect:/provider/login?error=" + "用户未登录";
        }

        String currentUser = PermissionUtil.getCurrentProvider(session).getUsername();

        if (tool.getId() == null) {
            // 新增
            tool.setProviderId(currentProviderId);
            tool.setCreateTime(LocalDateTime.now());
            tool.setCreateBy(currentUser);
            tool.setUpdateTime(LocalDateTime.now());
            tool.setUpdateBy(currentUser);
        } else {
            // 更新 - 检查权限
            OriginToolHttp existingTool = originToolHttpService.getById(tool.getId());
            if (existingTool == null) {
                return "redirect:/origin-http-tools?error=" + "工具不存在";
            }
            if (!PermissionUtil.hasPermission(session, existingTool.getProviderId())) {
                return "redirect:/origin-http-tools?error=" + "无权限修改此工具";
            }
            // 保持原有的providerId，不允许修改
            tool.setProviderId(existingTool.getProviderId());
            tool.setUpdateTime(LocalDateTime.now());
            tool.setUpdateBy(currentUser);
        }

        originToolHttpService.saveOrUpdate(tool);
        return "redirect:/origin-http-tools";
    }

    /**
     * 删除HTTP接口
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        log.info("删除原始HTTP接口: id={}", id);

        // 检查工具是否存在
        OriginToolHttp tool = originToolHttpService.getById(id);
        if (tool == null) {
            try {
                return "redirect:/origin-http-tools?error=" + URLEncoder.encode("工具不存在", "UTF-8");
            } catch (Exception e) {
                return "redirect:/origin-http-tools?error=tool_not_found";
            }
        }

        // 检查权限：只有工具的创建者可以删除
        if (!PermissionUtil.hasPermission(session, tool.getProviderId())) {
            try {
                return "redirect:/origin-http-tools?error=" + URLEncoder.encode("无权限删除此工具", "UTF-8");
            } catch (Exception e) {
                return "redirect:/origin-http-tools?error=no_permission";
            }
        }

        originToolHttpService.removeById(id);
        try {
            return "redirect:/origin-http-tools?success=" + URLEncoder.encode("工具删除成功", "UTF-8");
        } catch (Exception e) {
            return "redirect:/origin-http-tools?success=deleted";
        }
    }

    /**
     * 搜索HTTP接口
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpSession session) {
        log.info("搜索原始HTTP接口: keyword={}", keyword);

        // 获取当前登录用户ID
        Long currentProviderId = PermissionUtil.getCurrentProviderId(session);

        QueryWrapper<OriginToolHttp> queryWrapper = new QueryWrapper<>();
        if (currentProviderId != null) {
            // 只搜索当前用户的工具
            queryWrapper.eq("provider_id", currentProviderId)
                .and(wrapper -> wrapper
                    .like("name_display", keyword)
                    .or()
                    .like("desc_display", keyword)
                );
        } else {
            // 用户未登录，返回空结果
            queryWrapper.eq("1", "0");
        }

        Page<OriginToolHttp> toolPage = new Page<>(page, size);
        IPage<OriginToolHttp> result = originToolHttpService.page(toolPage, queryWrapper);

        model.addAttribute("tools", result.getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getPages());
        model.addAttribute("totalRecords", result.getTotal());
        model.addAttribute("keyword", keyword);

        return "origin-tools/list";
    }
}