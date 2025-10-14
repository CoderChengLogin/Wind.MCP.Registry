-- ==================================================================
-- H2数据库表结构脚本 (Jenkins测试环境)
-- 基于Flyway迁移脚本 V1-V8
-- 生成时间: 2025-10-14
-- ==================================================================

-- 1. Provider表(提供商/系统用户)
CREATE TABLE IF NOT EXISTS provider (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    salt VARCHAR(50) NOT NULL COMMENT '密码盐',
    email VARCHAR(100) COMMENT '邮箱',
    phone_number VARCHAR(20) COMMENT '手机号码',
    company_name VARCHAR(200) COMMENT '公司名称',
    contact_person VARCHAR(100) COMMENT '联系人姓名',
    api_key VARCHAR(64) COMMENT 'API密钥',
    api_secret VARCHAR(128) COMMENT 'API密钥',
    status INT DEFAULT 1 NOT NULL COMMENT '状态：-1-删除，0-禁用，1-启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    last_login_time TIMESTAMP COMMENT '最后登录时间',
    last_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '最后更新时间',
    create_by VARCHAR(50),
    update_by VARCHAR(50)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_provider_username ON provider(username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_provider_api_key ON provider(api_key);

-- 2. MCP Tool表(MCP工具)
CREATE TABLE IF NOT EXISTS mcp_tool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    create_by VARCHAR(128) DEFAULT 'system' NOT NULL COMMENT '创建人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    update_by VARCHAR(128) DEFAULT 'system' NOT NULL COMMENT '更新人',
    provider_id BIGINT COMMENT '提供者ID',
    tool_num BIGINT NOT NULL COMMENT '工具编号',
    tool_version BIGINT NOT NULL COMMENT '工具版本',
    valid CHAR(1) NOT NULL COMMENT '1: 有效, 0: 无效',
    tool_name VARCHAR(256) COMMENT '工具英文名',
    tool_description VARCHAR(2000) COMMENT '工具描述',
    name_display TEXT COMMENT '显示用名字，多语言',
    description_display TEXT COMMENT '显示用描述，多语言',
    input_schema TEXT COMMENT 'mcp工具输入schema',
    output_schema TEXT COMMENT '输出schema',
    stream_output CHAR(1) DEFAULT '0' COMMENT '0:非流, 1:流式',
    convert_type CHAR(1) COMMENT '三种方式: 1 http, 2 expo, 3 code',
    tool_type CHAR(1) COMMENT '1: tool, 2: agent'
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tool_num_valid_version ON mcp_tool(tool_num, valid, tool_version);
CREATE INDEX IF NOT EXISTS idx_mcp_tool_provider_id ON mcp_tool(provider_id);

-- 3. HTTP Template Converter表(HTTP转换模板)
CREATE TABLE IF NOT EXISTS http_template_converter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    create_by VARCHAR(128) DEFAULT 'system' NOT NULL COMMENT '创建人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    update_by VARCHAR(128) DEFAULT 'system' NOT NULL COMMENT '更新人',
    provider_id BIGINT COMMENT '提供者ID',
    tool_num BIGINT NOT NULL COMMENT '工具编号',
    tool_version BIGINT NOT NULL COMMENT '工具版本',
    req_url VARCHAR(1024) COMMENT 'http请求,ginja2模板',
    req_method VARCHAR(10),
    req_headers VARCHAR(2048) COMMENT 'http请求头,jinja2模板',
    req_body TEXT COMMENT 'http请求体,jinja2模板',
    resp_body TEXT COMMENT 'http响应体,jinja2模板',
    provider_tool_num BIGINT COMMENT '提供者工具编号'
);

CREATE INDEX IF NOT EXISTS idx_http_converter_provider_id ON http_template_converter(provider_id);

-- 4. Expo Template Converter表(Expo转换模板)
CREATE TABLE IF NOT EXISTS expo_template_converter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    create_by VARCHAR(128) DEFAULT 'system' NOT NULL COMMENT '创建人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    update_by VARCHAR(128) DEFAULT 'system' NOT NULL COMMENT '更新人',
    provider_id BIGINT COMMENT '提供者ID',
    tool_num BIGINT NOT NULL COMMENT '工具编号',
    tool_version BIGINT NOT NULL COMMENT '工具版本',
    app_class INT COMMENT 'expo app class',
    command_id INT COMMENT 'expo command id',
    input_args TEXT COMMENT 'jinja2模板',
    output_args TEXT COMMENT 'jinja2模板',
    provider_tool_num BIGINT COMMENT '提供者工具编号'
);

CREATE INDEX IF NOT EXISTS idx_expo_converter_provider_id ON expo_template_converter(provider_id);

