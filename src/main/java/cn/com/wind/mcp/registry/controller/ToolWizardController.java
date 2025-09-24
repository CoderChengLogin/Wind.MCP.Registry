package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.dto.UnifiedToolAddDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 工具录入向导Controller
 * 一体化录入页面：源工具 + 工具信息 + 转换模板
 */
@Slf4j
@Controller
@RequestMapping("/tool-wizard")
public class ToolWizardController {

    /**
     * 工具录入向导首页
     */
    @GetMapping("")
    public String index(Model model) {
        log.info("访问工具录入向导");
        return "tool-wizard/index";
    }

    /**
     * 一体化工具录入页面
     */
    @GetMapping("/unified-add")
    public String unifiedAdd(Model model) {
        log.info("访问一体化工具录入页面");
        return "tool-wizard/unified-add";
    }

    /**
     * 一键保存三部分关联内容
     */
    @PostMapping("/save-unified")
    @ResponseBody
    public String saveUnified(UnifiedToolAddDto dto) {
        log.info("一键保存工具数据: {}", dto);
        // TODO: 实现保存逻辑
        return "success";
    }
}