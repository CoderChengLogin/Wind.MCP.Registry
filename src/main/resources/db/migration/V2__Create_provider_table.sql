-- 创建provider表（提供者/系统用户）
CREATE TABLE provider
(
    id               bigint AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    username         varchar(50)                        NOT NULL COMMENT '用户名',
    password         varchar(255)                       NOT NULL COMMENT '加密后的密码',
    salt             varchar(50)                        NOT NULL COMMENT '密码盐',
    email            varchar(100)                       NULL COMMENT '邮箱',
    phone_number     varchar(20)                        NULL COMMENT '手机号码',
    company_name     varchar(200)                       NULL COMMENT '公司名称',
    contact_person   varchar(100)                       NULL COMMENT '联系人姓名',
    api_key          varchar(64)                        NULL COMMENT 'API密钥',
    api_secret       varchar(128)                       NULL COMMENT 'API密钥',
    status           int      DEFAULT 1                 NOT NULL COMMENT '状态：-1-删除，0-禁用，1-启用',
    create_time      datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    last_login_time  datetime                           NULL COMMENT '最后登录时间',
    last_update_time datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    CONSTRAINT uk_provider_username UNIQUE (username),
    CONSTRAINT uk_provider_api_key UNIQUE (api_key),
    INDEX idx_provider_status (status),
    INDEX idx_provider_email (email),
    INDEX idx_provider_phone (phone_number)
)
    COMMENT '提供者/系统用户表' COLLATE = utf8mb4_unicode_ci;