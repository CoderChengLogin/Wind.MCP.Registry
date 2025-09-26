-- 添加提供者ID字段到工具表，实现用户权限控制
-- 每个工具都应该属于创建它的用户

-- 为mcp_tool表添加provider_id字段
ALTER TABLE mcp_tool
    ADD COLUMN provider_id BIGINT NULL COMMENT '提供者ID，关联provider表' AFTER update_by,
    ADD INDEX idx_provider_id (provider_id);

-- 为origin_tool_http表添加provider_id字段
ALTER TABLE origin_tool_http
    ADD COLUMN provider_id BIGINT NULL COMMENT '提供者ID，关联provider表' AFTER update_by,
    ADD INDEX idx_http_provider_id (provider_id);

-- 为origin_tool_expo表添加provider_id字段
ALTER TABLE origin_tool_expo
    ADD COLUMN provider_id BIGINT NULL COMMENT '提供者ID，关联provider表' AFTER update_by,
    ADD INDEX idx_expo_provider_id (provider_id);

-- 为转换器表添加provider_id字段
ALTER TABLE http_template_converter
    ADD COLUMN provider_id BIGINT NULL COMMENT '提供者ID，关联provider表' AFTER update_by,
    ADD INDEX idx_http_converter_provider_id (provider_id);

ALTER TABLE expo_template_converter
    ADD COLUMN provider_id BIGINT NULL COMMENT '提供者ID，关联provider表' AFTER update_by,
    ADD INDEX idx_expo_converter_provider_id (provider_id);