# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Wind MCP Registry 是一个基于Spring Boot 3.3.6的MCP工具注册管理系统,用于管理和注册MCP(Model Context Protocol)
工具信息。该系统支持HTTP、Expo等多种类型的工具转换和管理,提供完整的CRUD功能、多表联合编辑、权限控制和工具测试功能。

**最后更新时间**: 2025-10-15
**当前版本**: v1.3.0

### 核心功能模块

1. **MCP工具管理** (`/mcp-tools`) - 标准化的MCP工具注册和管理
2. **HTTP工具管理** (`/origin-http-tools`) - 基于HTTP协议的原始工具管理
3. **Expo工具管理** (`/origin-tools-expo`) - Expo类型工具的专门管理
4. **提供商管理** (`/provider`) - 工具提供商信息维护和认证
5. **应用节点管理** (`/provider-app`) - 提供商应用服务节点的负载均衡配置
6. **工具录入向导** (`/tool-wizard`) - 用户友好的分步式工具添加流程

## 技术栈

- **Java 17** - 开发语言
- **Spring Boot 3.3.6** - 主框架
- **MyBatis Plus 3.5.10** - ORM框架,支持ActiveRecord模式
- **Thymeleaf** - 服务端模板引擎
- **MySQL 8.0** - 数据库 (远程Docker部署在192.168.248.208:3306)
- **Flyway** - 数据库版本管理 (当前配置已禁用,需手动迁移)
- **Hutool 4.6.3** - 工具类库 (建议升级到5.x)
- **Lombok** - 注解简化代码
- **Logback** - 日志系统
- **Bootstrap 5.1.3** - 前端UI框架
- **Font Awesome 6.0.0** - 图标库

## 常用命令

### 构建和运行

```bash
# 清理编译
mvn clean compile

# 运行应用
mvn spring-boot:run

# 清理并运行
mvn clean spring-boot:run

# 停止运行中的Java进程
taskkill /F /IM java.exe
```

### 测试

```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=ProviderServiceTest

# 运行特定测试方法
mvn test -Dtest=ProviderServiceTest#testCreateProvider
```

### 数据库迁移 (Flyway已禁用)

```bash
# 注意: 当前配置中Flyway已禁用 (spring.flyway.enabled=false)
# 如需迁移,请手动执行SQL脚本或临时启用Flyway

# 执行数据库迁移 (需先启用Flyway)
mvn flyway:migrate

# 查看迁移状态
mvn flyway:info

# 清理数据库 (谨慎使用,会删除所有表)
mvn flyway:clean
```

### 端口管理

```bash
# 查看8081端口占用情况
netstat -ano | findstr :8081

# 强制停止占用8081端口的进程
taskkill /F /PID <进程ID>
```

### 远程数据库操作

```bash
# 通过SSH连接MySQL (密码: root)
ssh root@192.168.248.208 "docker exec -it mysql57 mysql -uroot -proot mcp_registry"

# 查看所有表
ssh root@192.168.248.208 "docker exec mysql57 mysql -uroot -proot mcp_registry -e 'SHOW TABLES;'"

# 查询示例数据
ssh root@192.168.248.208 "docker exec mysql57 mysql -uroot -proot mcp_registry -e 'SELECT id, tool_name, provider_id FROM mcp_tool LIMIT 5;'"
```

## 项目架构

### 代码结构

```
cn.com.wind.mcp.registry
├── entity/              # 实体类 (对应数据库表)
├── mapper/              # MyBatis Plus Mapper接口
├── service/             # 业务逻辑层
│   └── impl/           # 服务实现类
├── controller/          # Web控制器
├── dto/                # 数据传输对象
├── config/             # 配置类
├── interceptor/        # 拦截器(登录验证等)
└── util/               # 工具类
```

### 核心实体关系 (ER模型)

```
Provider (提供者/用户)
  ├── 1:N → McpTool (MCP工具主表)
  ├── 1:N → OriginToolHttp (原始HTTP工具)
  ├── 1:N → OriginToolExpo (原始Expo工具)
  └── 1:N → OriginProviderConfig (应用服务节点)

McpTool
  ├── tool_num → OriginToolHttp (通过provider_tool_num关联)
  ├── tool_num → OriginToolExpo (通过provider_tool_num关联)
  ├── tool_num → HttpTemplateConverter (HTTP转换模板)
  └── tool_num → ExpoTemplateConverter (Expo转换模板)

OriginToolHttp/OriginToolExpo
  └── provider_app_num → OriginProviderConfig.app_num (应用节点)
```

