create table mcp_tool
(
    id                  bigint auto_increment comment '主键'
        primary key,
    create_time         datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    create_by           varchar(128) default 'system'          not null comment '创建人',
    update_time         datetime     default CURRENT_TIMESTAMP not null comment '更新时间',
    update_by           varchar(128) default 'system'          not null comment '更新人',
    tool_num            bigint                                 not null comment '工具编号',
    tool_version        bigint                                 not null comment '工具版本',
    valid               char                                   not null comment '1: 有效, 0: 无效',
    tool_name           varchar(256)                           null comment 'mcp工具英文名',
    tool_description    varchar(2000)                          null comment 'mcp工具描述',
    name_display        text                                   null comment '显示用名字，多语言',
    description_display text                                   null comment '显示用描述，多语言',
    input_schema        text                                   null comment 'mcp工具，遵足json schema规范',
    output_schema       text                                   null comment '输出schema',
    stream_output       char         default '0'               null comment '0:非流, 1:流式',
    convert_type        char                                   null comment '三种方式: 1 http, 2 expo, 3 code',
    tool_type           char                                   null comment '1: tool, 2: agent',
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
