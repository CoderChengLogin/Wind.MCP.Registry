package cn.com.wind.mcp.registry.controller;

import cn.com.wind.mcp.registry.entity.Provider;
import cn.com.wind.mcp.registry.service.ProviderService;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 提供者控制器
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Slf4j
@Controller
@RequestMapping("/provider")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage() {
        return "provider/register";
    }

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage() {
        return "provider/login";
    }

    /**
     * 个人中心页面
     */
    @GetMapping("/profile")
    public String profilePage(HttpSession session) {
        Provider provider = (Provider) session.getAttribute("currentProvider");
        if (provider == null) {
            return "redirect:/provider/login";
        }
        return "provider/profile";
    }

    /**
     * 提供者注册API
     */
    @PostMapping("/register")
    @ResponseBody
    public Map<String, Object> register(@RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam String confirmPassword,
                                        @RequestParam(required = false) String email,
                                        @RequestParam(required = false) String phoneNumber,
                                        @RequestParam(required = false) String companyName) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 基本验证
            if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
                result.put("success", false);
                result.put("message", "用户名和密码不能为空");
                return result;
            }

            if (!password.equals(confirmPassword)) {
                result.put("success", false);
                result.put("message", "两次输入的密码不一致");
                return result;
            }

            if (username.length() < 3 || username.length() > 20) {
                result.put("success", false);
                result.put("message", "用户名长度应在3-20个字符之间");
                return result;
            }

            if (password.length() < 6) {
                result.put("success", false);
                result.put("message", "密码长度不能少于6个字符");
                return result;
            }

            // 调用服务层注册
            boolean success = providerService.register(username, password, email, phoneNumber);
            if (success) {
                result.put("success", true);
                result.put("message", "注册成功");
            } else {
                result.put("success", false);
                result.put("message", "用户名或邮箱已存在");
            }
        } catch (Exception e) {
            log.error("提供者注册失败", e);
            result.put("success", false);
            result.put("message", "注册失败，请稍后重试");
        }

        return result;
    }

    /**
     * 提供者登录API
     */
    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> login(@RequestParam String username,
                                     @RequestParam String password,
                                     HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 基本验证
            if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
                result.put("success", false);
                result.put("message", "用户名和密码不能为空");
                return result;
            }

            // 调用服务层登录
            Provider provider = providerService.login(username, password);
            if (provider != null) {
                // 登录成功，保存到session
                session.setAttribute("currentProvider", provider);
                result.put("success", true);
                result.put("message", "登录成功");

                Map<String, Object> providerInfo = new HashMap<>();
                providerInfo.put("id", provider.getId());
                providerInfo.put("username", provider.getUsername());
                providerInfo.put("email", provider.getEmail());
                providerInfo.put("companyName", provider.getCompanyName());
                result.put("provider", providerInfo);
            } else {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
            }
        } catch (Exception e) {
            log.error("提供者登录失败", e);
            result.put("success", false);
            result.put("message", "登录失败，请稍后重试");
        }

        return result;
    }

    /**
     * 获取当前提供者信息API
     */
    @GetMapping("/current")
    @ResponseBody
    public Map<String, Object> getCurrentProvider(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Provider provider = (Provider) session.getAttribute("currentProvider");
        if (provider != null) {
            result.put("success", true);

            Map<String, Object> providerInfo = new HashMap<>();
            providerInfo.put("id", provider.getId());
            providerInfo.put("username", provider.getUsername());
            providerInfo.put("email", provider.getEmail());
            providerInfo.put("phoneNumber", provider.getPhoneNumber());
            providerInfo.put("companyName", provider.getCompanyName());
            providerInfo.put("contactPerson", provider.getContactPerson());
            providerInfo.put("apiKey", provider.getApiKey());
            result.put("provider", providerInfo);
        } else {
            result.put("success", false);
            result.put("message", "用户未登录");
        }

        return result;
    }

    /**
     * 提供者退出登录API
     */
    @PostMapping("/logout")
    @ResponseBody
    public Map<String, Object> logout(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            session.removeAttribute("currentProvider");
            session.invalidate();
            result.put("success", true);
            result.put("message", "退出登录成功");
        } catch (Exception e) {
            log.error("退出登录失败", e);
            result.put("success", false);
            result.put("message", "退出登录失败");
        }

        return result;
    }

    /**
     * 更新个人资料API
     */
    @PostMapping("/updateProfile")
    @ResponseBody
    public Map<String, Object> updateProfile(@RequestParam(required = false) String email,
                                             @RequestParam(required = false) String phoneNumber,
                                             @RequestParam(required = false) String companyName,
                                             @RequestParam(required = false) String contactPerson,
                                             HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Provider currentProvider = (Provider) session.getAttribute("currentProvider");
            if (currentProvider == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            // 更新用户信息
            boolean success = providerService.updateProfile(currentProvider.getId(), email, phoneNumber, companyName,
                    contactPerson);
            if (success) {
                // 更新session中的用户信息
                Provider updatedProvider = providerService.getById(currentProvider.getId());
                session.setAttribute("currentProvider", updatedProvider);

                result.put("success", true);
                result.put("message", "个人信息更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新个人资料失败", e);
            result.put("success", false);
            result.put("message", "更新失败，请稍后重试");
        }

        return result;
    }

    /**
     * 重新生成API密钥API
     */
    @PostMapping("/regenerateApiKey")
    @ResponseBody
    public Map<String, Object> regenerateApiKey(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Provider currentProvider = (Provider) session.getAttribute("currentProvider");
            if (currentProvider == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            String newApiKey = providerService.regenerateApiKey(currentProvider.getId());
            if (StrUtil.isNotBlank(newApiKey)) {
                // 更新session中的用户信息
                Provider updatedProvider = providerService.getById(currentProvider.getId());
                session.setAttribute("currentProvider", updatedProvider);

                result.put("success", true);
                result.put("message", "API密钥重新生成成功");
                result.put("apiKey", newApiKey);
            } else {
                result.put("success", false);
                result.put("message", "生成失败");
            }
        } catch (Exception e) {
            log.error("重新生成API密钥失败", e);
            result.put("success", false);
            result.put("message", "生成失败，请稍后重试");
        }

        return result;
    }
}