**关键字段说明**:

- `tool_num`: 工具编号,用于关联MCP工具和原始工具 (时间戳生成)
- `provider_tool_num`: 提供者工具编号,原始工具的唯一标识
- `convert_type`: 转换类型 ("http" / "expo" / "code")
- `provider_id`: 提供者ID,用于权限控制和数据隔离
- `app_num`: 应用编号,关联OriginProviderConfig的唯一标识

### 关键架构理解

#### 1. 工具转换流程 (核心业务逻辑)

系统最核心的功能是将**原始工具**转换为**标准化的MCP工具**:

```
┌──────────────────┐
│  原始工具录入    │ (步骤1-2: 可选)
│  - HTTP接口      │ origin_tool_http表
│  - Expo接口      │ origin_tool_expo表
└────────┬─────────┘
         │ provider_tool_num (关联)
         ↓
┌──────────────────┐
│  MCP工具信息     │ (步骤3: 必填)
│  - 标准化字段    │ mcp_tool表
│  - JSON Schema   │
└────────┬─────────┘
         │ tool_num (关联)
         ↓
┌──────────────────┐
│  转换模板        │ (步骤4: 必填)
│  - HTTP: Jinja2  │ http_template_converter表
│  - Expo: 参数映射│ expo_template_converter表
└──────────────────┘
```

**两种录入模式** (ToolWizardController.saveUnified):

**完整流程**:

1. 选择原始工具类型 (HTTP/Expo)
2. 录入原始工具信息 → 保存到origin_tool_http/expo
3. 录入MCP工具信息 → 保存到mcp_tool
4. 配置转换模板 → 保存到对应的converter表

**简化流程**:

- 跳过步骤1-2,直接录入MCP工具 + 转换模板
- 适用于不需要记录原始工具信息的场景

**转换类型说明**:

- `http`: 使用HttpTemplateConverter将HTTP接口转换为MCP工具 (支持Jinja2模板)
- `expo`: 使用ExpoTemplateConverter将Expo接口转换为MCP工具 (参数映射)
- `code`: 直接代码实现的工具 (不需要转换器)

#### 2. 多表联合编辑 (McpToolController.saveApi)

这是系统的**核心设计模式**之一,实现了McpTool、OriginToolHttp和HttpTemplateConverter三张表的原子性编辑:

```java
// 使用McpToolEditDto封装三张表的数据
@PostMapping("/api/save")
public ResponseEntity<String> saveApi(@RequestBody McpToolEditDto toolDto) {
    // 1. 保存MCP工具基本信息
    // 2. 保存/更新原始HTTP工具
    // 3. 保存/更新转换器
}
```

**特点**:

- 自动处理新增/更新逻辑 (根据ID判断)
- 通过`tool_num`关联三张表
- 带验证的保存 (`saveOrUpdateWithValidation`)
- 支持部分更新 (只更新提供的字段)

#### 3. 权限控制机制

**实现**: `PermissionUtil`工具类 + Session管理

```java
// 权限检查核心逻辑
public static boolean hasPermission(HttpSession session, Long toolProviderId) {
    Provider currentProvider = getCurrentProvider(session);
    return currentProvider != null &&
        currentProvider.getId().equals(toolProviderId);
}
```

**应用场景**:

- **编辑**: 只能编辑自己创建的工具 (`provider_id`匹配)
- **删除**: 只能删除自己的工具
- **查询**: 默认只显示当前用户的工具列表 (通过QueryWrapper过滤)

**拦截器** (`LoginInterceptor`):

- 拦截所有请求 (`/**`)
- 放行登录/注册页面、静态资源 (/css, /js, /static等)
- 未登录用户重定向到`/provider/login`
- Ajax请求返回JSON格式的未授权响应 (判断Accept头)

#### 4. 用户认证流程

**注册** (`ProviderServiceImpl.register`):

```java
// 1. 生成盐值和加密密码
String salt = RandomUtil.randomString(16); // 16位随机字符串
String encodedPassword = DigestUtil.md5Hex(password + salt); // MD5(password + salt)

// 2. 生成API密钥 (用于API认证)
String apiKey = "mcp_" + DigestUtil.md5Hex(...).

substring(0,24);

String apiSecret = DigestUtil.sha256Hex(...).

substring(0,32);

// 3. 保存到数据库
provider.

setPassword(encodedPassword);
provider.

setSalt(salt);
provider.

setStatus(1); // 启用状态
```

