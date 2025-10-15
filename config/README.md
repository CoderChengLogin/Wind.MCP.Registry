# 配置文件目录说明

本目录用于集中管理项目的各类配置文件。

## 目录结构

```
config/
├── README.md                    # 本说明文档
├── logback-spring.xml          # Logback日志配置文件
└── [其他配置文件]              # 将来的配置文件放在这里
```

## 配置文件说明

### 1. logback-spring.xml - 日志配置

**功能**:

- 配置应用程序的日志输出策略
- 控制日志级别、格式、存储位置等

**主要特性**:

- ✅ 完全兼容 Spring Boot 3.x
- ✅ 支持控制台彩色输出
- ✅ 支持文件日志自动归档和压缩
- ✅ 支持异步日志提升性能
- ✅ 支持环境特定配置 (dev/test/prod)
- ✅ 错误日志单独存储

**日志位置**:

- 默认: `./logs/wind-mcp-registry.log`
- 错误: `./logs/wind-mcp-registry-error.log`
- 归档: `./logs/archive/`

**自定义日志路径**:
在 `application.yml` 中配置:

```yaml
logging:
  file:
    path: /your/custom/path
```

## 未来规划

以下类型的配置文件建议放在本目录:

### 建议存放的配置文件

1. **应用配置文件** (如有需要单独管理)
    - `application-custom.yml`
    - `application-{profile}.properties`

2. **数据库配置文件**
    - `database-config.xml`
    - `mybatis-config.xml`

3. **安全配置文件**
    - `security-config.yml`
    - `jwt-config.properties`

4. **缓存配置文件**
    - `redis-config.yml`
    - `ehcache.xml`

5. **消息队列配置**
    - `rabbitmq-config.yml`
    - `kafka-config.properties`

6. **外部服务配置**
    - `elasticsearch-config.yml`
    - `mongodb-config.properties`

7. **监控配置**
    - `prometheus-config.yml`
    - `actuator-config.properties`

### 不建议存放的文件

❌ **不要**将以下内容放在本目录:

- 敏感信息 (密码、密钥等) - 应使用环境变量或密钥管理系统
- 业务代码
- 静态资源文件
- 临时文件

## 配置文件命名规范

为保持一致性,建议遵循以下命名规范:

1. **使用小写字母和连字符**
    - ✅ `logback-spring.xml`
    - ❌ `LogbackSpring.xml`

2. **清晰的功能描述**
    - ✅ `database-connection-pool.yml`
    - ❌ `config1.yml`

3. **包含环境后缀(如有需要)**
    - `application-dev.yml`
    - `application-prod.yml`

## 使用方式

### Spring Boot 自动加载

Spring Boot 会自动从以下位置加载配置文件:

1. `classpath:/` (src/main/resources/)
2. `classpath:/config/`
3. `file:./` (项目根目录)
4. `file:./config/`

本目录的配置文件需要通过以下方式之一加载:

**方式1: 复制到 resources 目录** (推荐用于日志配置)

```bash
# Maven构建时自动复制
配置在 pom.xml 的 <resources> 标签中
```

**方式2: 启动参数指定**

```bash
java -jar app.jar --spring.config.location=file:./config/
```

**方式3: 环境变量**

```bash
export SPRING_CONFIG_LOCATION=file:./config/
```

## 维护建议

1. **版本控制**
    - ✅ 提交配置文件模板
    - ✅ 提交开发环境配置
    - ❌ 不要提交生产环境敏感信息

2. **文档更新**
    - 添加新配置文件时,请更新本 README
    - 说明配置文件的用途和主要参数

3. **安全性**
    - 使用 `.gitignore` 排除敏感配置
    - 生产环境配置使用环境变量或配置中心

4. **向后兼容**
    - 修改配置时注意向后兼容性
    - 重大变更需要在 CHANGELOG 中说明

## 相关文档

- [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Logback 官方文档](http://logback.qos.ch/manual/index.html)
- [项目主文档](../CLAUDE.md)

---

**最后更新**: 2025-10-15
**维护者**: 开发团队
