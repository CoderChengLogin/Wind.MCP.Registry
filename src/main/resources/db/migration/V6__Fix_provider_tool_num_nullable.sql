-- 修改origin_tool_http表的provider_tool_num字段为可为空
-- MySQL的唯一约束允许多个NULL值，所以不需要移除约束
ALTER TABLE origin_tool_http MODIFY COLUMN provider_tool_num bigint NULL COMMENT '提供者工具编号';

-- 修改origin_tool_expo表的provider_tool_num字段为可为空(保持一致性)
ALTER TABLE origin_tool_expo MODIFY COLUMN provider_tool_num bigint NULL COMMENT '提供者工具编号';
