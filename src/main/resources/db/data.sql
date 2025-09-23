create table mcp_tool
(
    id                  bigint auto_increment comment '主键'
        primary key,
    create_time         datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by           varchar(128) default 'system'          not null comment '创建人',
    update_time         datetime     default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by           varchar(128) default 'system'          not null comment '更新人',
    tool_num            bigint                                 not null comment '工具编号',
    tool_version        varchar(50)                            not null comment '工具版本',
    valid               char                                   not null comment '1: 有效, 0: 无效',
    tool_name           varchar(256)                           null comment 'mcp工具英文名',
    tool_desc           varchar(2000)                          null comment 'mcp工具描述',
    tool_type           varchar(50)                            null comment '工具类型',
    tool_status         varchar(20)                            null comment '工具状态',
    tool_config         text                                   null comment '工具配置',
    tool_author         varchar(100)                           null comment '工具作者',
    tool_url            varchar(500)                           null comment '工具URL',
    tool_tags           varchar(500)                           null comment '工具标签',
    tool_doc            text                                   null comment '工具文档',
    name_display        text                                   null comment '显示用名字，多语言',
    description_display text                                   null comment '显示用描述，多语言',
    input_schema        text                                   null comment 'mcp工具，遵足json schema规范',
    output_schema       text                                   null comment '输出schema',
    stream_output       char         default '0'               null comment '0:非流, 1:流式',
    convert_type        char                                   null comment '三种方式: 1 http, 2 expo, 3 code',
    constraint uk_tool_num_valid_version
        unique (tool_num, valid, tool_version)
)
    comment 'MCP工具' collate = utf8mb4_unicode_ci;

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
create table origin_tool_http
(
    id                 bigint auto_increment comment '主键'
        primary key,
    create_time        datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by          varchar(128) default 'system'          not null comment '创建人',
    update_time        datetime     default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by          varchar(128) default 'system'          not null comment '更新人',
    provider_tool_num  bigint                                 not null comment '提供者工具编号',
    provider_tool_name varchar(128)                           null comment '提供者工具名称',
    name_display       varchar(128)                           null comment '名称(开发者填写)',
    desc_display       varchar(1024)                          null comment '功能描述',
    req_url            varchar(512)                           null,
    req_method         varchar(10)                            null,
    req_headers        varchar(4096)                          null,
    input_schema       json                                   null comment '遵足json schema规范',
    output_schema      json                                   null comment '遵足json schema规范',
    provider_app_num   bigint                                 null comment '提供者app编号',
    constraint uk_provider_tool_num_http
        unique (provider_tool_num)
)
    comment '原始HTTP工具' collate = utf8mb4_unicode_ci;


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

-- MCP工具注册中心初始化数据脚本

-- 插入系统配置数据
INSERT INTO system_config (config_key, config_value, config_type, description, is_system)
VALUES ('system.name', 'MCP工具注册中心', 'string', '系统名称', 1),
       ('system.version', '1.0.0', 'string', '系统版本', 1),
       ('system.description', '用于管理和注册MCP工具及原始HTTP工具的中心化平台', 'string', '系统描述', 1),
       ('pagination.default_size', '10', 'number', '默认分页大小', 1),
       ('pagination.max_size', '100', 'number', '最大分页大小', 1),
       ('tool.max_name_length', '100', 'number', '工具名称最大长度', 1),
       ('tool.max_description_length', '2000', 'number', '工具描述最大长度', 1),
       ('http.default_timeout', '30', 'number', 'HTTP请求默认超时时间(秒)', 1),
       ('http.max_retry_count', '3', 'number', 'HTTP请求最大重试次数', 1),
       ('log.retention_days', '90', 'number', '日志保留天数', 1);

-- 插入示例MCP工具数据
INSERT INTO mcp_tool (name, type, description, version, author, homepage, repository, documentation, configuration,
                      tags, status, install_count, rating, created_by, updated_by)
