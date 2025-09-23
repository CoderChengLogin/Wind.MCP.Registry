-- MCP工具注册中心数据库初始化脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS mcp_registry DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mcp_registry;

-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS mcp_tool;

-- 创建MCP工具表
CREATE TABLE mcp_tool
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name          VARCHAR(100) NOT NULL COMMENT '工具名称',
    type          VARCHAR(50)  NOT NULL COMMENT '工具类型',
    description   TEXT COMMENT '工具描述',
    version       VARCHAR(20)   DEFAULT '1.0.0' COMMENT '版本号',
    author        VARCHAR(100) COMMENT '作者',
    homepage      VARCHAR(500) COMMENT '主页地址',
    repository    VARCHAR(500) COMMENT '仓库地址',
    documentation TEXT COMMENT '文档内容',
    configuration TEXT COMMENT '配置信息(JSON格式)',
    tags          VARCHAR(500) COMMENT '标签，逗号分隔',
    status        VARCHAR(20)   DEFAULT 'active' COMMENT '状态：active-活跃, inactive-非活跃, deprecated-已弃用',
    install_count INT           DEFAULT 0 COMMENT '安装次数',
    rating        DECIMAL(3, 2) DEFAULT 0.00 COMMENT '评分(0-5)',
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by    VARCHAR(100) COMMENT '创建者',
    updated_by    VARCHAR(100) COMMENT '更新者',

    -- 索引
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_tags (tags),

    -- 唯一约束
    UNIQUE KEY uk_name_version (name, version)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='MCP工具表';

create table expo_template_converter
(
    id                bigint auto_increment comment '主键'
        primary key,
    create_time       datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by         varchar(128) default 'system'          not null comment '创建人',
    update_time       datetime     default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by         varchar(128) default 'system'          not null comment '更新人',
    tool_num          bigint                                 not null comment '工具编号',
    tool_version      bigint                                 not null comment '工具版本',
    app_class         int                                    null comment 'expo app class',
    command_id        int                                    null comment 'expo command id',
    input_args        text                                   null comment 'jinja2 模板',
    output_args       text                                   null comment 'jinja2 模板',
    provider_tool_num bigint                                 null comment '提供者工具编号'
)
    comment 'Expo模板转换器' collate = utf8mb4_unicode_ci;
create table http_template_converter
(
    id                bigint auto_increment comment '主键'
        primary key,
    create_time       datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by         varchar(128) default 'system'          not null comment '创建人',
    update_time       datetime     default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by         varchar(128) default 'system'          not null comment '更新人',
    tool_num          bigint                                 not null comment '工具编号',
    tool_version      bigint                                 not null comment '工具版本',
    req_url           varchar(1024)                          null comment 'http请求,ginja2 模板',
    req_method        varchar(10)                            null,
    req_headers       varchar(2048)                          null comment 'http请求头,jinja2 模板',
    req_body          text                                   null comment 'http请求体,jinja2 模板',
    resp_body         text                                   null comment 'http响应体,jinja2 模板',
    provider_tool_num bigint                                 null comment '提供者工具编号'
)
    comment 'HTTP模板转换器' collate = utf8mb4_unicode_ci;


create table virtual_server
(
    id          bigint auto_increment comment '主键'
        primary key,
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by   varchar(128) default 'system'          not null comment '创建人',
    update_time datetime     default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by   varchar(128) default 'system'          not null comment '更新人',
    vserver_id  varchar(128)                           null comment '虚拟服务器id',
    userid      varchar(128)                           null comment '用户id',
    name        varchar(128)                           null comment '名称',
    `desc`      varchar(512)                           null comment '描述',
    url         varchar(256)                           null comment 'url',
    status      char                                   null comment '状态: 1 已发布',
    constraint uk_vserver_id
        unique (vserver_id)
)
    comment '虚拟服务器' collate = utf8mb4_unicode_ci;
