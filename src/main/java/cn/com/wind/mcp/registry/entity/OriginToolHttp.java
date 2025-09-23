package cn.com.wind.mcp.registry.entity;

import java.io.Serializable;
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
 * 原始HTTP工具实体类
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Data
@TableName("origin_tool_http")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class OriginToolHttp extends Model<OriginToolHttp> {

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
     * 提供者工具编号
     */
    @TableField(exist = false)
    private Long providerToolNum;

    /**
     * 名称显示（开发者填写）
     */
    private String nameDisplay;

    /**
     * 功能描述
     */
    private String descDisplay;

    /**
     * 请求URL
     */
    private String reqUrl;

    /**
     * 请求方法
     */
    private String reqMethod;

    /**
     * 请求头
     */
    private String reqHeaders;

    /**
     * 输入Schema（遵循JSON Schema规范）
     */
    private String inputSchema;

    /**
     * 输出Schema（遵循JSON Schema规范）
     */
    private String outputSchema;

    /**
     * 提供者应用编号
     */
    @TableField(exist = false)
    private Long providerAppNum;

    /**
     * 提供者ID
     */
    private Long providerId;

    /**
     * 工具名称（用于验证，映射到nameDisplay）
     */
    public String getName() {
        return this.nameDisplay;
    }

    public void setName(String name) {
        this.nameDisplay = name;
    }

    /**
     * 工具类型（HTTP工具固定为"http"）
     */
    public String getType() {
        return "http";
    }

    public void setType(String type) {
        // HTTP工具类型固定，忽略设置
    }

    /**
     * 工具描述（映射到descDisplay）
     */
    public String getDescription() {
        return this.descDisplay;
    }

    public void setDescription(String description) {
        this.descDisplay = description;
    }

    /**
     * HTTP请求方法（映射到reqMethod）
     */
    public String getMethod() {
        return this.reqMethod;
    }

    public void setMethod(String method) {
        this.reqMethod = method;
    }

    /**
     * 请求URL（映射到reqUrl）
     */
    public String getUrl() {
        return this.reqUrl;
    }

    public void setUrl(String url) {
        this.reqUrl = url;
    }

    /**
     * 主键值，ActiveRecord 模式这个必须有
     *
     * @return 主键值
     */
    @Override
    public Serializable pkVal() {
        return this.id;
    }
}