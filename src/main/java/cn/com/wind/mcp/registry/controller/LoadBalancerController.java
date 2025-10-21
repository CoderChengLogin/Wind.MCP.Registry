package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.entity.OriginProviderConfig;
import cn.com.wind.mcp.registry.service.OriginProviderConfigService;
import cn.com.wind.mcp.registry.service.ProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡器控制器
 * <p>
 * 参考Wind Share设计,以app_name作为负载名称(主实体),聚合各个站点的服务节点配置信息
 * </p>
 * <p>
 * 设计思想:
 * - 相同app_name的多条记录 = 一个负载均衡器的多个服务节点
 * - app_num用于唯一标识每个服务节点
 * - site_type用于按站点分组展示节点
 * </p>
 *
 * @author Claude Code
 * @date 2025-10-11
 */
@Slf4j
@Controller
@RequestMapping("/load-balancer")
public class LoadBalancerController {

    @Autowired
    private OriginProviderConfigService originProviderConfigService;

    @Autowired
    private ProviderService providerService;

    /**
     * 负载均衡器列表页面
     * <p>
     * 展示所有负载均衡器(按app_name聚合),类似Wind Share的主表格
     * </p>
     *
     * @param model Spring MVC Model
     * @return 负载均衡器列表页面
     */
    @GetMapping("/list")
    public String list(Model model) {
        try {
            log.info("访问负载均衡器列表页面");

            // 获取所有负载均衡器名称(去重的app_name列表)
            List<String> loadBalancerNames = originProviderConfigService.getAllLoadBalancerNames();

            // 为每个负载构建统计信息
            List<Map<String, Object>> loadBalancers = new java.util.ArrayList<>();
            for (String loadName : loadBalancerNames) {
                List<OriginProviderConfig> nodes = originProviderConfigService.getNodesByLoadName(loadName);
                if (!nodes.isEmpty()) {
                    // 取第一个节点的公共信息
                    OriginProviderConfig firstNode = nodes.get(0);

                    // 统计健康节点数(is_enabled=true且status=1)
                    long healthyCount = nodes.stream()
                            .filter(n -> n.getIsEnabled() != null && n.getIsEnabled() && n.getStatus() == 1)
                            .count();

                    Map<String, Object> loadInfo = new HashMap<>();
                    loadInfo.put("loadName", loadName);
                    loadInfo.put("providerId", firstNode.getProviderId());
                    loadInfo.put("healthCheckUrl", firstNode.getHealthCheckUrl());
                    loadInfo.put("healthCheckInterval", firstNode.getHealthCheckInterval());
                    loadInfo.put("totalNodes", nodes.size());
                    loadInfo.put("healthyNodes", healthyCount);
                    loadInfo.put("createTime", firstNode.getCreateTime());

                    loadBalancers.add(loadInfo);
                }
            }

            model.addAttribute("loadBalancers", loadBalancers);
            model.addAttribute("providers", providerService.list());

            log.info("查询到 {} 个负载均衡器", loadBalancers.size());
            return "load-balancer/list";
        } catch (Exception e) {
            log.error("查询负载均衡器列表失败", e);
            model.addAttribute("error", "查询失败: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 查询指定负载的所有服务节点(API接口)
     * <p>
     * 返回指定app_name下的所有节点,按站点类型分组
     * 用于前端模态框展示服务节点列表
     * </p>
     *
     * @param loadName 负载名称(app_name)
     * @return JSON响应 {success: boolean, data: Map<站点类型, 节点列表>}
     */
    @GetMapping("/nodes/{loadName}")
    @ResponseBody
    public Map<String, Object> getNodes(@PathVariable String loadName) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("查询负载 {} 的服务节点", loadName);

            // 按站点类型分组查询节点
            Map<String, List<OriginProviderConfig>> nodesBySite =
                    originProviderConfigService.getNodesGroupBySiteType(loadName);

            result.put("success", true);
            result.put("data", nodesBySite);
            result.put("message", "查询成功");

            log.info("负载 {} 共有 {} 个站点类型", loadName, nodesBySite.size());
        } catch (Exception e) {
            log.error("查询服务节点失败: loadName={}", loadName, e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 查询指定负载的所有服务节点(不分组)
     * <p>
     * 返回指定app_name下的所有节点列表,不按站点分组
     * </p>
     *
     * @param loadName 负载名称(app_name)
     * @return JSON响应 {success: boolean, nodes: List<OriginProviderConfig>}
     */
    @GetMapping("/nodes/list/{loadName}")
    @ResponseBody
    public Map<String, Object> getNodesList(@PathVariable String loadName) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("查询负载 {} 的服务节点列表", loadName);

            List<OriginProviderConfig> nodes = originProviderConfigService.getNodesByLoadName(loadName);

            result.put("success", true);
            result.put("nodes", nodes);
            result.put("count", nodes.size());
            result.put("message", "查询成功");

            log.info("负载 {} 共有 {} 个服务节点", loadName, nodes.size());
        } catch (Exception e) {
            log.error("查询服务节点列表失败: loadName={}", loadName, e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 创建新的服务节点
     * <p>
     * 为指定的负载(app_name)添加新的服务节点
     * 会自动生成app_num(使用雪花算法)
     * </p>
     *
     * @param config 节点配置信息(包含app_name, site_type, app_ip等)
     * @return JSON响应 {success: boolean, message: string}
     */
    @PostMapping("/node/create")
    @ResponseBody
    public Map<String, Object> createNode(@RequestBody OriginProviderConfig config) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("创建新服务节点: loadName={}, siteType={}, ip={}",
                    config.getAppName(), config.getSiteType(), config.getAppIp());

            boolean success = originProviderConfigService.createConfig(config);

            result.put("success", success);
            result.put("message", success ? "创建成功" : "创建失败");

            if (success) {
                log.info("服务节点创建成功: app_num={}", config.getAppNum());
            }
        } catch (Exception e) {
            log.error("创建服务节点失败", e);
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新服务节点配置
     *
     * @param config 节点配置信息
     * @return JSON响应 {success: boolean, message: string}
     */
    @PostMapping("/node/update")
    @ResponseBody
    public Map<String, Object> updateNode(@RequestBody OriginProviderConfig config) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("更新服务节点: id={}", config.getId());

            boolean success = originProviderConfigService.updateConfig(config);

            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            log.error("更新服务节点失败", e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除服务节点
     *
     * @param id 节点ID
     * @return JSON响应 {success: boolean, message: string}
     */
    @PostMapping("/node/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteNode(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("删除服务节点: id={}", id);

            boolean success = originProviderConfigService.deleteConfig(id);

            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            log.error("删除服务节点失败", e);
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 启用/禁用服务节点
     *
     * @param id      节点ID
     * @param enabled 是否启用
     * @return JSON响应 {success: boolean, message: string}
     */
    @PostMapping("/node/toggle/{id}")
    @ResponseBody
    public Map<String, Object> toggleNode(@PathVariable Long id, @RequestParam Boolean enabled) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("切换服务节点状态: id={}, enabled={}", id, enabled);

            boolean success = enabled ?
                    originProviderConfigService.enableConfig(id) :
                    originProviderConfigService.disableConfig(id);

            result.put("success", success);
            result.put("message", success ? "操作成功" : "操作失败");
        } catch (Exception e) {
            log.error("切换服务节点状态失败", e);
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
        }
        return result;
    }
}