**登录** (`ProviderServiceImpl.login`):

```java
// 1. 查询用户
Provider provider = lambdaQuery()
        .eq(Provider::getUsername, username)
        .one();

// 2. 验证密码
String inputEncoded = DigestUtil.md5Hex(password + provider.getSalt());
if(inputEncoded.

equals(provider.getPassword())){
    // 3. 更新最后登录时间
    provider.

setLastLoginTime(LocalDateTime.now());

updateById(provider);

// 4. 保存到Session
    session.

setAttribute("currentProvider",provider);
}
```

**安全建议**: 当前使用MD5加密,建议升级到BCrypt以提升安全性。

## 开发约定

### 代码约定

**实体类**:

- 使用`@TableId(type = IdType.AUTO)`自增主键
- 使用Lombok注解: `@Data`, `@TableName`, `@Accessors(chain = true)`
- 继承`Model<T>`以支持ActiveRecord模式
- 字段命名使用驼峰,自动映射到数据库下划线字段

**服务层**:

- Service接口 + ServiceImpl实现类结构
- 使用`@Slf4j`记录日志
- 关键业务操作添加事务控制`@Transactional`
- 使用Hutool工具类进行通用操作

**控制器层**:

- 返回Thymeleaf视图名或`ResponseEntity<T>`
- 使用`@Slf4j`记录关键操作
- 所有公共方法添加JavaDoc注释
- 权限检查使用`PermissionUtil.hasPermission()`

**数据库迁移** (Flyway):

- Flyway脚本位于`src/main/resources/db/migration/`
- 命名规范: `V{version}__{description}.sql`
- 创建时间字段: `create_time`, `last_update_time`
- 创建人字段: `create_by`, `update_by`
- 级联删除外键约束: `ON DELETE CASCADE`
- 当前Flyway已禁用,迁移需手动执行

**日志规范**:

- 业务层DEBUG级别: `cn.com.wind.mcp.registry: debug`
- SQL追踪TRACE级别: `cn.com.wind.mcp.registry.mapper: trace`
- 关键操作使用`log.info`,警告使用`log.warn`,异常使用`log.error(msg, e)`

**前端约定**:

- Bootstrap 5响应式布局
- 统一导航: `fragments/navigation.html`
- 统一布局: `layout/base.html`
- AJAX返回格式: `{success: boolean, message: string, data: object}`

## 环境配置

### 关键信息

**应用访问**:

- URL: http://localhost:8081
- 测试账号: testtest / testtest

**远程数据库** (MySQL 8.0在Docker中):

- 主机: 192.168.248.208:3306
- 数据库: mcp_registry
- 账号: root / root
- 字符集: utf8mb4_unicode_ci

**远程服务器** (SSH):

- 主机: 192.168.248.208:22
- 账号: root / centos7
- Docker容器: mysql57 (MySQL), redis (Redis), mongo (MongoDB)

### Docker远程连接

```bash
# 创建并切换到远程Docker context
"D:\docker\docker.exe" context create remote --docker "host=tcp://192.168.248.208:2375"
"D:\docker\docker.exe" context use remote

# 验证连接
"D:\docker\docker.exe" ps

# 查看MySQL日志
"D:\docker\docker.exe" logs mysql57 --tail 50
```

### SSH连接和数据库操作

```bash
# SSH连接远程服务器
ssh root@192.168.248.208

# 远程Docker操作
ssh root@192.168.248.208 "docker ps"

# 进入MySQL容器
ssh root@192.168.248.208 "docker exec -it mysql57 mysql -uroot -proot mcp_registry"
```

## Bug修复验证流程

修复Bug后**必须**按以下步骤验证:

1. 重新编译: `mvn clean compile`
2. 运行测试: `mvn test`
3. 启动应用: `mvn spring-boot:run`
4. 使用MCP Playwright工具进行端到端测试验证
5. 手动验证: 访问 http://localhost:8081, 登录并测试相关功能

## 测试指南

### 单元测试

测试类位于`src/test/java/cn/com/wind/mcp/registry/`目录:

- **Mapper测试**: `mapper/*MapperTest.java` - 数据库访问层测试
- **Service测试**: `service/*ServiceTest.java` - 业务逻辑层测试
- **Controller测试**: `controller/*ControllerTest.java` - Web层测试 (使用MockMvc)

**测试环境配置**:

