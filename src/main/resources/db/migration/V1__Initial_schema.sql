-- MCP工具注册中心初始化数据脚本

-- 创建系统配置表
create table system_config
(
    id           bigint auto_increment comment '主键'
        primary key,
    create_time  datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by    varchar(128) default 'system'          not null comment '创建人',
    update_time  datetime     default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by    varchar(128) default 'system'          not null comment '更新人',
    config_key   varchar(100)                           not null comment '配置键',
    config_value varchar(1000)                          null comment '配置值',
    config_type  varchar(20)  default 'string'          null comment '配置类型',
    description  varchar(500)                           null comment '配置描述',
    is_system    boolean      default false             null comment '是否系统配置',
    constraint uk_config_key
        unique (config_key)
);

-- 创建MCP工具表
create table mcp_tool
(
    id            bigint auto_increment comment '主键'
        primary key,
    create_time   datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by     varchar(128)  default 'system'          not null comment '创建人',
    update_time   datetime      default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by     varchar(128)  default 'system'          not null comment '更新人',
    name          varchar(100)                            not null comment '工具名称',
    type          varchar(50)                             null comment '工具类型',
    description   text                                    null comment '工具描述',
    version       varchar(20)                             null comment '版本号',
    author        varchar(100)                            null comment '作者',
    homepage      varchar(500)                            null comment '主页',
    repository    varchar(500)                            null comment '仓库地址',
    documentation text                                    null comment '文档',
    configuration text                                    null comment '配置信息',
    tags          varchar(500)                            null comment '标签',
    status        varchar(20)   default 'active'          null comment '状态',
    install_count int           default 0                 null comment '安装次数',
    rating        decimal(3, 2) default 0.00              null comment '评分',
    created_by    varchar(128)  default 'system'          null comment '创建者',
    updated_by    varchar(128)  default 'system'          null comment '更新者',
    constraint uk_name_version
        unique (name, version)
);

-- 创建Expo模板转换器表
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
);

-- 创建HTTP模板转换器表
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
);

-- 创建虚拟服务器表
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
    description varchar(512)                           null comment '描述',
    url         varchar(256)                           null comment 'url',
    status      char                                   null comment '状态: 1 已发布',
    constraint uk_vserver_id
        unique (vserver_id)
);

-- 创建虚拟服务器关联条目表
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
);

create index key_vserver_id
    on vserver_items (vserver_id);

-- 创建原始工具导出表
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
);

-- 创建原始HTTP工具表
create table origin_tool_http
(
    id                    bigint auto_increment comment '主键'
        primary key,
    create_time           datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by             varchar(128)  default 'system'          not null comment '创建人',
    update_time           datetime      default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by             varchar(128)  default 'system'          not null comment '更新人',
    name                  varchar(100)                            not null comment '工具名称',
    type                  varchar(50)                             null comment '工具类型',
    description           text                                    null comment '工具描述',
    method                varchar(10)                             not null comment 'HTTP方法',
    url                   varchar(1000)                           not null comment 'API地址',
    headers               text                                    null comment '请求头',
    parameters            text                                    null comment '请求参数',
    request_body          text                                    null comment '请求体',
    response_example      text                                    null comment '响应示例',
    timeout_seconds       int           default 30                null comment '超时时间(秒)',
    retry_count           int           default 0                 null comment '重试次数',
    authentication_type   varchar(20)                             null comment '认证类型',
    authentication_config text                                    null comment '认证配置',
    tags                  varchar(500)                            null comment '标签',
    status                varchar(20)   default 'active'          null comment '状态',
    test_count            int           default 0                 null comment '测试次数',
    success_rate          decimal(5, 2) default 0.00              null comment '成功率',
    avg_response_time     int           default 0                 null comment '平均响应时间(ms)',
    created_by            varchar(128)  default 'system'          null comment '创建者',
    updated_by            varchar(128)  default 'system'          null comment '更新者',
    constraint uk_name_method_url
        unique (name, method, url)
);

-- 创建原始提供者环境配置表
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
);

-- 创建工具使用统计表
create table tool_usage_stats
(
    id                bigint auto_increment comment '主键'
        primary key,
    tool_type         varchar(20)                        not null comment '工具类型(mcp/http)',
    tool_id           bigint                             not null comment '工具ID',
    usage_date        date                               not null comment '使用日期',
    usage_count       int      default 0                 null comment '使用次数',
    success_count     int      default 0                 null comment '成功次数',
    error_count       int      default 0                 null comment '错误次数',
    avg_response_time int      default 0                 null comment '平均响应时间(ms)',
    create_time       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_tool_type_id_date
        unique (tool_type, tool_id, usage_date)
);

-- 创建操作日志表
create table operation_log
(
    id              bigint auto_increment comment '主键'
        primary key,
    operation_type  varchar(20)                           not null comment '操作类型',
    resource_type   varchar(50)                           not null comment '资源类型',
    resource_id     bigint                                null comment '资源ID',
    operation_desc  varchar(500)                          null comment '操作描述',
    operator        varchar(128)                          not null comment '操作者',
    ip_address      varchar(45)                           null comment 'IP地址',
    user_agent      varchar(500)                          null comment '用户代理',
    request_params  text                                  null comment '请求参数',
    response_result text                                  null comment '响应结果',
    execution_time  int         default 0                 null comment '执行时间(ms)',
    status          varchar(20) default 'success'         null comment '状态',
    create_time     datetime    default CURRENT_TIMESTAMP not null comment '创建时间'
);