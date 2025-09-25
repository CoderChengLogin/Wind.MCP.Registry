package cn.com.wind.mcp.registry.controller;

import java.time.LocalDateTime;

import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.service.OriginToolHttpService;
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
     * 工具列表页面
     */
    @GetMapping
    public String list(Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String method,
        @RequestParam(required = false) String url) {
        log.info("查询原始工具列表: page={}, size={}, name={}, method={}, url={}", page, size, name, method, url);

        Page<OriginToolHttp> toolPage = new Page<>(page, size);
        QueryWrapper<OriginToolHttp> queryWrapper = new QueryWrapper<>();

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
     * 工具详情页面
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.info("查询原始工具详情: id={}", id);

        OriginToolHttp tool = originToolHttpService.getById(id);
        if (tool == null) {
            return "redirect:/origin-tools";
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
     * 编辑工具页面
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        log.info("编辑原始工具: id={}", id);

        OriginToolHttp tool = originToolHttpService.getById(id);
        if (tool == null) {
            return "redirect:/origin-tools";
        }

        model.addAttribute("tool", tool);
        return "origin-tools/form";
    }

    /**
     * 保存工具
     */
    @PostMapping("/save")
    public String save(@ModelAttribute OriginToolHttp tool) {
        log.info("保存原始工具: {}", tool);

        try {
            if (tool.getId() == null) {
                // 新增
                tool.setProviderToolNum(System.currentTimeMillis()); // 生成工具编号
                tool.setCreateTime(LocalDateTime.now());
                tool.setCreateBy("system");
                tool.setUpdateTime(LocalDateTime.now());
                tool.setUpdateBy("system");
            } else {
                // 更新
                tool.setUpdateTime(LocalDateTime.now());
                tool.setUpdateBy("system");
            }

            originToolHttpService.saveOrUpdate(tool);
            return "redirect:/origin-tools?success=保存成功";
        } catch (Exception e) {
            log.error("保存原始工具失败", e);
            return "redirect:/origin-tools?error=" + e.getMessage();
        }
    }

    /**
     * 删除工具
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        log.info("删除原始工具: id={}", id);

        try {
            originToolHttpService.removeById(id);
            return "redirect:/origin-tools?success=删除成功";
        } catch (Exception e) {
            log.error("删除原始工具失败", e);
            return "redirect:/origin-tools?error=" + e.getMessage();
        }
    }

    /**
     * 搜索工具 - 支持多条件搜索
     */
    @GetMapping("/search")
    public String search(Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String method,
        @RequestParam(required = false) String url) {
        log.info("搜索原始工具: name={}, method={}, url={}", name, method, url);

        // 重定向到list方法处理搜索
        return list(model, page, size, name, method, url);
    }
}