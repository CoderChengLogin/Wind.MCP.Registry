package cn.com.wind.mcp.registry.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 虚拟服务器
 *
 * @TableName virtual_server
 */
@Data
@TableName(value = "virtual_server")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class VirtualServer extends Model<VirtualServer> {
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
     * 用户id
     */
    private String userid;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    @TableField("`desc`")
    private String desc;

    /**
     * url
     */
    private String url;

    /**
     * 状态: 1 已发布
     */
    private String status;
}



