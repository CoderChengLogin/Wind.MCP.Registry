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
 * <p>
 * Expo模板转换器实体类
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Data
@TableName("expo_template_converter")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ExpoTemplateConverter extends Model<ExpoTemplateConverter> {

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
     * 工具编号 (MCP工具ID)
     */
    private Long toolNum;

    /**
     * 工具版本
     */
    private Long toolVersion;

    /**
     * expo app class
     */
    private Integer appClass;

    /**
     * expo command id
     */
    private Integer commandId;

    /**
     * 输入参数 jinja2 模板
     */
    private String inputArgs;

    /**
     * 输出参数 jinja2 模板
     */
    private String outputArgs;

    /**
     * 提供者工具编号
     */
    private Long providerToolNum;

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

