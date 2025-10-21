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
 * 提供者实体类（系统登录用户）
 * </p>
 *
 * @author system
 * @date Created in 2024-01-01
 */
@Data
@TableName("provider")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Provider extends Model<Provider> {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 密码盐
     */
    private String salt;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phoneNumber;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 联系人姓名
     */
    private String contactPerson;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API密钥
     */
    private String apiSecret;

    /**
     * 状态：-1-删除，0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

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