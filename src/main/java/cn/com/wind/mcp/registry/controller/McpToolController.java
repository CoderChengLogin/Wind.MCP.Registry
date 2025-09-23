package cn.com.wind.mcp.registry.controller;

import java.time.LocalDateTime;
import java.util.List;

import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.service.McpToolService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * MCP工具Controller
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Slf4j
@Controller
@RequestMapping("/mcp-tools")
public class McpToolController {

    @Autowired
    private McpToolService mcpToolService;

    /**
     * 工具列表页面
     */
    @GetMapping
    public String list(Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        log.info("查询MCP工具列表: page={}, size={}", page, size);

        Page<McpTool> toolPage = new Page<>(page, size);
        IPage<McpTool> result = mcpToolService.page(toolPage);

        model.addAttribute("tools", result.getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getPages());
        model.addAttribute("totalRecords", result.getTotal());

        return "mcp-tools/list";
    }

    /**
     * 新增工具页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("tool", new McpTool());
        return "mcp-tools/form";
    }

    /**
     * 新增工具页面（兼容/new路径）
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("tool", new McpTool());
        return "mcp-tools/form";
    }

    /**
     * 编辑工具页面
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        log.info("编辑MCP工具: id={}", id);

        McpTool tool = mcpToolService.getById(id);
        if (tool == null) {
            return "redirect:/mcp-tools";
        }

        model.addAttribute("tool", tool);
        return "mcp-tools/form";
    }

    /**
     * 工具详情页面
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.info("查询MCP工具详情: id={}", id);

        McpTool tool = mcpToolService.getById(id);
        if (tool == null) {
            return "redirect:/mcp-tools";
        }

        model.addAttribute("tool", tool);
        return "mcp-tools/detail";
    }

    /**
     * 保存工具 - 表单提交
     */
    @PostMapping("/save")
    public String save(@ModelAttribute McpTool tool) {
        log.info("保存MCP工具: {}", tool);

        try {
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

            mcpToolService.saveOrUpdateWithValidation(tool);
            return "redirect:/mcp-tools";
        } catch (RuntimeException e) {
            log.error("保存MCP工具失败: {}", e.getMessage());
            return "redirect:/mcp-tools?error=" + e.getMessage();
        }
    }

    /**
     * 保存工具 - JSON API
     */
    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<String> saveApi(@RequestBody McpTool tool) {
        log.info("API保存MCP工具: {}", tool);

        try {
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

            mcpToolService.saveOrUpdateWithValidation(tool);
            return ResponseEntity.ok("保存成功");
        } catch (RuntimeException e) {
            log.error("保存MCP工具失败: {}", e.getMessage());
            return ResponseEntity.status(400).body("保存失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("保存MCP工具失败", e);
            return ResponseEntity.status(500).body("保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取工具列表 - JSON API
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<McpTool>> listApi(@RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        log.info("API查询MCP工具列表: page={}, size={}", page, size);

        try {
            Page<McpTool> toolPage = new Page<>(page, size);
            IPage<McpTool> result = mcpToolService.page(toolPage);
            return ResponseEntity.ok(result.getRecords());
        } catch (Exception e) {
            log.error("查询MCP工具列表失败", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * 删除工具
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        log.info("删除MCP工具: id={}", id);

        mcpToolService.removeById(id);
        return "redirect:/mcp-tools";
    }

    /**
     * 搜索工具
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        log.info("搜索MCP工具: keyword={}", keyword);

        QueryWrapper<McpTool> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", keyword)
            .or()
            .like("description", keyword);

        Page<McpTool> toolPage = new Page<>(page, size);
        IPage<McpTool> result = mcpToolService.page(toolPage, queryWrapper);

        model.addAttribute("tools", result.getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getPages());
        model.addAttribute("totalRecords", result.getTotal());
        model.addAttribute("keyword", keyword);

        return "mcp-tools/list";
    }

    /**
     * 根据唯一标识查找工具
     */
    @GetMapping("/api/unique/{uniqueId}")
    @ResponseBody
    public ResponseEntity<McpTool> findByUniqueId(@PathVariable String uniqueId) {
        log.info("根据唯一标识查找MCP工具: {}", uniqueId);

        try {
            McpTool tool = mcpToolService.findByUniqueId(uniqueId);
            if (tool != null) {
                return ResponseEntity.ok(tool);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("查找MCP工具失败", e);
            return ResponseEntity.status(500).build();
        }
    }
}