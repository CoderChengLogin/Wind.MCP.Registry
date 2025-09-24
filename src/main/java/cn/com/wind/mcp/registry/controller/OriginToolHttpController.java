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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 * 原始HTTP工具Controller
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
     * HTTP工具列表页面
     */
    @GetMapping
    public String list(Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        log.info("查询原始HTTP工具列表: page={}, size={}", page, size);

        Page<OriginToolHttp> toolPage = new Page<OriginToolHttp>(page, size);
        IPage<OriginToolHttp> result = originToolHttpService.page(toolPage);

        model.addAttribute("tools", result.getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getPages());
        model.addAttribute("totalRecords", result.getTotal());

        return "origin-tools/list";
    }

    /**
     * HTTP工具详情页面
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.info("查询原始HTTP工具详情: id={}", id);

        OriginToolHttp tool = originToolHttpService.getById(id);
        if (tool == null) {
            return "redirect:/origin-http-tools";
        }

        model.addAttribute("tool", tool);
        return "origin-tools/detail";
    }

    /**
     * 新增HTTP工具页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("tool", new OriginToolHttp());
        return "origin-tools/form";
    }

    /**
     * 编辑HTTP工具页面
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        log.info("编辑原始HTTP工具: id={}", id);

        OriginToolHttp tool = originToolHttpService.getById(id);
        if (tool == null) {
            return "redirect:/origin-http-tools";
        }

        model.addAttribute("tool", tool);
        return "origin-tools/form";
    }

    /**
     * 保存HTTP工具
     */
    @PostMapping("/save")
    public String save(@ModelAttribute OriginToolHttp tool) {
        log.info("保存原始HTTP工具: {}", tool);

        if (tool.getId() == null) {
            // 新增
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
        return "redirect:/origin-http-tools";
    }

    /**
     * 删除HTTP工具
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        log.info("删除原始HTTP工具: id={}", id);

        originToolHttpService.removeById(id);
        return "redirect:/origin-http-tools";
    }

    /**
     * 搜索HTTP工具
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        log.info("搜索原始HTTP工具: keyword={}", keyword);

        QueryWrapper<OriginToolHttp> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name_display", keyword)
            .or()
            .like("desc_display", keyword);

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