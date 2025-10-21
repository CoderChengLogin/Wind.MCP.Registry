package cn.com.wind.mcp.registry.controller;
// Force recompile

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.entity.OriginToolExpo;
import cn.com.wind.mcp.registry.service.OriginProviderConfigService;
import cn.com.wind.mcp.registry.service.OriginToolExpoService;
import cn.com.wind.mcp.registry.util.PermissionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.time.LocalDateTime;

/**
 * <p>
 * 原始Expo接口Controller
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Slf4j
@Controller
@RequestMapping("/origin-expo-tools")
public class OriginToolExpoController {

    @Autowired
    private OriginToolExpoService originToolExpoService;

    @Autowired
    private OriginProviderConfigService originProviderConfigService;

    /**
     * Expo接口列表页面
     *
     * @param model   模型
     * @param page    当前页码
     * @param size    每页大小
     * @param session 会话
     * @return 列表页面
     */
    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       HttpSession session) {
        log.info("查询原始Expo接口列表: page={}, size={}", page, size);

        Page<OriginToolExpo> toolPage = new Page<OriginToolExpo>(page, size);
        IPage<OriginToolExpo> result;

        // 获取当前登录用户ID
        Long currentProviderId = PermissionUtil.getCurrentProviderId(session);
        if (currentProviderId == null) {
            // 用户未登录，返回空结果
            result = originToolExpoService.page(toolPage, new QueryWrapper<OriginToolExpo>().eq("1", "0"));
        } else {
            // 查询用户自己的工具（默认行为）
            QueryWrapper<OriginToolExpo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("provider_id", currentProviderId);
            result = originToolExpoService.page(toolPage, queryWrapper);
        }

        model.addAttribute("tools", result.getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getPages());
        model.addAttribute("totalRecords", result.getTotal());
        model.addAttribute("toolType", "expo");

        return "origin-tools-expo/list";
    }

    /**
     * Expo接口详情页面
     *
     * @param id    工具ID
     * @param model 模型
     * @return 详情页面
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.info("查询原始Expo接口详情: id={}", id);

        OriginToolExpo tool = originToolExpoService.getById(id);
        if (tool == null) {
            return "redirect:/origin-expo-tools";
        }

        // 加载服务方App信息
        if (tool.getProviderAppNum() != null) {
            OriginProviderConfig providerApp = originProviderConfigService.getById(tool.getProviderAppNum());
            if (providerApp != null) {
                model.addAttribute("providerApp", providerApp);
            }
        }

        model.addAttribute("tool", tool);
        return "origin-tools-expo/detail";
    }

    /**
     * 新增Expo接口页面
     *
     * @param model 模型
     * @return 表单页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("tool", new OriginToolExpo());
        return "origin-tools-expo/form";
    }

    /**
     * 新增Expo接口页面 (new路径别名)
     *
     * @param toolNum 关联的MCP工具编号 (可选,用于自动关联)
     * @param model   模型
     * @return 表单页面
     */
    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long toolNum, Model model) {
        OriginToolExpo tool = new OriginToolExpo();

        // 如果传入了toolNum,自动设置为providerToolNum,实现自动关联
        if (toolNum != null) {
            tool.setProviderToolNum(toolNum);
            log.info("创建Expo接口并关联到MCP工具: toolNum={}", toolNum);
        }

        model.addAttribute("tool", tool);
        return "origin-tools-expo/form";
    }

    /**
     * 编辑Expo接口页面
     *
     * @param id      工具ID
     * @param model   模型
     * @param session 会话
     * @return 表单页面
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        log.info("编辑原始Expo接口: id={}", id);

        OriginToolExpo tool = originToolExpoService.getById(id);
        if (tool == null) {
            return "redirect:/origin-expo-tools?error=工具不存在";
        }

        // 检查权限：只有工具的创建者可以编辑
        if (!PermissionUtil.hasPermission(session, tool.getProviderId())) {
            return "redirect:/origin-expo-tools?error=无权限编辑此工具";
        }

        // 加载服务方App信息(用于前端回显应用名称)
        if (tool.getProviderAppNum() != null) {
            OriginProviderConfig providerApp = originProviderConfigService.getById(tool.getProviderAppNum());
            if (providerApp != null) {
                model.addAttribute("providerApp", providerApp);
            }
        }

        model.addAttribute("tool", tool);
        return "origin-tools-expo/form";
    }

    /**
     * 保存Expo接口
     *
     * @param tool    工具对象
     * @param session 会话
     * @return 重定向到列表页面
     */
    @PostMapping("/save")
    public String save(@ModelAttribute OriginToolExpo tool, HttpSession session) {
        log.info("保存原始Expo接口: {}", tool);

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

            // 自动生成providerToolNum: 使用时间戳确保唯一性
            if (tool.getProviderToolNum() == null) {
                tool.setProviderToolNum(System.currentTimeMillis());
            }
        } else {
            // 更新 - 检查权限
            OriginToolExpo existingTool = originToolExpoService.getById(tool.getId());
            if (existingTool == null) {
                return "redirect:/origin-expo-tools?error=" + "工具不存在";
            }
            if (!PermissionUtil.hasPermission(session, existingTool.getProviderId())) {
                return "redirect:/origin-expo-tools?error=" + "无权限修改此工具";
            }
            // 保持原有的providerId，不允许修改
            tool.setProviderId(existingTool.getProviderId());
            tool.setUpdateTime(LocalDateTime.now());
            tool.setUpdateBy(currentUser);
        }

        originToolExpoService.saveOrUpdate(tool);
        return "redirect:/origin-expo-tools";
    }

    /**
     * 删除Expo接口
     *
     * @param id      工具ID
     * @param session 会话
     * @return 重定向到列表页面
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        log.info("删除原始Expo接口: id={}", id);

        // 检查工具是否存在
        OriginToolExpo tool = originToolExpoService.getById(id);
        if (tool == null) {
            try {
                return "redirect:/origin-expo-tools?error=" + URLEncoder.encode("工具不存在", "UTF-8");
            } catch (Exception e) {
                return "redirect:/origin-expo-tools?error=tool_not_found";
            }
        }

        // 检查权限：只有工具的创建者可以删除
        if (!PermissionUtil.hasPermission(session, tool.getProviderId())) {
            try {
                return "redirect:/origin-expo-tools?error=" + URLEncoder.encode("无权限删除此工具", "UTF-8");
            } catch (Exception e) {
                return "redirect:/origin-expo-tools?error=no_permission";
            }
        }

        originToolExpoService.removeById(id);
        try {
            return "redirect:/origin-expo-tools?success=" + URLEncoder.encode("工具删除成功", "UTF-8");
        } catch (Exception e) {
            return "redirect:/origin-expo-tools?success=deleted";
        }
    }

    /**
     * 搜索Expo接口
     *
     * @param keyword 搜索关键词
     * @param model   模型
     * @param page    当前页码
     * @param size    每页大小
     * @param session 会话
     * @return 列表页面
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         HttpSession session) {
        log.info("搜索原始Expo接口: keyword={}", keyword);

        // 获取当前登录用户ID
        Long currentProviderId = PermissionUtil.getCurrentProviderId(session);

        QueryWrapper<OriginToolExpo> queryWrapper = new QueryWrapper<>();
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

        Page<OriginToolExpo> toolPage = new Page<>(page, size);
        IPage<OriginToolExpo> result = originToolExpoService.page(toolPage, queryWrapper);

        model.addAttribute("tools", result.getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getPages());
        model.addAttribute("totalRecords", result.getTotal());
        model.addAttribute("keyword", keyword);
        model.addAttribute("toolType", "expo");

        return "origin-tools-expo/list";
    }
}