- 使用H2内存数据库,配置在`src/test/resources/application.yml`
- 使用`@SpringBootTest`和`@SpringJUnitConfig`注解
- 使用`@Transactional`自动回滚,保证测试独立性
- 使用`@ActiveProfiles("jenkins")`激活测试配置

**测试数据准备**:

- 每个测试方法独立创建数据
- 使用时间戳生成唯一的`tool_num`和`provider_tool_num`
- 避免硬编码ID,使用自增主键

**运行测试**:

```bash
# 运行所有测试 (忽略失败)
mvn test -Dmaven.test.failure.ignore=true

# 运行单个测试类
mvn test -Dtest=McpToolServiceTest

# 运行特定测试方法
mvn test -Dtest=McpToolServiceTest#testSaveOrUpdateWithValidation

# 查看测试结果摘要
mvn test -Dmaven.test.failure.ignore=true 2>&1 | grep -E '(Tests run:|BUILD)'
```

### 端到端测试 (E2E)

使用MCP Playwright工具进行浏览器自动化测试:

**测试流程**:

```javascript
// 1. 导航到应用
mcp__playwright__browser_navigate({url: "http://localhost:8081"})

// 2. 拍摄快照 (获取页面元素)
mcp__playwright__browser_snapshot()

// 3. 点击登录按钮
mcp__playwright__browser_click({element: "登录按钮", ref: "..."})

// 4. 填写表单
mcp__playwright__browser_fill_form({
    elements: [
        {name: "用户名", ref: "...", value: "testtest"},
        {name: "密码", ref: "...", value: "testtest"}
    ]
})

// 5. 提交表单
mcp__playwright__browser_click({element: "提交按钮", ref: "..."})
```

**测试场景**:

- 用户登录注册流程
- 工具创建和编辑 (完整流程和简化流程)
- 多表编辑功能验证
- 权限控制验证 (尝试编辑他人工具)
- 应用节点管理

## 重要数据库表结构

### mcp_tool (MCP工具主表)

```sql
- id: 主键(自增)
- tool_num: 工具编号 (关联原始工具,时间戳生成)
- tool_version: 工具版本号
- valid: 有效标志 ('1'有效, '0'无效)
- tool_name: MCP工具英文名 (必须符合规范: 字母
、数字
、下划线)
- tool_description: 工具描述
- name_display: 多语言显示名称 (TEXT,JSON格式)
- description_display: 多语言显示描述 (TEXT,JSON格式)
- input_schema: 输入JSON Schema (TEXT,包含uniqueId)
- output_schema: 输出JSON Schema (TEXT)
- convert_type: 转换类型 ('http' / 'expo' / 'code')
- tool_type: 工具类型 ('1':tool, '2':agent)
- provider_id: 提供者ID (外键,用于权限控制)

唯一约束: uk_tool_num_valid_version (tool_num, valid, tool_version)
```

### origin_tool_http (原始HTTP工具)

```sql
- id: 主键(自增)
- provider_tool_num: 提供者工具编号 (唯一约束,用于关联mcp_tool)
- name_display: 工具名称
- desc_display: 工具描述
- req_url: HTTP请求URL
- req_method: HTTP请求方法 (GET/POST/PUT/DELETE等)
- req_headers: 请求头 (TEXT,JSON格式)
- input_schema: 输入JSON Schema (JSON类型)
- output_schema: 输出JSON Schema (JSON类型)
- provider_app_num: 关联应用服务节点 (origin_provider_config.app_num)
- provider_id: 提供者ID (外键)

唯一约束: uk_provider_tool_num_http
```

### http_template_converter (HTTP转换模板)

```sql
- tool_num: 关联MCP工具 (外键)
- tool_version: 工具版本
- req_url: URL模板 (Jinja2语法)
- req_method: 请求方法
- req_headers: 请求头模板 (TEXT,Jinja2语法)
- req_body: 请求体模板 (TEXT,Jinja2语法)
- resp_body: 响应体映射模板 (TEXT,Jinja2语法)
- provider_tool_num: 提供者工具编号
```

### provider (提供者/用户表)

```sql
- id: 主键(自增)
- username: 用户名 (唯一约束)
- password: MD5加密密码
- salt: 16位随机盐值
- email: 邮箱
- phone_number: 手机号
- api_key: API密钥 (唯一约束,格式: "mcp_" + 24位)
- api_secret: API密钥 (32位)
- status: 状态 (-1删除, 0禁用, 1启用)
- last_login_time: 最后登录时间
```

### origin_provider_config (应用服务节点)