-- 5. Origin Tool HTTP表(HTTP原始工具)
CREATE TABLE IF NOT EXISTS origin_tool_http (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    create_by VARCHAR(128) DEFAULT 'system' NOT NULL COMMENT '创建人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    update_by VARCHAR(128) DEFAULT 'system' NOT NULL COMMENT '更新人',
    provider_id BIGINT COMMENT '提供者ID',
    provider_tool_num BIGINT NOT NULL COMMENT '提供者工具编号',
    provider_tool_name VARCHAR(128) COMMENT '提供者工具名称',
    name_display VARCHAR(128) COMMENT '名称(开发者填写)',
    desc_display VARCHAR(1024) COMMENT '功能描述',
    req_url VARCHAR(512),
    req_method VARCHAR(10),
    req_headers VARCHAR(4096),
    input_schema TEXT COMMENT '遵足json schema规范',
    output_schema TEXT COMMENT '遵足json schema规范',
    provider_app_num BIGINT COMMENT '提供者app编号'
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_provider_tool_num_http ON origin_tool_http(provider_tool_num);
CREATE INDEX IF NOT EXISTS idx_http_provider_id ON origin_tool_http(provider_id);
CREATE INDEX IF NOT EXISTS idx_http_provider_app_num ON origin_tool_http(provider_app_num);

-- 6. Origin Tool Expo表(Expo原始工具)
CREATE TABLE IF NOT EXISTS origin_tool_expo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    create_by VARCHAR(128) DEFAULT 'system' NOT NULL COMMENT '创建人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    update_by VARCHAR(128) DEFAULT 'system' NOT NULL COMMENT '更新人',
    provider_id BIGINT COMMENT '提供者ID',
    provider_tool_num BIGINT NOT NULL COMMENT '提供者工具编号',
    provider_tool_name VARCHAR(128) COMMENT '提供者工具名称',
    name_display VARCHAR(128) COMMENT '名称',
    desc_display VARCHAR(1024) COMMENT '功能描述',
    app_class INT,
    command_id INT,
    function_name VARCHAR(64),
    expo_api_define VARCHAR(2048) COMMENT 'api描述, xml',
    provider_app_num BIGINT COMMENT '提供者app编号'
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_provider_tool_num_expo ON origin_tool_expo(provider_tool_num);
CREATE INDEX IF NOT EXISTS idx_expo_provider_id ON origin_tool_expo(provider_id);
CREATE INDEX IF NOT EXISTS idx_expo_provider_app_num ON origin_tool_expo(provider_app_num);

-- 7. Origin Provider Config表(提供商应用配置) - 替代了provider_app, virtual_server, vserver_items
CREATE TABLE IF NOT EXISTS origin_provider_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    create_by VARCHAR(128) NOT NULL COMMENT '创建人',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    update_by VARCHAR(128) NOT NULL COMMENT '更新人',
    app_num BIGINT COMMENT 'app编号',
    provider_id BIGINT COMMENT '提供商ID',
    app_name VARCHAR(100) COMMENT '应用名称',
    site_type VARCHAR(50) COMMENT '站点类型',
    app_ip VARCHAR(50) COMMENT '应用IP地址',
    app_port INT COMMENT '应用端口',
    load_factor INT DEFAULT 1 COMMENT '负载权重因子',
    request_timeout INT DEFAULT 60 COMMENT '请求超时时间(秒)',
    max_fail_count INT DEFAULT 3 COMMENT '最大失败次数',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用：0-禁用，1-启用',
    app_description VARCHAR(500) COMMENT '应用描述信息',
    health_check_url VARCHAR(200) COMMENT '健康检查URL',
    health_check_interval INT DEFAULT 5000 COMMENT '健康检查间隔(毫秒)',
    status INT DEFAULT 1 COMMENT '状态：-1-删除，0-禁用，1-启用',
    env VARCHAR(50) COMMENT '环境标识',
    config_key VARCHAR(100) COMMENT '配置key',
    config_value VARCHAR(1000) COMMENT '配置value'
);

CREATE INDEX IF NOT EXISTS idx_origin_provider_config_app_num ON origin_provider_config(app_num);
CREATE INDEX IF NOT EXISTS idx_origin_provider_config_provider_id ON origin_provider_config(provider_id);
CREATE INDEX IF NOT EXISTS idx_origin_provider_config_status ON origin_provider_config(status);
CREATE INDEX IF NOT EXISTS idx_origin_provider_config_is_enabled ON origin_provider_config(is_enabled);

-- ==================================================================
-- 注意事项:
-- 1. provider_app, virtual_server, vserver_items 在V8迁移中已废弃
-- 2. 统一使用 origin_provider_config 管理应用服务节点配置
-- 3. 使用H2数据库MySQL兼容模式
-- ==================================================================
