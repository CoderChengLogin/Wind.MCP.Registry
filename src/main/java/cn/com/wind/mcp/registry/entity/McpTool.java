package cn.com.wind.mcp.registry.entity;

import java.time.LocalDateTime;

import cn.com.wind.mcp.registry.handler.ConvertTypeHandler;
import cn.com.wind.mcp.registry.handler.ToolTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
     * 使用ConvertTypeHandler进行自动转换
     */
    @TableField(typeHandler = ConvertTypeHandler.class)
    private String convertType;

    /**
     * 1: tool, 2: agent
     * 数据库存储: '1', '2'
     * 业务逻辑使用: "1", "2"
     * 使用ToolTypeHandler进行自动转换
     */
    @TableField(typeHandler = ToolTypeHandler.class)
    private String toolType;
}