```sql
- app_num: 应用编号 (关联origin_tool_http/expo的provider_app_num)
- provider_id: 提供者ID (外键)
- app_name: 应用名称 (用于负载均衡器分组)
- site_type: 站点类型 (测试站/河西/外高桥等)
- app_ip: 应用IP地址
- app_port: 应用端口
- load_factor: 负载权重因子 (默认1.0)
- is_enabled: 是否启用 (1启用, 0禁用)
- health_check_url: 健康检查URL (可选)
- health_check_interval: 健康检查间隔秒数 (可选)
```

## 开发建议和最佳实践

### 新功能开发流程

1. **数据库设计**: 编写Flyway迁移脚本 (`src/main/resources/db/migration/V*__*.sql`)
2. **实体类**: 创建Entity类,继承`Model<T>`,添加MyBatis Plus注解
3. **Mapper接口**: 创建Mapper接口,继承`BaseMapper<T>`
4. **Service层**:
    - 定义Service接口,继承`IService<T>`
    - 实现ServiceImpl类,继承`ServiceImpl<Mapper, Entity>`
    - 添加业务逻辑和验证 (使用`@Transactional`控制事务)
5. **Controller层**:
    - 创建Controller类,添加路由和方法
    - 添加权限检查 (`PermissionUtil.hasPermission()`)
    - 添加详细的JavaDoc注释和日志记录
6. **前端页面**:
    - 创建Thymeleaf模板,引用统一布局 (`layout/base.html`)
    - 引用统一导航 (`fragments/navigation.html`)
    - 使用Bootstrap 5样式
7. **测试**:
    - 编写Mapper测试 (验证数据库访问)
    - 编写Service测试 (验证业务逻辑)
    - 编写Controller测试 (验证Web层,使用MockMvc)
    - 端到端测试 (验证完整流程,使用MCP Playwright)

### 编码规范

**命名约定**:

- Entity: 单数名词,如`McpTool`
- Mapper: `EntityMapper`,如`McpToolMapper`
- Service: `EntityService`,如`McpToolService`
- Controller: `EntityController`,如`McpToolController`
- 方法: 动词开头,如`saveTool()`, `findByUsername()`

**注释规范**:

```java
/**
 * 保存工具 - JSON API (支持多表编辑)
 *
 * @param toolDto 工具编辑DTO
 * @param session HTTP会话
 * @return 操作结果 (成功/失败消息)
 */
@PostMapping("/api/save")
public ResponseEntity<String> saveApi(
    @RequestBody McpToolEditDto toolDto,
    HttpSession session) {
    // 实现逻辑
}
```

### 常见问题和解决方案

**Q: 启动时提示端口8081已被占用**

```bash
# 查看占用进程
netstat -ano | findstr :8081

# 强制停止Java进程
taskkill /F /IM java.exe

# 或停止特定进程ID
taskkill /F /PID <进程ID>
```

**Q: 数据库连接失败**

- 检查远程MySQL服务是否运行: `ssh root@192.168.248.208 "docker ps | grep mysql57"`
- 检查网络连接: `ping 192.168.248.208`
- 检查数据库配置: `application.yml`中的`spring.datasource.url`

**Q: Flyway迁移失败**

- 当前Flyway已禁用,如需启用,修改`application.yml`中的`spring.flyway.enabled=true`
- 手动执行迁移脚本: `ssh root@192.168.248.208 "docker exec mysql57 mysql -uroot -proot mcp_registry < migration.sql"`

**Q: 测试失败**

- 检查H2数据库配置: `src/test/resources/application.yml`
- 确保使用`@Transactional`自动回滚
- 使用`-Dmaven.test.failure.ignore=true`忽略失败继续测试

### 性能优化建议

1. **数据库查询优化**:
    - 为常用查询字段添加索引 (provider_id, tool_num, username等)
    - 使用分页查询避免全表扫描 (`IPage<T>`)
    - 避免N+1查询问题,使用MyBatis Plus的关联查询

2. **缓存策略**:
    - 考虑引入Redis缓存热点数据 (工具列表、用户信息)
    - 使用Spring Cache抽象简化缓存操作
    - 设置合理的缓存过期时间

3. **安全加固**:
    - 升级密码加密算法: MD5 → BCrypt
    - 添加API请求频率限制 (防止暴力破解)
    - 实现CSRF防护 (Spring Security)
    - 添加审计日志记录关键操作

---

**文档维护者**: Claude Code
**最后更新**: 2025-10-15
**项目版本**: v1.3.0
