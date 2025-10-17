-- ============================================================================
-- V9: 重构mcp_tool表的convert_type和tool_type字段为CHAR类型
--
-- 变更说明：
-- 1. convert_type: varchar(50) -> CHAR(1)
--    - 1: http
--    - 2: expo
--    - 3: code
-- 2. tool_type: varchar(20) -> CHAR(1)
--    - 1: tool
--    - 2: agent
--
-- 作者: Claude
-- 创建时间: 2025-10-17
-- ============================================================================

-- 步骤1: 添加新的临时字段
ALTER TABLE mcp_tool
ADD COLUMN convert_type_new CHAR(1) COMMENT '三种方式: 1 http, 2 expo, 3 code',
ADD COLUMN tool_type_new CHAR(1) COMMENT '1: tool, 2: agent';

-- 步骤2: 数据迁移 - convert_type字段
-- 将字符串值转换为数字编码
UPDATE mcp_tool
SET convert_type_new = CASE
    WHEN LOWER(convert_type) LIKE '%http%' THEN '1'
    WHEN LOWER(convert_type) = 'expo' THEN '2'
    WHEN LOWER(convert_type) = 'code' OR LOWER(convert_type) = 'manual' THEN '3'
    ELSE '1'  -- 默认为http
END
WHERE convert_type IS NOT NULL;

-- 步骤3: 数据迁移 - tool_type字段
-- 将字符串值转换为数字编码
UPDATE mcp_tool
SET tool_type_new = CASE
    WHEN tool_type = '1' OR LOWER(tool_type) = 'tool' THEN '1'
    WHEN tool_type = '2' OR LOWER(tool_type) = 'agent' THEN '2'
    ELSE '1'  -- 默认为tool
END
WHERE tool_type IS NOT NULL;

-- 步骤4: 删除旧字段
ALTER TABLE mcp_tool
DROP COLUMN convert_type,
DROP COLUMN tool_type;

-- 步骤5: 重命名新字段为原字段名
ALTER TABLE mcp_tool
CHANGE COLUMN convert_type_new convert_type CHAR(1) COMMENT '三种方式: 1 http, 2 expo, 3 code',
CHANGE COLUMN tool_type_new tool_type CHAR(1) COMMENT '1: tool, 2: agent';

-- 步骤6: 为新字段添加索引（可选，提升查询性能）
CREATE INDEX idx_convert_type ON mcp_tool(convert_type);
CREATE INDEX idx_tool_type ON mcp_tool(tool_type);

-- 步骤7: 验证数据迁移结果（生成统计信息）
-- 这些SELECT语句会在迁移日志中显示，用于验证
SELECT
    '数据迁移完成统计' AS info,
    COUNT(*) AS total_records,
    SUM(CASE WHEN convert_type = '1' THEN 1 ELSE 0 END) AS http_count,
    SUM(CASE WHEN convert_type = '2' THEN 1 ELSE 0 END) AS expo_count,
    SUM(CASE WHEN convert_type = '3' THEN 1 ELSE 0 END) AS code_count,
    SUM(CASE WHEN tool_type = '1' THEN 1 ELSE 0 END) AS tool_count,
    SUM(CASE WHEN tool_type = '2' THEN 1 ELSE 0 END) AS agent_count
FROM mcp_tool;
