
package cn.com.wind.mcp.registry.entity;

import java.io.Serializable;
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
 * 原始Expo工具实体类
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Data
@TableName("origin_tool_expo")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class OriginToolExpo extends Model<OriginToolExpo> {

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
     * 提供者工具编号
     */
    private Long providerToolNum;

    /**
     * 提供者工具名称
     */
    private String providerToolName;

    /**
     * 名称
     */
    private String nameDisplay;

    /**
     * 功能描述
     */
    private String descDisplay;

    /**
     * 应用类别
     */
    private Integer appClass;

    /**
     * 命令ID
     */
    private Integer commandId;

    /**
     * 函数名称
     */
    private String functionName;

    /**
     * API描述，xml
     */
    private String expoApiDefine;

    /**
     * 提供者app编号
     */
    private Long providerAppNum;

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

