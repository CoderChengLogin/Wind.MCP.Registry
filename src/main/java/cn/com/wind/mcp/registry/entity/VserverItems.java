package cn.com.wind.mcp.registry.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * 虚拟服务器关联条目
 *
 * @TableName vserver_items
 */
@TableName(value = "vserver_items")
@Data
public class VserverItems extends Model<VserverItems> {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 虚拟服务器id
     */
    private String vserverId;

    /**
     * mcp项目编号
     */
    private Long mcpItemNum;

    /**
     * MCP类型(1:tool, 2:prompt, 3:resource)
     */
    private String mcpItemType;

    /**
     * 状态: 1 已发布
     */
    private String status;

    /**
     * 排序
     */
    private Integer orderNum;
}