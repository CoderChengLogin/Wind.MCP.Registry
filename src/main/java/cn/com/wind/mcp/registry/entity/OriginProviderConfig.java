package cn.com.wind.mcp.registry.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提供商应用配置实体类
 * <p>
 * 统一管理应用服务节点和配置信息，替代原有的ProviderApp表
 * </p>
 *
 * @author Claude Code
 * @date 2025-10-11
 */
@Data
@TableName("origin_provider_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class OriginProviderConfig extends Model<OriginProviderConfig> {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 应用编号 - 关联origin_tool_http和origin_tool_expo的provider_app_num
     */
    private Long appNum;

    /**
     * 提供商ID - 关联provider表
     */
    private Long providerId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 站点类型（如：测试站、河西、外高桥等）
     */
    private String siteType;

    /**
     * 应用IP地址
     */
    private String appIp;

    /**
     * 应用端口
     */
    private Integer appPort;

    /**
     * 负载因子（权重）
     */
    private Integer loadFactor;

    /**
     * 请求超时时间（秒）
     */
    private Integer requestTimeout;

    /**
     * 最大失败次数
     */
    private Integer maxFailCount;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Boolean isEnabled;

    /**
     * 应用节点描述
     */
    private String appDescription;

    /**
     * 健康检查地址
     */
    private String healthCheckUrl;

    /**
     * 健康检查间隔（毫秒）
     */
    private Integer healthCheckInterval;

    /**
     * 状态：-1-删除，0-禁用，1-启用
     */
    private Integer status;

    /**
     * 环境标识（如：dev, test, prod等）
     */
    private String env;

    /**
     * 配置key
     */
    private String configKey;

    /**
     * 配置value
     */
    private String configValue;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

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
