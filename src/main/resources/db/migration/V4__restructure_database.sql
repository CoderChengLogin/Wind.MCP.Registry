-- 数据库结构重构：删除冗余表，创建provider表
-- V4__restructure_database.sql

-- 1. 删除冗余表
DROP TABLE IF EXISTS tool_usage_stats;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS orm_user_role;
DROP TABLE IF EXISTS orm_role;
DROP TABLE IF EXISTS orm_user;

-- 2. 创建provider表作为系统登录用户表
CREATE TABLE provider
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username         VARCHAR(50)  NOT NULL COMMENT '用户名',
    password         VARCHAR(128) NOT NULL COMMENT '加密后的密码',
    salt             VARCHAR(50)  NOT NULL COMMENT '密码盐',
    email            VARCHAR(100) COMMENT '邮箱',
    phone_number     VARCHAR(20) COMMENT '手机号码',
    company_name     VARCHAR(100) COMMENT '公司名称',
    contact_person   VARCHAR(50) COMMENT '联系人姓名',
    api_key          VARCHAR(128) COMMENT 'API密钥',
    api_secret       VARCHAR(128) COMMENT 'API密钥',
    status           INT       DEFAULT 1 COMMENT '状态：-1-删除，0-禁用，1-启用',
    create_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_login_time  TIMESTAMP    NULL COMMENT '最后登录时间',
    last_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    -- 索引
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_api_key (api_key),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='提供者表（系统登录用户）';

-- 3. 修改origin_tool_http表结构，添加provider_id字段
ALTER TABLE origin_tool_http
    ADD COLUMN provider_id BIGINT COMMENT '提供者ID';
ALTER TABLE origin_tool_http
    ADD INDEX idx_provider_id (provider_id);

-- 4. 修改origin_tool_expo表结构，添加provider_id字段
ALTER TABLE origin_tool_expo
    ADD COLUMN provider_id BIGINT COMMENT '提供者ID';
ALTER TABLE origin_tool_expo
    ADD INDEX idx_provider_id (provider_id);

-- 5. 修改mcp_tool表结构，添加provider_id字段和origin_tool_id字段
ALTER TABLE mcp_tool
    ADD COLUMN provider_id BIGINT COMMENT '提供者ID';
ALTER TABLE mcp_tool
    ADD COLUMN origin_tool_id BIGINT COMMENT '原始工具ID';
ALTER TABLE mcp_tool
    ADD COLUMN origin_tool_type VARCHAR(20) COMMENT '原始工具类型：http, expo';
ALTER TABLE mcp_tool
    ADD INDEX idx_provider_id (provider_id);
ALTER TABLE mcp_tool
    ADD INDEX idx_origin_tool (origin_tool_type, origin_tool_id);

-- 6. 修改http_template_converter表，确保与新结构兼容
ALTER TABLE http_template_converter
    ADD COLUMN mcp_tool_id BIGINT COMMENT 'MCP工具ID';
ALTER TABLE http_template_converter
    ADD INDEX idx_mcp_tool_id (mcp_tool_id);

-- 7. 修改expo_template_converter表，确保与新结构兼容
ALTER TABLE expo_template_converter
    ADD COLUMN mcp_tool_id BIGINT COMMENT 'MCP工具ID';
ALTER TABLE expo_template_converter
    ADD INDEX idx_mcp_tool_id (mcp_tool_id);

-- 8. 插入默认的provider用户
INSERT INTO provider (username, password, salt, email, company_name, contact_person, api_key, api_secret, status)
VALUES ('admin', '21232f297a57a5a743894a0e4a801fc3', 'default_salt', 'admin@mcp-registry.com', 'MCP Registry',
        'Administrator',
        'mcp_registry_api_key_001', 'mcp_registry_api_secret_001', 1),
       ('demo_provider', 'fe01ce2a7fbac8fafaed7c982a04e229', 'demo_salt', 'demo@example.com', 'Demo Company',
        'Demo User',
        'demo_api_key_001', 'demo_api_secret_001', 1);