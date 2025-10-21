package cn.com.wind.mcp.registry.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 虚拟服务器实体类
 *
 * @author system
 * @date 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("virtual_server")
public class VirtualServer extends Model<VirtualServer> {

    /**
     * 主键ID
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
     * 虚拟服务器ID
     */
    private String vserverId;

    /**
     * 用户ID
     */
    private String userid;

    /**
     * 虚拟服务器名称
     */
    private String name;

    /**
     * 描述
     */
    @TableField("`desc`")
    private String desc;

    /**
     * URL地址
     */
    private String url;

    /**
     * 状态(1:启用,0:禁用)
     */
    private String status;
}
