-- V8__Refactor_provider_app_to_origin_provider_config.sql
-- 重构应用服务节点管理，将provider_app迁移到origin_provider_config
-- 作者: Claude Code
-- 日期: 2025-10-11

-- ============================================
-- 步骤1: 给provider_app表添加app_num字段 (如果不存在)
-- ============================================
-- 检查并添加app_num字段 (幂等性处理)
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'mcp_registry'
    AND TABLE_NAME = 'provider_app'
    AND COLUMN_NAME = 'app_num'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE provider_app ADD COLUMN app_num BIGINT NULL COMMENT ''应用编号'' AFTER id',
    'SELECT ''Column app_num already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为现有记录生成app_num (基于id生成唯一编号)
UPDATE provider_app SET app_num = id + 1000000 WHERE app_num IS NULL;

-- ============================================
-- 步骤2: 重构origin_provider_config表结构
-- ============================================
-- 删除旧的唯一索引 (MySQL 5.7兼容语法)
ALTER TABLE origin_provider_config
DROP INDEX uk_app_num_env_config_key;

-- 清空origin_provider_config表（如果有数据）
TRUNCATE TABLE origin_provider_config;

-- 重新设计origin_provider_config表结构，使其包含provider_app的所有字段
ALTER TABLE origin_provider_config
ADD COLUMN provider_id BIGINT NULL COMMENT '提供商ID' AFTER id,
ADD COLUMN app_name VARCHAR(100) NULL COMMENT '应用名称' AFTER app_num,
ADD COLUMN site_type VARCHAR(50) NULL COMMENT '站点类型' AFTER app_name,
ADD COLUMN app_ip VARCHAR(50) NULL COMMENT '应用IP地址' AFTER site_type,
ADD COLUMN app_port INT NULL COMMENT '应用端口' AFTER app_ip,
ADD COLUMN load_factor INT DEFAULT 1 COMMENT '负载权重因子' AFTER app_port,
ADD COLUMN request_timeout INT DEFAULT 60 COMMENT '请求超时时间(秒)' AFTER load_factor,
ADD COLUMN max_fail_count INT DEFAULT 3 COMMENT '最大失败次数' AFTER request_timeout,
ADD COLUMN is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用' AFTER max_fail_count,
ADD COLUMN app_description VARCHAR(500) NULL COMMENT '应用描述信息' AFTER is_enabled,
ADD COLUMN health_check_url VARCHAR(200) NULL COMMENT '健康检查URL' AFTER app_description,
ADD COLUMN health_check_interval INT DEFAULT 5000 COMMENT '健康检查间隔(毫秒)' AFTER health_check_url,
ADD COLUMN status INT DEFAULT 1 COMMENT '状态：-1-删除，0-禁用，1-启用' AFTER health_check_interval,
MODIFY COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
MODIFY COLUMN create_by VARCHAR(128) NOT NULL DEFAULT 'system' COMMENT '创建人',
MODIFY COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
MODIFY COLUMN update_by VARCHAR(128) NOT NULL DEFAULT 'system' COMMENT '更新人',
MODIFY COLUMN env VARCHAR(50) NULL COMMENT '环境标识' AFTER status,
MODIFY COLUMN config_key VARCHAR(100) NULL COMMENT '配置key' AFTER env,
MODIFY COLUMN config_value VARCHAR(1000) NULL COMMENT '配置value' AFTER config_key;

-- ============================================
-- 步骤3: 迁移provider_app数据到origin_provider_config
-- ============================================
INSERT INTO origin_provider_config (
    app_num,
    provider_id,
    app_name,
    site_type,
    app_ip,
    app_port,
    load_factor,
    request_timeout,
    max_fail_count,
    is_enabled,
    app_description,
    health_check_url,
    health_check_interval,
    status,
    create_time,
    create_by,
    update_time,
    update_by
)
SELECT
    app_num,
    provider_id,
    app_name,
    site_type,
    app_ip,
    app_port,
    load_factor,
    request_timeout,
    max_fail_count,
    is_enabled,
    app_description,
    health_check_url,
    health_check_interval,
    status,
    create_time,
    create_by,
    last_update_time,
    update_by
FROM provider_app;

-- ============================================
-- 步骤4: 更新origin_tool_http和origin_tool_expo的关联关系
-- ============================================
-- 更新origin_tool_http表，将provider_app_num关联到新的app_num
-- 由于原先的provider_app_num可能为空，我们需要确保只更新有效记录
UPDATE origin_tool_http oth
INNER JOIN provider_app pa ON oth.provider_app_num = pa.id
SET oth.provider_app_num = pa.app_num
WHERE oth.provider_app_num IS NOT NULL;

-- 更新origin_tool_expo表，将provider_app_num关联到新的app_num
UPDATE origin_tool_expo ote
INNER JOIN provider_app pa ON ote.provider_app_num = pa.id
SET ote.provider_app_num = pa.app_num
WHERE ote.provider_app_num IS NOT NULL;

-- ============================================
-- 步骤5: 添加索引和约束
-- ============================================
-- 为origin_provider_config添加索引
ALTER TABLE origin_provider_config
ADD INDEX idx_origin_provider_config_app_num (app_num),
ADD INDEX idx_origin_provider_config_provider_id (provider_id),
ADD INDEX idx_origin_provider_config_status (status),
ADD INDEX idx_origin_provider_config_is_enabled (is_enabled);

-- 为origin_tool_http的provider_app_num添加索引
ALTER TABLE origin_tool_http
ADD INDEX idx_http_provider_app_num (provider_app_num);

-- 为origin_tool_expo的provider_app_num添加索引
ALTER TABLE origin_tool_expo
ADD INDEX idx_expo_provider_app_num (provider_app_num);

-- ============================================
-- 步骤6: 删除废弃的表
-- ============================================
-- 删除provider_app表（数据已迁移）
DROP TABLE IF EXISTS provider_app;

-- 删除virtual_server表（不再使用）
DROP TABLE IF EXISTS virtual_server;

-- 删除vserver_items表（不再使用）
DROP TABLE IF EXISTS vserver_items;

-- ============================================
-- 步骤7: 更新表注释
-- ============================================
ALTER TABLE origin_provider_config
COMMENT = '提供商应用配置表 - 统一管理应用服务节点和配置信息';
