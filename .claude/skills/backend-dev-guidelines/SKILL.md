# Java/Spring Boot 后端开发指南

> **技能类型**: 开发规范和最佳实践  
> **适用范围**: mindtrip-module-*、mindtrip-framework、mindtrip-server  
> **技术栈**: Java 8+, Spring Boot, MyBatis-Plus, Redis, MySQL

---

## 📋 目录

本技能提供完整的 Java/Spring Boot 后端开发规范，采用模块化设计，按需加载：

### 核心模块
- [架构概览](#架构概览) - 项目整体架构说明
- [分层设计规范](#分层设计规范) - Controller/Service/Mapper 分层
- [代码风格](#代码风格) - 命名、注释、格式化

### 详细指南（按需查看资源文件）
- `01-controller-guidelines.md` - Controller 层开发规范
- `02-service-guidelines.md` - Service 层业务逻辑规范
- `03-mapper-guidelines.md` - Mapper/DAO 层数据访问规范
- `04-entity-dto-vo.md` - 实体类、DTO、VO 设计规范
- `05-exception-handling.md` - 异常处理和错误码规范
- `06-validation.md` - 参数校验规范
- `07-redis-usage.md` - Redis 缓存使用规范
- `08-transaction.md` - 事务管理规范
- `09-logging.md` - 日志规范
- `10-testing.md` - 单元测试和集成测试

---

## 架构概览

### 项目结构

```
lvye-project/
├── mindtrip-framework/           # 框架核心
│   ├── mindtrip-common/         # 通用工具和基础类
│   ├── mindtrip-spring-boot-starter-*  # 各种 Starter
├── mindtrip-module-*/           # 业务模块
│   └── src/main/java/
│       └── com.lvye.mindtrip/module/[module]/
│           ├── controller/   # 控制器层
│           │   └── [admin|app]/  # 管理端/用户端
│           ├── service/      # 业务逻辑层
│           │   ├── [Service].java      # 接口
│           │   └── [ServiceImpl].java  # 实现
│           ├── dal/          # 数据访问层
│           │   ├── dataobject/  # DO (数据库实体)
│           │   └── mysql/       # MyBatis Mapper
│           ├── convert/      # 对象转换器
│           ├── enums/        # 枚举类
│           └── vo/           # VO (Value Object)
│               ├── [ReqVO].java   # 请求参数
│               └── [RespVO].java  # 响应参数
└── mindtrip-server/             # 服务启动入口
```

### 技术栈

- **框架**: Spring Boot 2.x
- **ORM**: MyBatis-Plus 3.x
- **缓存**: Redis (Spring Data Redis)
- **数据库**: MySQL 8.0
- **校验**: Hibernate Validator
- **API 文档**: Swagger/Knife4j
- **工具**: Lombok, MapStruct, Hutool

---

## 分层设计规范

### 四层架构

#### 1. Controller 层
- **职责**: 接收请求、参数校验、调用 Service、返回响应
- **原则**: 薄控制器，不包含业务逻辑
- **命名**: `[Entity]Controller.java`
- **详细**: 查看 `01-controller-guidelines.md`

#### 2. Service 层
- **职责**: 业务逻辑处理、事务控制、调用 Mapper
- **原则**: 单一职责、可复用、可测试
- **命名**: `[Entity]Service.java` (接口) + `[Entity]ServiceImpl.java` (实现)
- **详细**: 查看 `02-service-guidelines.md`

#### 3. Mapper 层
- **职责**: 数据访问、SQL 操作
- **原则**: 只做数据访问，不包含业务逻辑
- **命名**: `[Entity]Mapper.java` + `[Entity]Mapper.xml`
- **详细**: 查看 `03-mapper-guidelines.md`

#### 4. Convert 层
- **职责**: 对象转换 (DO ↔ VO ↔ DTO)
- **工具**: MapStruct
- **命名**: `[Entity]Convert.java`

### 对象类型

| 类型 | 全称 | 用途 | 位置 |
|-----|------|------|------|
| DO | Data Object | 数据库实体 | dal/dataobject/ |
| VO | Value Object | 视图对象（请求/响应） | vo/ |
| DTO | Data Transfer Object | 服务间传输 | dto/ |
| BO | Business Object | 业务对象（可选） | bo/ |

---

## 代码风格

### 命名规范

#### 类命名
```java
// Controller
[Entity]Controller          // 用户控制器: UserController
[Entity]AdminController     // 管理端: UserAdminController

// Service
[Entity]Service            // 接口
[Entity]ServiceImpl        // 实现

// Mapper
[Entity]Mapper             // UserMapper

// VO
[Entity]CreateReqVO        // 创建请求
[Entity]UpdateReqVO        // 更新请求
[Entity]PageReqVO          // 分页查询请求
[Entity]RespVO             // 响应
[Entity]ExcelVO            // Excel 导入导出
```

#### 方法命名
```java
// Controller 方法 - 对应 HTTP 方法
create[Entity]()           // POST   - 创建
update[Entity]()           // PUT    - 更新
delete[Entity]()           // DELETE - 删除
get[Entity]()              // GET    - 查询单个
get[Entity]Page()          // GET    - 分页查询
list[Entity]()             // GET    - 列表查询

// Service 方法 - 业务含义清晰
validate[Entity]Exists()   // 校验是否存在
validate[Entity]Duplicate() // 校验重复
checkPermission()          // 权限检查
```

### 注释规范

```java
/**
 * 用户服务接口
 *
 * @author 你的名字
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param createReqVO 创建信息
     * @return 用户编号
     */
    Long createUser(UserCreateReqVO createReqVO);
    
    /**
     * 更新用户
     *
     * @param updateReqVO 更新信息
     */
    void updateUser(UserUpdateReqVO updateReqVO);
}
```

### 常量定义

```java
// 使用 interface 定义常量（推荐）
public interface UserConstants {
    /**
     * 用户类型 - 普通用户
     */
    Integer TYPE_NORMAL = 1;
    
    /**
     * 用户类型 - VIP 用户
     */
    Integer TYPE_VIP = 2;
}

// 或使用枚举（更推荐）
@Getter
@AllArgsConstructor
public enum UserTypeEnum {
    NORMAL(1, "普通用户"),
    VIP(2, "VIP用户");
    
    private final Integer type;
    private final String name;
}
```

---

## 快速参考

### 常见操作

#### 1. 创建新的 CRUD 接口
```java
// 1. 定义 DO (dal/dataobject/)
// 2. 定义 VO (vo/)
// 3. 创建 Mapper (dal/mysql/)
// 4. 创建 Service 接口和实现 (service/)
// 5. 创建 Controller (controller/)
// 6. 创建 Convert (convert/)
```

#### 2. 添加字段
```java
// 1. 修改数据库表（执行 SQL）
// 2. 修改 DO 类
// 3. 修改相关 VO 类
// 4. 修改 Convert 转换
// 5. 更新业务逻辑
```

#### 3. 添加业务逻辑
```java
// 1. 在 Service 接口定义方法
// 2. 在 ServiceImpl 实现
// 3. 如需暴露接口，在 Controller 添加
// 4. 编写单元测试
```

---

## 关键原则

### ✅ 遵循的原则

1. **单一职责**: 每个类、方法只做一件事
2. **分层清晰**: 严格遵守分层，不跨层调用
3. **面向接口**: Service 层使用接口 + 实现
4. **参数校验**: Controller 层必须校验参数
5. **异常统一**: 使用项目统一的异常类
6. **事务控制**: Service 层添加 `@Transactional`
7. **日志规范**: 关键操作必须记录日志
8. **代码复用**: 提取公共逻辑到工具类或基类

### ❌ 避免的做法

1. **不要在 Controller 写业务逻辑**
2. **不要在 Mapper 写业务逻辑**
3. **不要跨层调用** (如 Controller 直接调 Mapper)
4. **不要暴露 DO 对象给前端**
5. **不要使用魔法值** (使用常量或枚举)
6. **不要忽略异常** (至少要记录日志)
7. **不要过度设计** (YAGNI 原则)

---

## 资源文件索引

当你需要深入了解某个主题时，参考对应的资源文件：

| 场景 | 资源文件 |
|------|----------|
| 开发 RESTful API | `01-controller-guidelines.md` |
| 编写业务逻辑 | `02-service-guidelines.md` |
| 数据库操作 | `03-mapper-guidelines.md` |
| 设计数据对象 | `04-entity-dto-vo.md` |
| 处理错误 | `05-exception-handling.md` |
| 参数校验 | `06-validation.md` |
| 使用缓存 | `07-redis-usage.md` |
| 事务管理 | `08-transaction.md` |
| 日志记录 | `09-logging.md` |
| 编写测试 | `10-testing.md` |

---

## 激活此技能

在以下情况下，此技能会自动激活：
- 编辑 Java 文件 (Controller, Service, Mapper)
- 在 mindtrip-module-* 或 mindtrip-framework 目录工作
- 询问关于后端、接口、数据库的问题

手动激活：`@backend-dev-guidelines`

---

> **提示**: 此技能采用模块化设计，核心概览保持在 500 行以内，详细内容分散在资源文件中。
> 这样可以快速加载概览，需要时再查看详细文档，避免上下文过载。

