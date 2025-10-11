package cn.com.wind.mcp.registry.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.OriginProviderConfigService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 提供者应用服务节点控制器
 * <p>
 * 提供应用节点的管理功能,包括列表展示、增删改查等
 * </p>
 *
 * @author system
 * @date Created in 2025-10-11
 */
@Slf4j
@Controller
@RequestMapping("/provider-app")
public class ProviderAppController {

    @Autowired
    private OriginProviderConfigService originProviderConfigService;

    /**
     * 应用节点管理页面
     *
     * @param session HttpSession
     * @param model   Model
     * @return 视图名称
     */
    @GetMapping("/list")
    public String listPage(HttpSession session, Model model) {
        Provider provider = (Provider)session.getAttribute("currentProvider");
        if (provider == null) {
            return "redirect:/provider/login";
        }
        model.addAttribute("providerId", provider.getId());
        return "provider-app/list";
    }

    /**
     * 分页查询应用节点列表
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param appName   应用名称(可选)
     * @param siteType  站点类型(可选)
     * @param isEnabled 是否启用(可选)
     * @param session   HttpSession
     * @return 分页结果
     */
    @GetMapping("/page")
    @ResponseBody
    public Map<String, Object> page(@RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String appName,
        @RequestParam(required = false) String siteType,
        @RequestParam(required = false) Boolean isEnabled,
        HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Provider provider = (Provider)session.getAttribute("currentProvider");
            if (provider == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            // 构建查询条件
            QueryWrapper<OriginProviderConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("provider_id", provider.getId());
            queryWrapper.ne("status", -1); // 排除已删除的

            if (StrUtil.isNotBlank(appName)) {
                queryWrapper.like("app_name", appName);
            }
            if (StrUtil.isNotBlank(siteType)) {
                queryWrapper.eq("site_type", siteType);
            }
            if (isEnabled != null) {
                queryWrapper.eq("is_enabled", isEnabled);
            }

            queryWrapper.orderByDesc("create_time");

            // 分页查询
            Page<OriginProviderConfig> page = new Page<>(pageNum, pageSize);
            Page<OriginProviderConfig> pageResult = originProviderConfigService.page(page, queryWrapper);

            result.put("success", true);
            result.put("total", pageResult.getTotal());
            result.put("records", pageResult.getRecords());
            result.put("pages", pageResult.getPages());
            result.put("current", pageResult.getCurrent());
            result.put("size", pageResult.getSize());
        } catch (Exception e) {
            log.error("分页查询应用节点失败", e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据应用名称查询节点列表(用于弹窗展示)
     *
     * @param appName 应用名称
     * @param session HttpSession
     * @return 节点列表
     */
    @GetMapping("/listByAppName")
    @ResponseBody
    public Map<String, Object> listByAppName(@RequestParam String appName, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Provider provider = (Provider)session.getAttribute("currentProvider");
            if (provider == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            // 根据提供商ID和应用名称查询
            QueryWrapper<OriginProviderConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("provider_id", provider.getId());
            queryWrapper.eq("app_name", appName);
            queryWrapper.ne("status", -1);
            List<OriginProviderConfig> apps = originProviderConfigService.list(queryWrapper);
            result.put("success", true);
            result.put("apps", apps);
        } catch (Exception e) {
            log.error("查询应用节点失败", e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取应用节点详情
     *
     * @param id 应用节点ID
     * @return 应用节点信息
     */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public Map<String, Object> detail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            OriginProviderConfig app = originProviderConfigService.getById(id);
            if (app != null && app.getStatus() != -1) {
                result.put("success", true);
                result.put("app", app);
            } else {
                result.put("success", false);
                result.put("message", "应用节点不存在");
            }
        } catch (Exception e) {
            log.error("获取应用节点详情失败", e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 创建应用节点
     *
     * @param providerApp 应用节点信息
     * @param session     HttpSession
     * @return 操作结果
     */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> create(@RequestBody OriginProviderConfig providerConfig, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Provider provider = (Provider)session.getAttribute("currentProvider");
            if (provider == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            // 设置提供者ID和创建人
            providerConfig.setProviderId(provider.getId());
            providerConfig.setCreateBy(provider.getUsername());
            providerConfig.setUpdateBy(provider.getUsername());

            boolean success = originProviderConfigService.createConfig(providerConfig);
            if (success) {
                result.put("success", true);
                result.put("message", "创建成功");
            } else {
                result.put("success", false);
                result.put("message", "创建失败,请检查配置是否正确");
            }
        } catch (Exception e) {
            log.error("创建应用节点失败", e);
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新应用节点
     *
     * @param providerApp 应用节点信息
     * @param session     HttpSession
     * @return 操作结果
     */
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> update(@RequestBody OriginProviderConfig providerConfig, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Provider provider = (Provider)session.getAttribute("currentProvider");
            if (provider == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            // 设置更新人
            providerConfig.setUpdateBy(provider.getUsername());

            boolean success = originProviderConfigService.updateConfig(providerConfig);
            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败,请检查配置是否正确");
            }
        } catch (Exception e) {
            log.error("更新应用节点失败", e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 启用/禁用应用节点
     *
     * @param id        应用节点ID
     * @param isEnabled 是否启用
     * @return 操作结果
     */
    @PostMapping("/toggleEnable")
    @ResponseBody
    public Map<String, Object> toggleEnable(@RequestParam Long id, @RequestParam Boolean isEnabled) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = isEnabled
                ? originProviderConfigService.enableConfig(id)
                : originProviderConfigService.disableConfig(id);
            if (success) {
                result.put("success", true);
                result.put("message", isEnabled ? "启用成功" : "禁用成功");
            } else {
                result.put("success", false);
                result.put("message", "操作失败");
            }
        } catch (Exception e) {
            log.error("启用/禁用应用节点失败", e);
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 删除应用节点
     *
     * @param id 应用节点ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = originProviderConfigService.deleteConfig(id);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除应用节点失败", e);
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 批量删除应用节点
     *
     * @param ids 应用节点ID列表
     * @return 操作结果
     */
    @PostMapping("/batchDelete")
    @ResponseBody
    public Map<String, Object> batchDelete(@RequestBody List<Long> ids) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 批量删除
            boolean success = true;
            for (Long id : ids) {
                if (!originProviderConfigService.deleteConfig(id)) {
                    success = false;
                    break;
                }
            }
            if (success) {
                result.put("success", true);
                result.put("message", "批量删除成功");
            } else {
                result.put("success", false);
                result.put("message", "批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除应用节点失败", e);
            result.put("success", false);
            result.put("message", "批量删除失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 检查应用节点健康状态
     *
     * @param id 应用节点ID
     * @return 健康状态信息
     */
    @GetMapping("/checkHealth/{id}")
    @ResponseBody
    public Map<String, Object> checkHealth(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取应用配置并检查健康状态
            OriginProviderConfig config = originProviderConfigService.getById(id);
            if (config != null && config.getHealthCheckUrl() != null) {
                // 简单返回配置的健康检查URL，具体检查逻辑可以后续实现
                result.put("success", true);
                result.put("healthStatus", "健康检查URL: " + config.getHealthCheckUrl());
                result.put("message", "请访问健康检查URL进行验证");
            } else {
                result.put("success", false);
                result.put("message", "应用配置不存在或未配置健康检查URL");
            }
        } catch (Exception e) {
            log.error("检查健康状态失败", e);
            result.put("success", false);
            result.put("message", "检查失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取所有已启用的应用列表(用于下拉选择)
     * <p>
     * 此接口用于在工具录入页面中,将服务方app字段从输入框改为下拉选择
     * 只返回当前登录用户已启用的应用
     * </p>
     *
     * @param session HttpSession
     * @return 已启用的应用列表
     */
    @GetMapping("/listEnabled")
    @ResponseBody
    public Map<String, Object> listEnabled(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Provider provider = (Provider)session.getAttribute("currentProvider");
            if (provider == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            // 查询已启用的应用配置
            QueryWrapper<OriginProviderConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("provider_id", provider.getId());
            queryWrapper.eq("is_enabled", true);
            queryWrapper.eq("status", 1);
            List<OriginProviderConfig> enabledApps = originProviderConfigService.list(queryWrapper);
            result.put("success", true);
            result.put("apps", enabledApps);
        } catch (Exception e) {
            log.error("获取已启用应用列表失败", e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }

        return result;
    }
}