VALUES ('file-manager', 'utility', '文件管理工具，支持文件的创建、读取、更新和删除操作', '1.0.0', 'MCP Team',
        'https://github.com/mcp/file-manager', 'https://github.com/mcp/file-manager.git',
        '# 文件管理工具\n\n这是一个强大的文件管理工具，提供以下功能：\n- 创建文件和目录\n- 读取文件内容\n- 更新文件内容\n- 删除文件和目录\n- 文件权限管理',
        '{"max_file_size": "10MB", "allowed_extensions": [".txt", ".json", ".xml", ".csv"], "base_directory": "/data"}',
        '文件管理,工具,实用程序', 'active', 156, 4.5, 'system', 'system'),

       ('database-connector', 'database', '数据库连接工具，支持多种数据库类型的连接和操作', '2.1.0', 'DB Team',
        'https://github.com/mcp/database-connector', 'https://github.com/mcp/database-connector.git',
        '# 数据库连接工具\n\n支持以下数据库：\n- MySQL\n- PostgreSQL\n- SQLite\n- MongoDB\n\n功能特性：\n- 连接池管理\n- SQL查询执行\n- 事务支持\n- 数据导入导出',
        '{"connection_pool_size": 10, "query_timeout": 30, "supported_databases": ["mysql", "postgresql", "sqlite", "mongodb"]}',
        '数据库,连接器,SQL', 'active', 89, 4.2, 'system', 'system'),

       ('api-client', 'network', 'HTTP API客户端工具，简化API调用和响应处理', '1.5.2', 'API Team', 'https://github.com/mcp/api-client',
        'https://github.com/mcp/api-client.git',
        '# API客户端工具\n\n功能包括：\n- RESTful API调用\n- 认证支持（Basic, Bearer, API Key）\n- 请求/响应拦截器\n- 自动重试机制\n- 响应缓存',
        '{"default_timeout": 30, "max_retries": 3, "cache_enabled": true, "auth_types": ["basic", "bearer", "api_key"]}',
        'API,HTTP,网络,客户端', 'active', 234, 4.7, 'system', 'system'),

       ('text-processor', 'text', '文本处理工具，提供各种文本操作和转换功能', '1.2.1', 'Text Team', 'https://github.com/mcp/text-processor',
        'https://github.com/mcp/text-processor.git',
        '# 文本处理工具\n\n提供以下功能：\n- 文本格式转换\n- 正则表达式匹配\n- 文本统计分析\n- 编码转换\n- 文本清理和标准化',
        '{"max_text_length": 1000000, "supported_encodings": ["utf-8", "gbk", "ascii"], "regex_timeout": 5}',
        '文本处理,转换,正则表达式', 'active', 67, 4.1, 'system', 'system'),

       ('scheduler', 'automation', '任务调度工具，支持定时任务和事件驱动任务', '3.0.0', 'Schedule Team', 'https://github.com/mcp/scheduler',
        'https://github.com/mcp/scheduler.git',
        '# 任务调度工具\n\n特性：\n- Cron表达式支持\n- 事件驱动调度\n- 任务依赖管理\n- 失败重试机制\n- 任务执行历史',
        '{"max_concurrent_tasks": 50, "default_retry_count": 3, "history_retention_days": 30}',
        '调度,定时任务,自动化', 'active', 123, 4.4, 'system', 'system');

-- 插入示例原始HTTP工具数据
INSERT INTO origin_tool_http (name, type, description, method, url, headers, parameters, request_body, response_example,
                              timeout_seconds, retry_count, authentication_type, authentication_config, tags, status,
                              test_count, success_rate, avg_response_time, created_by, updated_by)