create table vserver_items
(
    id            bigint auto_increment comment '主键'
        primary key,
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by     varchar(128)                       not null comment '创建人',
    update_time   datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by     varchar(128)                       not null comment '更新人',
    vserver_id    varchar(128)                       null comment '虚拟服务器id',
    mcp_item_num  bigint                             null comment 'mcp项目编号',
    mcp_item_type char                               null comment 'MCP类型(1:tool, 2:prompt, 3:resource)',
    status        char                               null comment '状态: 1 已发布',
    order_num     int                                null comment '排序'
)
    comment '虚拟服务器关联条目' collate = utf8mb4_unicode_ci;

create index key_vserver_id
    on vserver_items (vserver_id);


create table origin_tool_expo
(
    id                 bigint auto_increment comment '主键'
        primary key,
    create_time        datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by          varchar(128) default 'system'          not null comment '创建人',
    update_time        datetime     default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by          varchar(128) default 'system'          not null comment '更新人',
    provider_tool_num  bigint                                 not null comment '提供者工具编号',
    provider_tool_name varchar(128)                           null comment '提供者工具名称',
    name_display       varchar(128)                           null comment '名称',
    desc_display       varchar(1024)                          null comment '功能描述',
    app_class          int                                    null,
    command_id         int                                    null,
    function_name      varchar(64)                            null,
    expo_api_define    varchar(2048)                          null comment 'api描述, xml',
    provider_app_num   bigint                                 null comment '提供者app编号',
    constraint uk_provider_tool_num_expo
        unique (provider_tool_num)
)
    comment '原始Expo工具' collate = utf8mb4_unicode_ci;
-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS origin_tool_http;

-- 创建原始HTTP工具表
CREATE TABLE origin_tool_http
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name                  VARCHAR(100)  NOT NULL COMMENT '工具名称',
    type                  VARCHAR(50)   NOT NULL COMMENT '工具类型',
    description           TEXT COMMENT '工具描述',
    method                VARCHAR(10)   NOT NULL DEFAULT 'GET' COMMENT 'HTTP方法：GET, POST, PUT, DELETE等',
    url                   VARCHAR(1000) NOT NULL COMMENT '请求URL',
    headers               TEXT COMMENT '请求头信息(JSON格式)',
    parameters            TEXT COMMENT '请求参数(JSON格式)',
    request_body          TEXT COMMENT '请求体(JSON格式)',
    response_example      TEXT COMMENT '响应示例(JSON格式)',
    timeout_seconds       INT                    DEFAULT 30 COMMENT '超时时间(秒)',
    retry_count           INT                    DEFAULT 0 COMMENT '重试次数',
    authentication_type   VARCHAR(50) COMMENT '认证类型：none, basic, bearer, api_key',
    authentication_config TEXT COMMENT '认证配置(JSON格式)',
    tags                  VARCHAR(500) COMMENT '标签，逗号分隔',
    status                VARCHAR(20)            DEFAULT 'active' COMMENT '状态：active-活跃, inactive-非活跃, deprecated-已弃用',
    test_count            INT                    DEFAULT 0 COMMENT '测试次数',
    success_rate          DECIMAL(5, 2)          DEFAULT 0.00 COMMENT '成功率(%)',
    avg_response_time     INT                    DEFAULT 0 COMMENT '平均响应时间(毫秒)',
    created_at            TIMESTAMP              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at            TIMESTAMP              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by            VARCHAR(100) COMMENT '创建者',
    updated_by            VARCHAR(100) COMMENT '更新者',

    -- 索引
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_method (method),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_tags (tags),
    INDEX idx_url (url(255)),

    -- 唯一约束
    UNIQUE KEY uk_name_method_url (name, method, url(255))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='原始HTTP工具表';

