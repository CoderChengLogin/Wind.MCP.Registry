package cn.com.wind.mcp.registry.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
     * 工具名称
     */
    @TableField("name")
    private String name;

    /**
     * 工具类型
     */
    @TableField("type")
    private String type;

    /**
     * 工具描述
     */
    @TableField("description")
    private String description;

    /**
     * 版本号
     */
    @TableField("version")
    private String version;

    /**
     * 作者
     */
    @TableField("author")
    private String author;

    /**
     * 主页地址
     */
    @TableField("homepage")
    private String homepage;

    /**
     * 仓库地址
     */
    @TableField("repository")
    private String repository;

    /**
     * 文档内容
     */
    @TableField("documentation")
    private String documentation;

    /**
     * 配置信息(JSON格式)
     */
    @TableField("configuration")
    private String configuration;

    /**
     * 标签，逗号分隔
     */
    @TableField("tags")
    private String tags;

    /**
     * 状态：active-活跃, inactive-非活跃, deprecated-已弃用
     */
    @TableField("status")
    private String status;

    /**
     * 安装次数
     */
    @TableField("install_count")
    private Integer installCount;

    /**
     * 评分(0-5)
     */
    @TableField("rating")
    private BigDecimal rating;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    @TableField("created_by")
    private String createBy;

    /**
     * 更新者
     */
    @TableField("updated_by")
    private String updateBy;

    /**
     * 提供者ID
     */
    @TableField("provider_id")
    private Long providerId;

    /**
     * 原始工具ID
     */
    @TableField("origin_tool_id")
    private Long originToolId;

    /**
     * 原始工具类型：http, expo
     */
    @TableField("origin_tool_type")
    private String originToolType;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}