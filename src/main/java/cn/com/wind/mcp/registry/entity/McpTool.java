
package cn.com.wind.mcp.registry.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * MCP工具实体类
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Data
@TableName("mcp_tool")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class McpTool extends Model<McpTool> {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 提供者ID
     */
    private Long providerId;

    /**
     * 工具编号
     */
    private Long toolNum;

    /**
     * 工具版本
     */
    private Long toolVersion;

    /**
     * 1: 有效, 0: 无效
     */
    private String valid;

    /**
     * 工具英文名
     */
    private String toolName;

    /**
     * 工具描述
     */
    private String toolDescription;

    /**
     * 显示用名字，多语言
     */
    private String nameDisplay;

    /**
     * 显示用描述，多语言
     */
    private String descriptionDisplay;

    /**
     * mcp工具，遵足json schema规范
     */
    private String inputSchema;

    /**
     * 输出schema
     */
    private String outputSchema;

    /**
     * 0:非流, 1:流式
     */
    private String streamOutput;

    /**
     * 三种方式: 1 http, 2 expo, 3 code
     * 数据库存储: '1', '2', '3'
     * 业务逻辑使用: "http", "expo", "code"
     */
    private String convertType;

    /**
     * 1: tool, 2: agent
     * 数据库存储: '1', '2'
     * 业务逻辑使用: "1", "2" (已经是数字字符串，保持兼容)
     */
    private String toolType;

    /**
     * 自定义convertType的getter方法
     * 将数据库中的数字编码转换为业务逻辑使用的字符串
     *
     * @return 转换后的convertType值: "http", "expo", "code"
     */
    public String getConvertType() {
        if (convertType == null) {
            return null;
        }

        switch (convertType) {
            case "1":
                return "http";
            case "2":
                return "expo";
            case "3":
                return "code";
            default:
                // 兼容旧数据：如果已经是字符串形式，直接返回
                return convertType;
        }
    }

    /**
     * 自定义convertType的setter方法
     * 将业务逻辑使用的字符串转换为数据库存储的数字编码
     *
     * @param convertType 业务逻辑值: "http", "expo", "code", "manual" 或 "1", "2", "3"
     */
    public void setConvertType(String convertType) {
        if (convertType == null) {
            this.convertType = null;
            return;
        }

        String lowerValue = convertType.toLowerCase().trim();

        // 转换为数字编码
        if (lowerValue.contains("http") || lowerValue.equals("1")) {
            this.convertType = "1";
        } else if (lowerValue.equals("expo") || lowerValue.equals("2")) {
            this.convertType = "2";
        } else if (lowerValue.equals("code") || lowerValue.equals("manual") || lowerValue.equals("3")) {
            this.convertType = "3";
        } else {
            // 默认为http
            this.convertType = "1";
        }
    }

    /**
     * 自定义toolType的getter方法
     * 将数据库中的数字编码转换为业务逻辑使用的字符串
     *
     * @return 转换后的toolType值: "1"(tool), "2"(agent)
     */
    public String getToolType() {
        if (toolType == null) {
            return null;
        }

        switch (toolType) {
            case "1":
                return "1";  // tool
            case "2":
                return "2";  // agent
            default:
                // 兼容旧数据：如果是"tool"或"agent"字符串，转换为数字
                if ("tool".equalsIgnoreCase(toolType)) {
                    return "1";
                } else if ("agent".equalsIgnoreCase(toolType)) {
                    return "2";
                }
                return toolType;
        }
    }

    /**
     * 自定义toolType的setter方法
     * 将业务逻辑使用的字符串转换为数据库存储的数字编码
     *
     * @param toolType 业务逻辑值: "1", "2", "tool", "agent"
     */
    public void setToolType(String toolType) {
        if (toolType == null) {
            this.toolType = null;
            return;
        }

        String lowerValue = toolType.toLowerCase().trim();

        // 转换为数字编码
        if (lowerValue.equals("1") || lowerValue.equals("tool")) {
            this.toolType = "1";
        } else if (lowerValue.equals("2") || lowerValue.equals("agent")) {
            this.toolType = "2";
        } else {
            // 默认为tool
            this.toolType = "1";
        }
    }
}
