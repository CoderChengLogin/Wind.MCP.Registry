package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.entity.OriginToolExpo;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.OriginProviderConfigService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * <p>
     * 集成负载均衡器聚合视图,展示按app_name分组的节点统计信息
     * </p>
     *
     * @param session HttpSession
     * @param model   Model
     * @return 视图名称
     */
    @GetMapping("/list")
    public String listPage(HttpSession session, Model model) {
        Provider provider = (Provider) session.getAttribute("currentProvider");
        if (provider == null) {
            return "redirect:/provider/login";
        }

        try {
            // 获取当前用户的所有负载均衡器名称(app_name去重)
            QueryWrapper<OriginProviderConfig> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT app_name");
            wrapper.eq("provider_id", provider.getId());
            wrapper.eq("status", 1);
            wrapper.isNotNull("app_name");
            wrapper.ne("app_name", "");
            List<OriginProviderConfig> configs = originProviderConfigService.list(wrapper);

            // 提取app_name列表并按字母排序
            List<String> loadBalancerNames = configs.stream()
                    .map(OriginProviderConfig::getAppName)
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());

            // 为每个负载均衡器计算统计信息
            List<Map<String, Object>> loadBalancers = new java.util.ArrayList<>();
            for (String loadName : loadBalancerNames) {
                Map<String, Object> loadInfo = new HashMap<>();
                loadInfo.put("loadName", loadName);

                // 查询该负载下的所有节点
                QueryWrapper<OriginProviderConfig> nodeWrapper = new QueryWrapper<>();
                nodeWrapper.eq("provider_id", provider.getId());
                nodeWrapper.eq("app_name", loadName);
                nodeWrapper.eq("status", 1);
                List<OriginProviderConfig> nodes = originProviderConfigService.list(nodeWrapper);

                // 统计节点数和健康节点数
                long healthyCount = nodes.stream()
                        .filter(n -> n.getIsEnabled() != null && n.getIsEnabled())
                        .count();
                loadInfo.put("totalNodes", nodes.size());
                loadInfo.put("healthyNodes", healthyCount);

                // 统计站点类型数量
                long siteTypeCount = nodes.stream()
                        .map(OriginProviderConfig::getSiteType)
                        .filter(st -> st != null && !st.isEmpty())
                        .distinct()
                        .count();
                loadInfo.put("siteTypeCount", siteTypeCount);

                loadBalancers.add(loadInfo);
            }

            model.addAttribute("loadBalancers", loadBalancers);
            model.addAttribute("providerId", provider.getId());
        } catch (Exception e) {
            log.error("加载负载均衡器信息失败", e);
            model.addAttribute("loadBalancers", new java.util.ArrayList<>());
            model.addAttribute("providerId", provider.getId());
        }

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
            Provider provider = (Provider) session.getAttribute("currentProvider");
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
     * <p>
     * 按站点类型分组返回节点列表，用于负载均衡器展开视图
     * </p>
     *
     * @param appName 应用名称
     * @param session HttpSession
     * @return 按站点类型分组的节点列表
     */
    @GetMapping(value = "/listByAppName", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Map<String, Object> listByAppName(@RequestParam String appName, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Provider provider = (Provider) session.getAttribute("currentProvider");
            if (provider == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            // 根据提供商ID和应用名称查询
            QueryWrapper<OriginProviderConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("provider_id", provider.getId());
            queryWrapper.eq("app_name", appName);
            queryWrapper.eq("status", 1); // 只查询有效的
            queryWrapper.orderByAsc("site_type", "app_ip");
            List<OriginProviderConfig> nodes = originProviderConfigService.list(queryWrapper);

            // 按站点类型分组
            Map<String, List<OriginProviderConfig>> nodesBySite = nodes.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            node -> node.getSiteType() != null && !node.getSiteType().isEmpty()
                                    ? node.getSiteType()
                                    : "未分类"
                    ));

            result.put("success", true);
            result.put("nodesBySite", nodesBySite);
            result.put("totalCount", nodes.size());
        } catch (Exception e) {
            log.error("查询应用节点失败: appName={}", appName, e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取应用节点详情
     * <p>
     * 用于查看和编辑功能,返回节点的完整信息
     * </p>
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
                result.put("data", app);  // 使用data作为key,匹配前端期望
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
            Provider provider = (Provider) session.getAttribute("currentProvider");
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
            Provider provider = (Provider) session.getAttribute("currentProvider");
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
     * 只返回当前登录用户已启用的应用,按app_name分组去重
     * </p>
     *
     * @param session HttpSession
     * @return 已启用的应用列表(按app_name去重)
     */
    @GetMapping("/listEnabled")
    @ResponseBody
    public Map<String, Object> listEnabled(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Provider provider = (Provider) session.getAttribute("currentProvider");
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
            queryWrapper.orderByAsc("app_name");
            List<OriginProviderConfig> allApps = originProviderConfigService.list(queryWrapper);

            // 按app_name分组,每个应用只保留一个代表节点(优先选择负载因子最高的)
            Map<String, OriginProviderConfig> appMap = new java.util.LinkedHashMap<>();
            for (OriginProviderConfig app : allApps) {
                String appName = app.getAppName();
                if (appName != null && !appName.isEmpty()) {
                    // 如果该应用名已存在,比较负载因子,保留更高的
                    if (!appMap.containsKey(appName)) {
                        appMap.put(appName, app);
                    } else {
                        OriginProviderConfig existing = appMap.get(appName);
                        int existingFactor = existing.getLoadFactor() != null ? existing.getLoadFactor() : 0;
                        int currentFactor = app.getLoadFactor() != null ? app.getLoadFactor() : 0;
                        if (currentFactor > existingFactor) {
                            appMap.put(appName, app);
                        }
                    }
                }
            }

            List<OriginProviderConfig> uniqueApps = new java.util.ArrayList<>(appMap.values());
            result.put("success", true);
            result.put("apps", uniqueApps);
        } catch (Exception e) {
            log.error("获取已启用应用列表失败", e);
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据app_num查询关联的HTTP工具
     * <p>
     * 返回该应用节点关联的所有HTTP工具列表
     * </p>
     *
     * @param appNum 应用编号
     * @return HTTP工具列表
     */
    @GetMapping("/relatedHttpTools/{appNum}")
    @ResponseBody
    public Map<String, Object> getRelatedHttpTools(@PathVariable Long appNum) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<OriginToolHttp> httpTools = originProviderConfigService.getRelatedHttpTools(appNum);
            result.put("success", true);
            result.put("tools", httpTools);
            result.put("count", httpTools.size());
        } catch (Exception e) {
            log.error("查询关联HTTP工具失败, appNum={}", appNum, e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据app_num查询关联的Expo工具
     * <p>
     * 返回该应用节点关联的所有Expo工具列表
     * </p>
     *
     * @param appNum 应用编号
     * @return Expo工具列表
     */
    @GetMapping("/relatedExpoTools/{appNum}")
    @ResponseBody
    public Map<String, Object> getRelatedExpoTools(@PathVariable Long appNum) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<OriginToolExpo> expoTools = originProviderConfigService.getRelatedExpoTools(appNum);
            result.put("success", true);
            result.put("tools", expoTools);
            result.put("count", expoTools.size());
        } catch (Exception e) {
            log.error("查询关联Expo工具失败, appNum={}", appNum, e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 根据app_num查询关联的所有工具(HTTP + Expo)
     * <p>
     * 返回该应用节点关联的所有工具的汇总信息
     * </p>
     *
     * @param appNum 应用编号
     * @return 关联工具汇总
     */
    @GetMapping("/relatedTools/{appNum}")
    @ResponseBody
    public Map<String, Object> getRelatedTools(@PathVariable Long appNum) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<OriginToolHttp> httpTools = originProviderConfigService.getRelatedHttpTools(appNum);
            List<OriginToolExpo> expoTools = originProviderConfigService.getRelatedExpoTools(appNum);

            result.put("success", true);
            result.put("httpTools", httpTools);
            result.put("httpCount", httpTools.size());
            result.put("expoTools", expoTools);
            result.put("expoCount", expoTools.size());
            result.put("totalCount", httpTools.size() + expoTools.size());
        } catch (Exception e) {
            log.error("查询关联工具失败, appNum={}", appNum, e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }
}
