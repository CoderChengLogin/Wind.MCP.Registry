-- 创建MCP测试成功记录表
-- 用于存储测试成功的完整快照信息，支持数据回溯

CREATE TABLE mcp_test_success_records (
    -- 主键
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',

    -- MCP工具信息
    tool_id BIGINT NOT NULL COMMENT 'MCP工具ID',
    tool_num VARCHAR(50) NOT NULL COMMENT '工具编号',
    tool_name VARCHAR(255) NOT NULL COMMENT 'MCP工具名称',
    tool_version VARCHAR(20) NOT NULL COMMENT '工具版本',

    -- 完整的MCP工具信息快照（JSON格式）
    tool_snapshot JSON NOT NULL COMMENT 'MCP工具完整信息快照（包含工具配置、转换模板等）',

    -- 测试参数详情（JSON格式）
    test_parameters JSON NOT NULL COMMENT '测试参数详情',

    -- 测试结果
    test_result JSON NOT NULL COMMENT '测试结果详细数据',
    test_result_summary TEXT COMMENT '测试结果摘要',

    -- 测试时间
    test_timestamp DATETIME(3) NOT NULL COMMENT '测试时间戳（毫秒精度）',

    -- 操作者信息
    operator_id BIGINT COMMENT '操作者ID（关联provider表）',
    operator_username VARCHAR(100) COMMENT '操作者用户名',

    -- 审计字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    create_by VARCHAR(100) DEFAULT 'system' COMMENT '记录创建者',

    -- 索引
    INDEX idx_tool_id (tool_id) COMMENT '工具ID索引',
    INDEX idx_tool_num (tool_num) COMMENT '工具编号索引',
    INDEX idx_test_timestamp (test_timestamp) COMMENT '测试时间索引',
    INDEX idx_operator_id (operator_id) COMMENT '操作者ID索引',
    INDEX idx_create_time (create_time) COMMENT '创建时间索引',

    -- 外键约束
    CONSTRAINT fk_test_record_tool FOREIGN KEY (tool_id) REFERENCES mcp_tool(id) ON DELETE CASCADE,
    CONSTRAINT fk_test_record_operator FOREIGN KEY (operator_id) REFERENCES provider(id) ON DELETE SET NULL

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='MCP工具测试成功记录表 - 存储测试成功的完整快照，用于数据回溯和审计';