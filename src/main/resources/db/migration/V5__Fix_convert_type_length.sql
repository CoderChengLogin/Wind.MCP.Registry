-- 修复convert_type字段长度限制
-- 将char(1)改为varchar(50)以支持更灵活的转换类型定义

ALTER TABLE mcp_tool
    MODIFY COLUMN convert_type VARCHAR(50) COMMENT '转换类型，如：http_template, expo_template, code_template等';