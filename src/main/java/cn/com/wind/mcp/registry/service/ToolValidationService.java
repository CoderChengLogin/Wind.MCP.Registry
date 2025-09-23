package cn.com.wind.mcp.registry.service;

import cn.com.wind.mcp.registry.entity.McpTool;
import cn.com.wind.mcp.registry.entity.OriginToolHttp;

/**
 * <p>
 * 工具验证服务接口
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
public interface ToolValidationService {

    /**
     * 验证MCP工具信息
     *
     * @param tool MCP工具
     * @return 验证结果
     */
    ValidationResult validateMcpTool(McpTool tool);

    /**
     * 验证HTTP工具信息
     *
     * @param tool HTTP工具
     * @return 验证结果
     */
    ValidationResult validateHttpTool(OriginToolHttp tool);

    /**
     * 生成工具唯一标识
     *
     * @param toolName 工具名称
     * @param version  版本
     * @param toolType 工具类型
     * @return 唯一标识
     */
    String generateUniqueIdentifier(String toolName, String version, String toolType);

    /**
     * 验证工具名称规范
     *
     * @param name 工具名称
     * @return 验证结果
     */
    boolean validateToolName(String name);

    /**
     * 验证版本格式
     *
     * @param version 版本号
     * @return 验证结果
     */
    boolean validateVersion(String version);

    /**
     * 验证结果类
     */
    class ValidationResult {
        private boolean valid;
        private String message;
        private String uniqueId;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public ValidationResult(boolean valid, String message, String uniqueId) {
            this.valid = valid;
            this.message = message;
            this.uniqueId = uniqueId;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }
    }
}