DROP TABLE IF EXISTS orm_user;
CREATE TABLE orm_user
(
    id               INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(32) NOT NULL UNIQUE,
    password         VARCHAR(32) NOT NULL,
    salt             VARCHAR(32) NOT NULL,
    email            VARCHAR(32) NOT NULL UNIQUE,
    phone_number     VARCHAR(15) NOT NULL UNIQUE,
    status           INT         NOT NULL DEFAULT 1,
    create_time      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_time  TIMESTAMP            DEFAULT NULL,
    last_update_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS orm_role;
CREATE TABLE orm_role
(
    id   INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE
);
