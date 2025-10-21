package cn.com.wind.mcp.registry.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 虚拟服务器项目实体类
 *
 * @author system
 * @date 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("vserver_items")
public class VserverItems extends Model<VserverItems> {

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
     * MCP项目编号
     */
    private Long mcpItemNum;

    /**
     * MCP项目类型(1:tool,2:agent)
     */
    private String mcpItemType;

    /**
     * 状态(1:启用,0:禁用)
     */
    private String status;

    /**
     * 排序号
     */
    private Integer orderNum;
}
