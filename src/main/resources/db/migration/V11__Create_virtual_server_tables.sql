-- 创建virtual_server表(如果不存在)
CREATE TABLE IF NOT EXISTS virtual_server
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    create_time DATETIME     NULL COMMENT '创建时间',
    create_by   VARCHAR(128) NULL COMMENT '创建人',
    update_time DATETIME     NULL COMMENT '更新时间',
    update_by   VARCHAR(128) NULL COMMENT '更新人',
    vserver_id  VARCHAR(128) NULL COMMENT '虚拟服务器ID',
    userid      VARCHAR(128) NULL COMMENT '用户ID',
    name        VARCHAR(128) NULL COMMENT '虚拟服务器名称',
    `desc`      VARCHAR(512) NULL COMMENT '描述',
    url         VARCHAR(256) NULL COMMENT 'URL地址',
    status      CHAR(1)      NULL COMMENT '状态(1:启用,0:禁用)',
    UNIQUE KEY uk_vserver_id (vserver_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='虚拟服务器表';

-- 创建vserver_items表(如果不存在)
CREATE TABLE IF NOT EXISTS vserver_items
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    create_time   DATETIME     NULL COMMENT '创建时间',
    create_by     VARCHAR(128) NULL COMMENT '创建人',
    update_time   DATETIME     NULL COMMENT '更新时间',
    update_by     VARCHAR(128) NULL COMMENT '更新人',
    vserver_id    VARCHAR(128) NULL COMMENT '虚拟服务器ID',
    mcp_item_num  BIGINT       NULL COMMENT 'MCP项目编号',
    mcp_item_type CHAR(1)      NULL COMMENT 'MCP项目类型(1:tool,2:agent)',
    status        CHAR(1)      NULL COMMENT '状态(1:启用,0:禁用)',
    order_num     INT          NULL COMMENT '排序号',
    CONSTRAINT fk_vserver_items_vserver
        FOREIGN KEY (vserver_id) REFERENCES virtual_server (vserver_id)
            ON DELETE CASCADE,
    KEY idx_vserver_id (vserver_id),
    KEY idx_mcp_item (mcp_item_num, mcp_item_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='虚拟服务器项目表';

-- 插入测试用的virtual_server记录(如果不存在)
INSERT INTO virtual_server (create_time, create_by, update_time, update_by, vserver_id, userid, name, `desc`, url, status)
SELECT NOW(), 'system', NOW(), 'system', 'vserver_test', 'testuser', '测试虚拟服务器', '用于测试的虚拟服务器', 'http://test.example.com', '1'
WHERE NOT EXISTS (
    SELECT 1 FROM virtual_server WHERE vserver_id = 'vserver_test'
);