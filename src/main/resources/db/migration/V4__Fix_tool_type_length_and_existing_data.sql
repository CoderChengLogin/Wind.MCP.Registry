-- 修复工具类型字段长度限制，并处理现有数据兼容性

-- 修改tool_type字段长度，从char(1)改为varchar(20)
ALTER TABLE mcp_tool
    MODIFY COLUMN tool_type VARCHAR(20) COMMENT '工具类型: tool, agent, http等';

-- 为现有的没有provider_id的工具设置一个默认的管理员用户
-- 首先创建一个系统管理员用户（如果不存在）
INSERT IGNORE INTO provider (username, password, salt, email, api_key, api_secret, status, create_time,
                             last_update_time)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '123456', 'admin@system.com', 'mcp_admin_key_default',
        'admin_secret_default', 1, NOW(), NOW());

-- 获取admin用户的ID并更新所有没有provider_id的工具
UPDATE mcp_tool
SET provider_id = (SELECT id FROM provider WHERE username = 'admin' LIMIT 1)
WHERE provider_id IS NULL;
UPDATE origin_tool_http
SET provider_id = (SELECT id FROM provider WHERE username = 'admin' LIMIT 1)
WHERE provider_id IS NULL;
UPDATE origin_tool_expo
SET provider_id = (SELECT id FROM provider WHERE username = 'admin' LIMIT 1)
WHERE provider_id IS NULL;
UPDATE http_template_converter
SET provider_id = (SELECT id FROM provider WHERE username = 'admin' LIMIT 1)
WHERE provider_id IS NULL;
UPDATE expo_template_converter
SET provider_id = (SELECT id FROM provider WHERE username = 'admin' LIMIT 1)
WHERE provider_id IS NULL;