-- 创建工具使用统计表
CREATE TABLE tool_usage_stats
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tool_type         VARCHAR(20) NOT NULL COMMENT '工具类型：mcp, http',
    tool_id           BIGINT      NOT NULL COMMENT '工具ID',
    usage_date        DATE        NOT NULL COMMENT '使用日期',
    usage_count       INT       DEFAULT 0 COMMENT '使用次数',
    success_count     INT       DEFAULT 0 COMMENT '成功次数',
    error_count       INT       DEFAULT 0 COMMENT '错误次数',
    avg_response_time INT       DEFAULT 0 COMMENT '平均响应时间(毫秒)',
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    INDEX idx_tool_type_id (tool_type, tool_id),
    INDEX idx_usage_date (usage_date),

    -- 唯一约束
    UNIQUE KEY uk_tool_date (tool_type, tool_id, usage_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='工具使用统计表';

-- 创建系统配置表
CREATE TABLE system_config
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_key   VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type  VARCHAR(20) DEFAULT 'string' COMMENT '配置类型：string, number, boolean, json',
    description  VARCHAR(500) COMMENT '配置描述',
    is_system    TINYINT(1)  DEFAULT 0 COMMENT '是否系统配置：0-否, 1-是',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    UNIQUE KEY uk_config_key (config_key),
    INDEX idx_is_system (is_system)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统配置表';

-- 创建操作日志表
CREATE TABLE operation_log
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    operation_type  VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE, UPDATE, DELETE, VIEW, TEST',
    resource_type   VARCHAR(20) NOT NULL COMMENT '资源类型：mcp_tool, http_tool, system_config',
    resource_id     BIGINT COMMENT '资源ID',
    operation_desc  VARCHAR(500) COMMENT '操作描述',
    operator        VARCHAR(100) COMMENT '操作者',
    ip_address      VARCHAR(50) COMMENT 'IP地址',
    user_agent      TEXT COMMENT '用户代理',
    request_params  TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    execution_time  INT COMMENT '执行时间(毫秒)',
    status          VARCHAR(20) DEFAULT 'success' COMMENT '状态：success-成功, error-失败',
    error_message   TEXT COMMENT '错误信息',
    created_at      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 索引
    INDEX idx_operation_type (operation_type),
    INDEX idx_resource_type (resource_type),
    INDEX idx_resource_id (resource_id),
    INDEX idx_operator (operator),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='操作日志表';

-- 创建用户表
CREATE TABLE orm_user
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name             VARCHAR(50)  NOT NULL COMMENT '用户名',
    password         VARCHAR(128) NOT NULL COMMENT '加密后的密码',
    salt             VARCHAR(50)  NOT NULL COMMENT '密码盐',
    email            VARCHAR(100) COMMENT '邮箱',
    phone_number     VARCHAR(20) COMMENT '手机号码',
    status           INT       DEFAULT 1 COMMENT '状态：-1-删除，0-禁用，1-启用',
    create_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_login_time  TIMESTAMP    NULL COMMENT '最后登录时间',
    last_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    -- 索引
    UNIQUE KEY uk_name (name),
    UNIQUE KEY uk_email (email),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

-- 创建角色表
CREATE TABLE orm_role
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name             VARCHAR(50) NOT NULL COMMENT '角色名称',
    description      VARCHAR(200) COMMENT '角色描述',
    status           INT       DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    -- 索引
    UNIQUE KEY uk_name (name),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色表';

-- 创建用户角色关联表
CREATE TABLE orm_user_role
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    role_id     BIGINT NOT NULL COMMENT '角色ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 索引
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),

    -- 外键约束
    FOREIGN KEY (user_id) REFERENCES orm_user (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES orm_role (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户角色关联表';


create table origin_provider_config
(
    id           bigint auto_increment comment '主键'
        primary key,
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by    varchar(128)                       not null comment '创建人',
    update_time  datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by    varchar(128)                       not null comment '更新人',
    app_num      bigint                             null comment 'app编号',
    env          varchar(50)                        null comment '环境',
    config_key   varchar(100)                       null comment '配置key',
    config_value varchar(1000)                      null comment '配置value',
    constraint uk_app_num_env_config_key
        unique (app_num, env, config_key)
)
    comment '原始提供者环境配置' collate = utf8mb4_unicode_ci;

