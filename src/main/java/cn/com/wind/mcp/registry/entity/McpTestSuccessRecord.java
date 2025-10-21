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
 * MCP工具测试成功记录实体类
 * 用于存储测试成功的完整快照信息,支持数据回溯和审计
 *
 * @author system
 * @date 2025-10-20
 */
@Data
@TableName("mcp_test_success_records")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class McpTestSuccessRecord extends Model<McpTestSuccessRecord> {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * MCP工具ID
     */
    private Long toolId;

    /**
     * 工具编号
     */
    private String toolNum;

    /**
     * MCP工具名称
     */
    private String toolName;

    /**
     * 工具版本
     */
    private String toolVersion;

    /**
     * MCP工具完整信息快照(JSON格式)
     * 包含工具配置、转换模板等完整信息,用于数据回溯
     */
    private String toolSnapshot;

    /**
     * 测试参数详情(JSON格式)
     */
    private String testParameters;

    /**
     * 测试结果详细数据(JSON格式)
     */
    private String testResult;

    /**
     * 测试结果摘要
     */
    private String testResultSummary;

    /**
     * 测试时间戳(毫秒精度)
     */
    private LocalDateTime testTimestamp;

    /**
     * 操作者ID(关联provider表)
     */
    private Long operatorId;

    /**
     * 操作者用户名
     */
    private String operatorUsername;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 记录创建者
     */
    private String createBy;
}