VALUES ('weather-api', 'weather', '获取天气信息的API工具', 'GET', 'https://api.openweathermap.org/data/2.5/weather',
        '{"Content-Type": "application/json", "Accept": "application/json"}',
        '{"q": "Beijing", "appid": "your_api_key", "units": "metric"}',
        NULL,
        '{"coord":{"lon":116.4074,"lat":39.9042},"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"main":{"temp":25.5,"feels_like":24.8,"temp_min":23.2,"temp_max":27.1,"pressure":1013,"humidity":45},"name":"Beijing","cod":200}',
        30, 2, 'api_key', '{"key_name": "appid", "key_location": "query"}',
        '天气,API,查询', 'active', 45, 95.56, 1250, 'system', 'system'),

       ('user-info', 'user', '获取用户信息的API', 'GET', 'https://jsonplaceholder.typicode.com/users/{id}',
        '{"Content-Type": "application/json"}',
        '{"id": "1"}',
        NULL,
        '{"id":1,"name":"Leanne Graham","username":"Bret","email":"Sincere@april.biz","address":{"street":"Kulas Light","suite":"Apt. 556","city":"Gwenborough","zipcode":"92998-3874"},"phone":"1-770-736-8031 x56442","website":"hildegard.org"}',
        15, 1, 'none', NULL,
        '用户,信息,查询', 'active', 78, 98.72, 890, 'system', 'system'),

       ('create-post', 'social', '创建新帖子的API', 'POST', 'https://jsonplaceholder.typicode.com/posts',
        '{"Content-Type": "application/json"}',
        NULL,
        '{"title": "新帖子标题", "body": "帖子内容", "userId": 1}',
        '{"id": 101, "title": "新帖子标题", "body": "帖子内容", "userId": 1}',
        20, 1, 'bearer', '{"token_prefix": "Bearer"}',
        '社交,帖子,创建', 'active', 23, 91.30, 1560, 'system', 'system'),

       ('file-upload', 'storage', '文件上传API工具', 'POST', 'https://httpbin.org/post',
        '{"Accept": "application/json"}',
        NULL,
        '{"file": "@/path/to/file.txt", "description": "测试文件上传"}',
        '{"args": {}, "data": "", "files": {"file": "file content"}, "form": {"description": "测试文件上传"}, "headers": {"Accept": "application/json"}, "json": null, "origin": "127.0.0.1", "url": "https://httpbin.org/post"}',
        60, 2, 'basic', '{"username": "user", "password": "pass"}',
        '文件,上传,存储', 'active', 12, 83.33, 2340, 'system', 'system'),

       ('search-api', 'search', '搜索API工具', 'GET', 'https://api.github.com/search/repositories',
        '{"Accept": "application/vnd.github.v3+json", "User-Agent": "MCP-Registry/1.0"}',
        '{"q": "language:java", "sort": "stars", "order": "desc", "per_page": "10"}',
        NULL,
        '{"total_count": 12345, "incomplete_results": false, "items": [{"id": 123, "name": "awesome-java", "full_name": "user/awesome-java", "description": "A curated list of awesome Java frameworks", "stargazers_count": 5000}]}',
        25, 1, 'none', NULL,
        '搜索,GitHub,仓库', 'active', 156, 96.15, 1100, 'system', 'system');

-- 插入工具使用统计数据（最近7天的模拟数据）
INSERT INTO tool_usage_stats (tool_type, tool_id, usage_date, usage_count, success_count, error_count,
                              avg_response_time)
VALUES
-- MCP工具统计
('mcp', 1, CURDATE() - INTERVAL 6 DAY, 15, 14, 1, 1200),
('mcp', 1, CURDATE() - INTERVAL 5 DAY, 18, 17, 1, 1150),
('mcp', 1, CURDATE() - INTERVAL 4 DAY, 22, 21, 1, 1300),
('mcp', 1, CURDATE() - INTERVAL 3 DAY, 19, 18, 1, 1250),
('mcp', 1, CURDATE() - INTERVAL 2 DAY, 25, 24, 1, 1180),
('mcp', 1, CURDATE() - INTERVAL 1 DAY, 21, 20, 1, 1220),
('mcp', 1, CURDATE(), 16, 15, 1, 1190),

('mcp', 2, CURDATE() - INTERVAL 6 DAY, 8, 7, 1, 2100),
('mcp', 2, CURDATE() - INTERVAL 5 DAY, 12, 11, 1, 1950),
('mcp', 2, CURDATE() - INTERVAL 4 DAY, 10, 9, 1, 2200),
('mcp', 2, CURDATE() - INTERVAL 3 DAY, 14, 13, 1, 2050),
('mcp', 2, CURDATE() - INTERVAL 2 DAY, 11, 10, 1, 2150),
('mcp', 2, CURDATE() - INTERVAL 1 DAY, 9, 8, 1, 2000),
('mcp', 2, CURDATE(), 13, 12, 1, 2080),

