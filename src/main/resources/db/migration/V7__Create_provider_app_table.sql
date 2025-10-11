-- 创建provider_app表（提供者应用服务节点信息）
CREATE TABLE provider_app
(
    id                   bigint AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    provider_id          bigint                             NOT NULL COMMENT '提供者ID',
    app_name             varchar(100)                       NOT NULL COMMENT '应用名称',
    site_type            varchar(50)                        NULL COMMENT '站点类型（如：测试站、河西、外高桥等）',
    app_ip               varchar(50)                        NOT NULL COMMENT '应用IP地址',
    app_port             int                                NOT NULL COMMENT '应用端口',
    load_factor          int          DEFAULT 1             NOT NULL COMMENT '负载因子（权重）',
    request_timeout      int          DEFAULT 60            NOT NULL COMMENT '请求超时时间（秒）',
    max_fail_count       int          DEFAULT 3             NOT NULL COMMENT '最大失败次数',
    is_enabled           tinyint(1)   DEFAULT 1             NOT NULL COMMENT '是否启用：0-禁用，1-启用',
    app_description      varchar(500)                       NULL COMMENT '应用节点描述',
    health_check_url     varchar(200)                       NULL COMMENT '健康检查地址',
    health_check_interval int         DEFAULT 5000          NOT NULL COMMENT '健康检查间隔（毫秒）',
    status               int          DEFAULT 1             NOT NULL COMMENT '状态：-1-删除，0-禁用，1-启用',
    create_by            varchar(50)                        NULL COMMENT '创建人',
    create_time          datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_by            varchar(50)                        NULL COMMENT '更新人',
    last_update_time     datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    CONSTRAINT fk_provider_app_provider_id FOREIGN KEY (provider_id) REFERENCES provider (id) ON DELETE CASCADE,
    INDEX idx_provider_app_provider_id (provider_id),
    INDEX idx_provider_app_status (status),
    INDEX idx_provider_app_is_enabled (is_enabled),
    INDEX idx_provider_app_site_type (site_type)
)
    COMMENT '提供者应用服务节点表' COLLATE = utf8mb4_unicode_ci;
