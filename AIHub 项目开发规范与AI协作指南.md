# AIHub 项目开发规范与AI协作指南

**核心目标**: 为 `AIHub` 项目建立统一、精确的开发标准和协作规范。本文档是根据当前项目状态生成的，请在后续所有开发任务中严格遵守。

API服务器接口文档目录
http://localhost:8080/swagger-ui/index.html
---

### 1. 项目概览

*   **项目名称**: AIHub
*   **技术栈**:
    *   **后端框架**: Spring Boot 3.3.6
    *   **语言**: Java 17
    *   **构建工具**: Maven
    *   **数据库**: MySQL, MongoDB
    *   **持久层**: MyBatis-Plus 3.5.7, Spring Data MongoDB
    *   **API文档**: SpringDoc 2.4.0
    *   **安全认证**: Spring Security, JWT (jjwt 0.11.5)
    *   **其他**: Spring Data Redis, Spring WebSocket
*   **开发语言**: 所有代码、注释、API文档、日志信息和用户可见的文本信息，**必须使用中文**。

### 2. 代码风格与格式

*   **构建命令**: 始终使用项目自带的Maven Wrapper进行构建，例如 `./mvnw clean install`。
*   **代码格式化**: 缩进4个空格，左大括号 `{` 不换行。
*   **命名规范**:
    *   **包 (Package)**: 全小写，遵循 `com.example.aihub.功能层` 结构。
    *   **类/接口 (Class/Interface)**: 大驼峰命名法 (PascalCase)，如 `UsersService`。
    *   **方法/变量 (Method/Variable)**: 小驼峰命名法 (camelCase)，如 `getUserById`。
    *   **常量 (Constant)**: 全大写，下划线分隔 (UPPER_SNAKE_CASE)，如 `MAX_LOGIN_ATTEMPTS`。
*   **注释**: 
    *   **类和接口**: 所有公开的类和接口都必须有 Javadoc 注释，说明其用途和职责。
    *   **方法**: 所有公开的方法都必须有 Javadoc 注释，清晰地描述方法的功能、参数 (`@param`)、返回值 (`@return`) 和可能抛出的异常 (`@throws`)。
    *   **实现逻辑**: 在复杂的业务逻辑实现内部，应添加必要的行内或块注释，解释"为什么这么做"，而不是"做了什么"。

### 3. 项目架构

项目采用分层架构，各层职责清晰，请严格遵循：

*   `config`: Spring的配置类，如 `SecurityConfig`, `MybatisPlusConfig`。
*   `controller`: **控制器层**。负责接收HTTP请求，调用`Service`层，并返回统一的 `Result` 对象。**禁止包含业务逻辑**。
*   `service`: **服务接口层**。定义核心业务接口。
*   `service.impl`: **服务实现层**。处理具体业务逻辑，包括事务管理。
*   `mapper`: **(MySQL)数据访问层**。存放MyBatis-Plus的Mapper接口，专门用于操作MySQL数据库。
*   `repository`: **(MongoDB)数据仓库层**。存放Spring Data MongoDB的Repository接口，专门用于操作MongoDB数据库。
*   `entity`: **实体类层**。与数据库表或MongoDB集合对应。MySQL实体必须继承`BaseEntity`。
*   `dto`: **数据传输对象层**。用于封装客户端请求和服务器响应的数据，如 `LoginRequest`, `RegisterRequest`。
*   `common`: **通用模块**。存放`Result`, `ResultCode`, `IErrorCode`等全局通用类。
*   `util`: **工具类层**。存放可复用的工具类，如`JwtUtil`。

### 4. API 设计与文档 (SpringDoc)

*   **统一响应格式**: 所有Controller接口必须返回 `com.example.aihub.common.Result<T>`。
    *   成功: `Result.success(data, "操作成功")`
    *   失败: `Result.failed("错误信息")`
*   **RESTful 风格**: 
    *   所有API路径不需要`/api开头` 作为前缀
    *   使用名词复数表示资源路径 (`/users`)。
    *   使用正确的HTTP动词 (`GET`, `POST`, `PUT`, `DELETE`) 表达操作。
*   **API文档**: 所有Controller和公开API必须有完整的SpringDoc注解。
    *   `@Tag`: 用于Controller类。
    *   `@Operation`: 用于Controller方法。
    *   `@Schema`: 用于DTO和实体类。
    *   示例 (`AuthController.java`):
        ```java
        @Tag(name = "用户认证", description = "提供用户登录和注册功能")
        @RestController
        public class AuthController {
            @Operation(summary = "用户登录", description = "用户使用用户名和密码进行登录认证，成功后返回JWT")
            public Result<Map<String, String>> login(@RequestBody LoginRequest loginRequest) { /* ... */ }
        }
        ```
*   **API响应行为规范**: 为确保API的健壮性和一致性，所有Controller在处理查询结果时必须遵循以下规范：
    1.  **查询单个资源**: 当根据ID等唯一条件查询单个资源时，Controller必须对Service层返回的结果进行`null`检查。
        *   **若结果不为`null`**: 返回 `Result.success(data)`。
        *   **若结果为`null`**: 必须返回业务失败的`Result`对象，如 `Result.failed("资源不存在")`，**严禁**因空指针等原因导致返回500错误。
    2.  **查询资源列表**: 当查询资源列表时，无论结果中是否包含数据，都应视为成功操作。
        *   **若结果不为空**: 返回 `Result.success(list)`，`data`字段为包含数据的数组。
        *   **若结果为空**: 同样返回 `Result.success(emptyList)`，`data`字段为一个空数组 `[]`。**严禁**将"列表为空"视为一种失败。

