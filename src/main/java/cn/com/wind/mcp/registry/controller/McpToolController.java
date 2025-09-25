package cn.com.wind.mcp.registry.controller;

import java.time.LocalDateTime;
import java.util.List;

import cn.com.wind.mcp.registry.dto.ToolWizardDto;
import cn.com.wind.mcp.registry.entity.ExpoTemplateConverter;
import cn.com.wind.mcp.registry.entity.HttpTemplateConverter;
import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.OriginToolExpo;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.mapper.ExpoTemplateConverterMapper;
import cn.com.wind.mcp.registry.mapper.HttpTemplateConverterMapper;
import cn.com.wind.mcp.registry.mapper.OriginToolExpoMapper;
import cn.com.wind.mcp.registry.mapper.OriginToolHttpMapper;
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

    @Autowired
    private OriginToolExpoMapper originToolExpoMapper;

    @Autowired
    private OriginToolHttpMapper originToolHttpMapper;

    @Autowired
    private ExpoTemplateConverterMapper expoTemplateConverterMapper;

    @Autowired
    private HttpTemplateConverterMapper httpTemplateConverterMapper;

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
     * 分步向导添加工具页面
     */
    @GetMapping("/wizard")
    public String wizardForm(Model model) {
        return "mcp-tools/wizard";
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

        // 查询关联的源工具信息
        OriginToolHttp httpTool = null;
        if (tool.getToolNum() != null) {
            QueryWrapper<OriginToolHttp> httpWrapper = new QueryWrapper<>();
            httpWrapper.eq("provider_tool_num", tool.getToolNum());
            httpTool = originToolHttpMapper.selectOne(httpWrapper);
        }

        // 查询关联的转换模板信息
        HttpTemplateConverter converter = null;
        if (tool.getToolNum() != null) {
            QueryWrapper<HttpTemplateConverter> converterWrapper = new QueryWrapper<>();
            converterWrapper.eq("tool_num", tool.getToolNum());
            converter = httpTemplateConverterMapper.selectOne(converterWrapper);
        }

        model.addAttribute("tool", tool);
        model.addAttribute("httpTool", httpTool);
        model.addAttribute("converter", converter);
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
                // 新增 - 自动生成工具编号和版本号
                tool.setToolNum(System.currentTimeMillis()); // 使用时间戳作为工具编号
                tool.setToolVersion(1L); // 默认版本为1
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
                // 新增 - 自动生成工具编号和版本号
                tool.setToolNum(System.currentTimeMillis()); // 使用时间戳作为工具编号
                tool.setToolVersion(1L); // 默认版本为1
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

    /**
     * 分步向导保存工具
     */
    @PostMapping("/wizard/save")
    public String saveWizard(@ModelAttribute ToolWizardDto wizardDto) {
        log.info("分步向导保存工具: {}", wizardDto);

        try {
            // 1. 保存原始工具
            Long providerToolNum = System.currentTimeMillis(); // 生成提供者工具编号

            if ("expo".equals(wizardDto.getToolType())) {
                // 保存Expo工具
                OriginToolExpo expoTool = new OriginToolExpo();
                expoTool.setProviderToolNum(providerToolNum);
                expoTool.setNameDisplay(wizardDto.getOriginTool().getName());
                expoTool.setDescDisplay(wizardDto.getOriginTool().getDescription());
                expoTool.setAppClass(wizardDto.getOriginTool().getAppClass());
                expoTool.setCommandId(wizardDto.getOriginTool().getCommandId());
                expoTool.setCreateTime(LocalDateTime.now());
                expoTool.setCreateBy("system");
                expoTool.setUpdateTime(LocalDateTime.now());
                expoTool.setUpdateBy("system");

                originToolExpoMapper.insert(expoTool);

                // 保存Expo模板转换器
                if (wizardDto.getConverter() != null) {
                    ExpoTemplateConverter converter = new ExpoTemplateConverter();
                    converter.setProviderToolNum(providerToolNum);
                    converter.setToolVersion(1L);
                    converter.setAppClass(wizardDto.getOriginTool().getAppClass());
                    converter.setCommandId(wizardDto.getOriginTool().getCommandId());
                    converter.setInputArgs(wizardDto.getConverter().getInputArgs());
                    converter.setOutputArgs(wizardDto.getConverter().getOutputArgs());
                    converter.setCreateTime(LocalDateTime.now());
                    converter.setCreateBy("system");
                    converter.setUpdateTime(LocalDateTime.now());
                    converter.setUpdateBy("system");

                    expoTemplateConverterMapper.insert(converter);
                }

            } else if ("http".equals(wizardDto.getToolType())) {
                // 保存HTTP工具
                OriginToolHttp httpTool = new OriginToolHttp();
                httpTool.setProviderToolNum(providerToolNum);
                httpTool.setNameDisplay(wizardDto.getOriginTool().getName());
                httpTool.setDescDisplay(wizardDto.getOriginTool().getDescription());
                httpTool.setReqUrl(wizardDto.getOriginTool().getUrl());
                httpTool.setReqMethod(wizardDto.getOriginTool().getMethod());
                httpTool.setReqHeaders(wizardDto.getOriginTool().getHeaders());
                httpTool.setCreateTime(LocalDateTime.now());
                httpTool.setCreateBy("system");
                httpTool.setUpdateTime(LocalDateTime.now());
                httpTool.setUpdateBy("system");

                originToolHttpMapper.insert(httpTool);

                // 保存HTTP模板转换器
                if (wizardDto.getConverter() != null) {
                    HttpTemplateConverter converter = new HttpTemplateConverter();
                    converter.setToolNum(providerToolNum);
                    converter.setProviderToolNum(providerToolNum);
                    converter.setReqUrl(wizardDto.getOriginTool().getUrl());
                    converter.setReqMethod(wizardDto.getOriginTool().getMethod());
                    converter.setReqHeaders(wizardDto.getConverter().getInputSchema());
                    converter.setReqBody(wizardDto.getConverter().getOutputSchema());
                    converter.setRespBody(wizardDto.getConverter().getOutputSchema());
                    converter.setCreateTime(LocalDateTime.now());
                    converter.setCreateBy("system");
                    converter.setUpdateTime(LocalDateTime.now());
                    converter.setUpdateBy("system");

                    httpTemplateConverterMapper.insert(converter);
                }
            }

            // 2. 保存MCP工具
            McpTool mcpTool = new McpTool();
            mcpTool.setToolNum(System.currentTimeMillis()); // 生成工具编号
            mcpTool.setToolVersion(1L); // 默认版本为1
            mcpTool.setToolName(wizardDto.getMcpTool().getToolName());
            mcpTool.setToolType(wizardDto.getMcpTool().getToolType());
            mcpTool.setToolDescription(wizardDto.getMcpTool().getToolDescription());
            mcpTool.setNameDisplay(wizardDto.getMcpTool().getNameDisplay());
            mcpTool.setDescriptionDisplay(wizardDto.getMcpTool().getDescriptionDisplay());
            mcpTool.setCreateTime(LocalDateTime.now());
            mcpTool.setCreateBy("system");
            mcpTool.setUpdateTime(LocalDateTime.now());
            mcpTool.setUpdateBy("system");

            mcpToolService.saveOrUpdateWithValidation(mcpTool);

            return "redirect:/mcp-tools?success=向导保存成功";

        } catch (Exception e) {
            log.error("分步向导保存工具失败", e);
            return "redirect:/mcp-tools/wizard?error=" + e.getMessage();
        }
    }
}