-- HTTP工具统计
('http', 1, CURDATE() - INTERVAL 6 DAY, 5, 5, 0, 1250),
('http', 1, CURDATE() - INTERVAL 5 DAY, 7, 7, 0, 1200),
('http', 1, CURDATE() - INTERVAL 4 DAY, 6, 6, 0, 1300),
('http', 1, CURDATE() - INTERVAL 3 DAY, 8, 7, 1, 1280),
('http', 1, CURDATE() - INTERVAL 2 DAY, 9, 9, 0, 1220),
('http', 1, CURDATE() - INTERVAL 1 DAY, 4, 4, 0, 1180),
('http', 1, CURDATE(), 6, 6, 0, 1240),

('http', 2, CURDATE() - INTERVAL 6 DAY, 12, 12, 0, 890),
('http', 2, CURDATE() - INTERVAL 5 DAY, 15, 15, 0, 920),
('http', 2, CURDATE() - INTERVAL 4 DAY, 11, 11, 0, 850),
('http', 2, CURDATE() - INTERVAL 3 DAY, 18, 17, 1, 910),
('http', 2, CURDATE() - INTERVAL 2 DAY, 14, 14, 0, 880),
('http', 2, CURDATE() - INTERVAL 1 DAY, 16, 16, 0, 900),
('http', 2, CURDATE(), 13, 13, 0, 870);

-- 插入操作日志数据（最近的一些操作记录）
INSERT INTO operation_log (operation_type, resource_type, resource_id, operation_desc, operator, ip_address, user_agent,
                           request_params, response_result, execution_time, status)
VALUES ('CREATE', 'mcp_tool', 1, '创建MCP工具：file-manager', 'system', '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', '{"name":"file-manager","type":"utility"}',
        '{"success":true,"id":1}', 150, 'success'),
       ('CREATE', 'mcp_tool', 2, '创建MCP工具：database-connector', 'system', '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        '{"name":"database-connector","type":"database"}', '{"success":true,"id":2}', 120, 'success'),
       ('CREATE', 'http_tool', 1, '创建HTTP工具：weather-api', 'system', '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', '{"name":"weather-api","method":"GET"}',
        '{"success":true,"id":1}', 100, 'success'),
       ('TEST', 'http_tool', 1, '测试HTTP工具：weather-api', 'system', '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        '{"url":"https://api.openweathermap.org/data/2.5/weather"}', '{"success":true,"response_time":1250}', 1250,
        'success'),
       ('VIEW', 'mcp_tool', 1, '查看MCP工具详情：file-manager', 'system', '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', '{"id":1}', '{"success":true}', 50, 'success'),
       ('UPDATE', 'mcp_tool', 3, '更新MCP工具：api-client', 'system', '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', '{"id":3,"version":"1.5.2"}',
        '{"success":true}', 80, 'success'),
       ('DELETE', 'http_tool', 999, '删除HTTP工具失败：工具不存在', 'system', '127.0.0.1',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', '{"id":999}',
        '{"success":false,"error":"Tool not found"}', 30, 'error');

-- 插入角色数据
INSERT INTO orm_role (name, description, status)
VALUES ('ADMIN', '系统管理员，拥有所有权限', 1),
       ('USER', '普通用户，可以查看和使用工具', 1),
       ('DEVELOPER', '开发者，可以创建和管理自己的工具', 1);

-- 插入用户数据
-- 默认密码为 "123456"，盐为 "salt123"，加密后的密码为 SHA256(123456salt123) = e10adc3949ba59abbe56e057f20f883e
INSERT INTO orm_user (name, password, salt, email, phone_number, status)
VALUES ('admin', 'd93a5def7511da3d0f2d171d9c344e91', 'salt123', 'admin@example.com', '13800138000', 1),
       ('user1', 'd93a5def7511da3d0f2d171d9c344e91', 'salt123', 'user1@example.com', '13800138001', 1),
       ('developer1', 'd93a5def7511da3d0f2d171d9c344e91', 'salt123', 'developer1@example.com', '13800138002', 1);

-- 插入用户角色关联数据
INSERT INTO orm_user_role (user_id, role_id)
VALUES (1, 1), -- admin 拥有 ADMIN 角色
       (2, 2), -- user1 拥有 USER 角色
       (3, 3), -- developer1 拥有 DEVELOPER 角色
       (3, 2); -- developer1 同时拥有 USER 角色
