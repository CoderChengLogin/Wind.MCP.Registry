package cn.com.wind.mcp.registry.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpSession;

import cn.com.wind.mcp.registry.dto.McpToolEditDto;
import cn.com.wind.mcp.registry.entity.HttpTemplateConverter;
import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;
import cn.com.wind.mcp.registry.mapper.HttpTemplateConverterMapper;
import cn.com.wind.mcp.registry.mapper.OriginToolHttpMapper;
import cn.com.wind.mcp.registry.service.McpToolService;
import cn.com.wind.mcp.registry.util.PermissionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private OriginToolHttpMapper originToolHttpMapper;

    @Autowired
    private HttpTemplateConverterMapper httpTemplateConverterMapper;

    /**
     * 工具列表页面
     */
    @GetMapping
    public String list(Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "all") String view,
        HttpSession session) {
        log.info("查询MCP工具列表: page={}, size={}, view={}", page, size, view);

        Page<McpTool> toolPage = new Page<>(page, size);
        IPage<McpTool> result;

        // 获取当前登录用户ID
        Long currentProviderId = PermissionUtil.getCurrentProviderId(session);
        if (currentProviderId == null) {
            // 用户未登录，返回空结果
            result = mcpToolService.page(toolPage, new QueryWrapper<McpTool>().eq("1", "0"));
        } else {
            // 查询用户自己的工具（默认行为）
            QueryWrapper<McpTool> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("provider_id", currentProviderId);
            result = mcpToolService.page(toolPage, queryWrapper);
        }

        model.addAttribute("tools", result.getRecords());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getPages());
        model.addAttribute("totalRecords", result.getTotal());
        model.addAttribute("currentView", view);
        model.addAttribute("currentProviderId", PermissionUtil.getCurrentProviderId(session));

        return "mcp-tools/list";
    }

    /**
     * 编辑工具页面
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        log.info("编辑MCP工具: id={}", id);

        try {
            McpTool tool = mcpToolService.getById(id);
            if (tool == null) {
                return "redirect:/mcp-tools?error=" + URLEncoder.encode("工具不存在", StandardCharsets.UTF_8.name());
            }

            // 检查权限：只有工具的创建者可以编辑
            if (!PermissionUtil.hasPermission(session, tool.getProviderId())) {
                return "redirect:/mcp-tools?error=" + URLEncoder.encode("无权限编辑此工具", StandardCharsets.UTF_8.name());
            }

            // 查询关联的源HTTP工具信息
            OriginToolHttp httpTool = null;
            if (tool.getToolNum() != null) {
                QueryWrapper<OriginToolHttp> httpWrapper = new QueryWrapper<>();
                httpWrapper.eq("provider_tool_num", tool.getToolNum());
                httpTool = originToolHttpMapper.selectOne(httpWrapper);
            }

            // 查询关联的HTTP转换模板信息
            HttpTemplateConverter converter = null;
            if (tool.getToolNum() != null) {
                QueryWrapper<HttpTemplateConverter> converterWrapper = new QueryWrapper<>();
                converterWrapper.eq("tool_num", tool.getToolNum());
                converter = httpTemplateConverterMapper.selectOne(converterWrapper);
            }

            model.addAttribute("tool", tool);
            model.addAttribute("httpTool", httpTool);
            model.addAttribute("converter", converter);
            return "mcp-tools/form";
        } catch (UnsupportedEncodingException e) {
            log.error("URL编码失败", e);
            return "redirect:/mcp-tools?error=system_error";
        }
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
     * 保存工具 - JSON API（支持多表编辑）
     */
    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<String> saveApi(@RequestBody McpToolEditDto toolDto, HttpSession session) {
        log.info("API保存MCP工具: {}", toolDto);

        try {
            Long currentProviderId = PermissionUtil.getCurrentProviderId(session);
            if (currentProviderId == null) {
                return ResponseEntity.status(401).body("用户未登录");
            }

            // 获取MCP工具基本信息
            McpTool tool = new McpTool();
            copyBasicProperties(toolDto, tool);

            if (tool.getId() == null) {
                // 新增 - 自动生成工具编号和版本号
                tool.setToolNum(System.currentTimeMillis()); // 使用时间戳作为工具编号
                tool.setToolVersion(1L); // 默认版本为1
                tool.setProviderId(currentProviderId); // 设置创建者ID
                tool.setCreateTime(LocalDateTime.now());
                tool.setCreateBy(PermissionUtil.getCurrentProvider(session).getUsername());
                tool.setUpdateTime(LocalDateTime.now());
                tool.setUpdateBy(PermissionUtil.getCurrentProvider(session).getUsername());
            } else {
                // 更新 - 检查权限
                McpTool existingTool = mcpToolService.getById(tool.getId());
                if (existingTool == null) {
                    return ResponseEntity.status(404).body("工具不存在");
                }
                if (!PermissionUtil.hasPermission(session, existingTool.getProviderId())) {
                    return ResponseEntity.status(403).body("无权限修改此工具");
                }
                // 保持原有的providerId和工具编号，不允许修改
                tool.setProviderId(existingTool.getProviderId());
                tool.setToolNum(existingTool.getToolNum());
                tool.setUpdateTime(LocalDateTime.now());
                tool.setUpdateBy(PermissionUtil.getCurrentProvider(session).getUsername());
            }

            // 保存MCP工具
            mcpToolService.saveOrUpdateWithValidation(tool);

            // 处理源HTTP工具数据
            if (toolDto.getHttpTool() != null) {
                updateHttpTool(toolDto.getHttpTool(), tool, currentProviderId, session);
            }

            // 处理转换器数据
            if (toolDto.getConverter() != null) {
                updateConverter(toolDto.getConverter(), tool, currentProviderId, session);
            }

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
     * 复制基本属性
     */
    private void copyBasicProperties(McpToolEditDto source, McpTool target) {
        target.setId(source.getId());
        target.setToolName(source.getToolName());
        target.setToolDescription(source.getToolDescription());
        target.setNameDisplay(source.getNameDisplay());
        target.setDescriptionDisplay(source.getDescriptionDisplay());
        target.setInputSchema(source.getInputSchema());
        target.setOutputSchema(source.getOutputSchema());
        target.setStreamOutput(source.getStreamOutput());
        target.setConvertType(source.getConvertType());
        target.setToolType(source.getToolType());
        target.setValid(source.getValid());
    }

    /**
     * 更新源HTTP工具信息
     */
    private void updateHttpTool(OriginToolHttp httpToolData, McpTool mcpTool, Long providerId, HttpSession session) {
        // 查询已存在的HTTP工具
        QueryWrapper<OriginToolHttp> wrapper = new QueryWrapper<>();
        wrapper.eq("provider_tool_num", mcpTool.getToolNum());
        OriginToolHttp existingHttpTool = originToolHttpMapper.selectOne(wrapper);

        if (existingHttpTool != null) {
            // 更新现有记录
            existingHttpTool.setNameDisplay(httpToolData.getNameDisplay());
            existingHttpTool.setDescDisplay(httpToolData.getDescDisplay());
            existingHttpTool.setReqUrl(httpToolData.getReqUrl());
            existingHttpTool.setReqMethod(httpToolData.getReqMethod());
            existingHttpTool.setReqHeaders(httpToolData.getReqHeaders());
            existingHttpTool.setInputSchema(httpToolData.getInputSchema());
            existingHttpTool.setOutputSchema(httpToolData.getOutputSchema());
            existingHttpTool.setUpdateTime(LocalDateTime.now());
            existingHttpTool.setUpdateBy(PermissionUtil.getCurrentProvider(session).getUsername());
            originToolHttpMapper.updateById(existingHttpTool);
        } else {
            // 创建新记录
            httpToolData.setProviderId(providerId);
            httpToolData.setProviderToolNum(mcpTool.getToolNum());
            httpToolData.setCreateTime(LocalDateTime.now());
            httpToolData.setCreateBy(PermissionUtil.getCurrentProvider(session).getUsername());
            httpToolData.setUpdateTime(LocalDateTime.now());
            httpToolData.setUpdateBy(PermissionUtil.getCurrentProvider(session).getUsername());
            originToolHttpMapper.insert(httpToolData);
        }
    }

    /**
     * 更新转换器信息
     */
    private void updateConverter(HttpTemplateConverter converterData, McpTool mcpTool, Long providerId,
        HttpSession session) {
        // 查询已存在的转换器
        QueryWrapper<HttpTemplateConverter> wrapper = new QueryWrapper<>();
        wrapper.eq("tool_num", mcpTool.getToolNum());
        HttpTemplateConverter existingConverter = httpTemplateConverterMapper.selectOne(wrapper);

        if (existingConverter != null) {
            // 更新现有记录
            existingConverter.setReqUrl(converterData.getReqUrl());
            existingConverter.setReqMethod(converterData.getReqMethod());
            existingConverter.setReqHeaders(converterData.getReqHeaders());
            existingConverter.setReqBody(converterData.getReqBody());
            existingConverter.setRespBody(converterData.getRespBody());
            existingConverter.setUpdateTime(LocalDateTime.now());
            existingConverter.setUpdateBy(PermissionUtil.getCurrentProvider(session).getUsername());
            httpTemplateConverterMapper.updateById(existingConverter);
        } else {
            // 创建新记录
            converterData.setProviderId(providerId);
            converterData.setToolNum(mcpTool.getToolNum());
            converterData.setToolVersion(mcpTool.getToolVersion());
            converterData.setProviderToolNum(mcpTool.getToolNum());
            converterData.setCreateTime(LocalDateTime.now());
            converterData.setCreateBy(PermissionUtil.getCurrentProvider(session).getUsername());
            converterData.setUpdateTime(LocalDateTime.now());
            converterData.setUpdateBy(PermissionUtil.getCurrentProvider(session).getUsername());
            httpTemplateConverterMapper.insert(converterData);
        }
    }

    /**
     * 获取工具列表 - JSON API
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<McpTool>> listApi(@RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpSession session) {
        log.info("API查询MCP工具列表: page={}, size={}", page, size);

        try {
            // 获取当前登录用户ID
            Long currentProviderId = PermissionUtil.getCurrentProviderId(session);
            if (currentProviderId == null) {
                return ResponseEntity.status(401).body(null);
            }

            Page<McpTool> toolPage = new Page<>(page, size);
            QueryWrapper<McpTool> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("provider_id", currentProviderId);
            IPage<McpTool> result = mcpToolService.page(toolPage, queryWrapper);
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
    public String delete(@PathVariable Long id, HttpSession session) {
        log.info("删除MCP工具: id={}", id);

        // 检查工具是否存在
        McpTool tool = mcpToolService.getById(id);
        if (tool == null) {
            return "redirect:/mcp-tools?error=工具不存在";
        }

        // 检查权限：只有工具的创建者可以删除
        if (!PermissionUtil.hasPermission(session, tool.getProviderId())) {
            return "redirect:/mcp-tools?error=无权限删除此工具";
        }

        mcpToolService.removeById(id);
        return "redirect:/mcp-tools?success=工具删除成功";
    }

    /**
     * 搜索工具
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpSession session) {
        log.info("搜索MCP工具: keyword={}", keyword);

        // 获取当前登录用户ID
        Long currentProviderId = PermissionUtil.getCurrentProviderId(session);

        QueryWrapper<McpTool> queryWrapper = new QueryWrapper<>();
        if (currentProviderId != null) {
            // 只搜索当前用户的工具
            queryWrapper.eq("provider_id", currentProviderId)
                .and(wrapper -> wrapper
                    .like("tool_name", keyword)
                    .or()
                    .like("tool_description", keyword)
                );
        } else {
            // 用户未登录，返回空结果
            queryWrapper.eq("1", "0");
        }

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