### 5. 持久层规范

项目同时使用MySQL和MongoDB作为数据存储。

#### 5.1 MySQL & MyBatis-Plus

*   **适用场景**: 结构化数据、需要事务一致性的核心业务数据（如用户信息、课程信息等）。
*   **统一基类**: 所有与MySQL表对应的实体类都必须继承 `BaseEntity`，以获得 `id`, `createTime`, `updateTime`, `deleted` 四个基础字段。
*   **自动填充**: `createTime`, `updateTime`, `deleted` 字段由 `MyBatisPlusMetaObjectHandler` 自动处理，业务代码无需手动设置。
*   **逻辑删除**: 已全局配置逻辑删除。所有删除操作均为更新 `deleted` 标志，所有查询会自动过滤已删除数据。`AIhub.sql`中所有表均已适配此规范。
*   **SQL规范**: `AIhub.sql` 中所有表字段使用下划线命名法 (`user_code`)，实体类中对应小驼峰命名法 (`userCode`)，由MyBatis-Plus自动映射。

#### 5.2 MongoDB & Spring Data MongoDB

*   **适用场景**: 非结构化或半结构化数据、日志记录、需要频繁写入且对事务要求不高的数据。
*   **实体类**: MongoDB实体类（POJO）应使用 `@Document(collection = "...")` 注解指定其集合名称。
*   **主键**: 使用 `@Id` 注解标记主键字段，通常为 `String` 类型。
*   **数据仓库接口**:
    *   所有MongoDB的数据访问接口应继承 `org.springframework.data.mongodb.repository.MongoRepository`。
    *   这些接口必须存放在 `com.example.aihub.repository` 包下，以避免与MyBatis的Mapper扫描冲突。

### 6. 安全认证 (Spring Security & JWT)

*   **认证流程**:
    1.  客户端调用 `/auth/login` 或 `/auth/register` 接口。
    2.  登录成功后，服务端返回JWT。
    3.  客户端在后续请求的 `Authorization` Header中携带 `Bearer <token>`。
*   **核心组件**:
    *   **`SecurityConfig`**: 配置HTTP安全规则、密码编码器 (`BCryptPasswordEncoder`) 和公开访问路径。
    *   **`JwtAuthenticationFilter`**: 在每个请求中校验JWT的有效性。
    *   **`UserDetailsServiceImpl`**: 连接Spring Security与`UsersService`，用于加载用户认证信息。
*   **路径豁免**: 无需认证的路径（如登录/注册、Swagger文档）已在 `SecurityConfig` 中通过 `.requestMatchers()` 配置。
*   **获取当前登录用户信息**: 在Service层中，如果需要获取当前登录用户的完整信息（如用户ID、角色等），必须遵循以下步骤：
    1.  从`SecurityContextHolder`获取`UserDetails`主体。
    2.  从`UserDetails`中获取用户名。
    3.  通过注入的`UsersService`使用该用户名从数据库中查询完整的`Users`实体。
    *   示例 (`CoursesServiceImpl.java`):
        ```java
        @Autowired
        private UsersService usersService;

        public void someBusinessMethod() {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = ((UserDetails) principal).getUsername();
            Users currentUser = usersService.findByUsername(username);
            // 现在可以使用currentUser对象了，例如: currentUser.getId()
        }
        ```

### 7. 代码生成器

*   项目在 `src/test/java/com/example/aihub/CodeGenerator.java` 中提供了代码生成器。
*   **用途**: 用于快速生成新数据库表的`Entity`, `Mapper`, `Service`, `Controller`代码。
*   **配置**: 已配置为适配本项目的规范，如：
    *   继承 `BaseEntity`。
    *   启用Lombok和SpringDoc。
    *   数据库字段下划线到实体类驼峰的命名策略。
*   **使用**: 直接运行`main`方法，按提示输入表名即可。

### 8. 日志规范 (SLF4J & Logback)

*   **技术栈**: 项目使用 `SLF4J` 作为日志门面，`Logback` 作为日志实现，通过 `logback-spring.xml`进行配置。
*   **如何记录日志**:
    1.  在需要记录日志的类上添加 Lombok 的 `@Slf4j` 注解。
    2.  调用 `log` 对象提供的不同级别的方法记录日志。
        ```java
        @Service
        @Slf4j
        public class MyService {
            public void myMethod(String param) {
                log.debug("方法开始，接收到参数: {}", param);
                try {
                    // ... 业务逻辑 ...
                    log.info("业务逻辑处理成功");
                } catch (Exception e) {
                    log.error("业务逻辑发生错误，参数: {}", param, e); // 最后一个参数传入异常对象，会打印堆栈信息
                }
            }
        }
        ```
*   **日志级别选择**:
    *   `log.trace()`: 追踪级别，记录非常详细的流程信息，生产环境通常关闭。
    *   `log.debug()`: 调试级别，用于开发过程中定位问题，记录方法入参、关键变量等。
    *   `log.info()`: 信息级别，记录关键的业务流程节点、系统状态变化等。这是生产环境推荐的默认级别。
    *   `log.warn()`: 警告级别，记录可预期的、不会影响系统运行但需要关注的潜在问题。
    *   `log.error()`: 错误级别，记录所有影响系统正常功能的异常情况。**必须附带异常对象**。

---

**请在开始新的开发任务时，以此规范为准则。** 