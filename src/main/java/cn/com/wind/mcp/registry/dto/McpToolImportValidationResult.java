package cn.com.wind.mcp.registry.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP工具导入校验结果DTO
 * 用于返回导入数据的校验结果
 *
 * @author system
 * @date 2025-10-21
 */
@Data
public class McpToolImportValidationResult {

    /**
     * 是否校验通过
     */
    private boolean valid;

    /**
     * 校验失败的错误信息列表
     */
    private List<String> errors = new ArrayList<>();

    /**
     * 警告信息列表(不影响导入,但需要用户注意)
     */
    private List<String> warnings = new ArrayList<>();

    /**
     * 创建成功的校验结果
     *
     * @return 校验结果
     */
    public static McpToolImportValidationResult success() {
        McpToolImportValidationResult result = new McpToolImportValidationResult();
        result.setValid(true);
        return result;
    }

    /**
     * 创建失败的校验结果
     *
     * @param error 错误信息
     * @return 校验结果
     */
    public static McpToolImportValidationResult failure(String error) {
        McpToolImportValidationResult result = new McpToolImportValidationResult();
        result.addError(error);
        return result;
    }

    /**
     * 添加错误信息
     *
     * @param error 错误信息
     */
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }

    /**
     * 添加警告信息
     *
     * @param warning 警告信息
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    /**
     * 检查是否有任何错误
     *
     * @return 是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 检查是否有任何警告
     *
     * @return 是否有警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}
