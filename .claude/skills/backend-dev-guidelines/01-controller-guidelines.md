# Controller 层开发规范

> 资源文件：backend-dev-guidelines/01-controller-guidelines.md

---

## Controller 职责

Controller 层是 **薄控制器**，只负责：
1. ✅ 接收 HTTP 请求
2. ✅ 参数校验（使用 `@Valid`）
3. ✅ 调用 Service 层
4. ✅ 返回统一响应
5. ❌ **不要**包含业务逻辑
6. ❌ **不要**直接操作数据库

---

## 标准模板

### 基础 CRUD Controller

```java
package cn.iocoder.yudao.module.psychology.controller.admin.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.user.vo.*;
import cn.iocoder.yudao.module.psychology.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户管理 Controller
 *
 * @author lvye-project
 */
@Tag(name = "管理后台 - 用户管理")
@RestController
@RequestMapping("/psychology/user")
@Validated
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 创建用户
     */
    @PostMapping("/create")
    @Operation(summary = "创建用户")
    @PreAuthorize("@ss.hasPermission('psychology:user:create')")
    public CommonResult<Long> createUser(@Valid @RequestBody UserCreateReqVO createReqVO) {
        return success(userService.createUser(createReqVO));
    }

    /**
     * 更新用户
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户")
    @PreAuthorize("@ss.hasPermission('psychology:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody UserUpdateReqVO updateReqVO) {
        userService.updateUser(updateReqVO);
        return success(true);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除用户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('psychology:user:delete')")
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return success(true);
    }

    /**
     * 获得用户
     */
    @GetMapping("/get")
    @Operation(summary = "获得用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('psychology:user:query')")
    public CommonResult<UserRespVO> getUser(@RequestParam("id") Long id) {
        return success(userService.getUser(id));
    }

    /**
     * 获得用户分页
     */
    @GetMapping("/page")
    @Operation(summary = "获得用户分页")
    @PreAuthorize("@ss.hasPermission('psychology:user:query')")
    public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO pageReqVO) {
        return success(userService.getUserPage(pageReqVO));
    }

    /**
     * 获得用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "获得用户列表")
    @PreAuthorize("@ss.hasPermission('psychology:user:query')")
    public CommonResult<List<UserRespVO>> getUserList(@Valid UserListReqVO listReqVO) {
        return success(userService.getUserList(listReqVO));
    }

    /**
     * 导出用户 Excel
     */
    @GetMapping("/export-excel")
    @Operation(summary = "导出用户 Excel")
    @PreAuthorize("@ss.hasPermission('psychology:user:export')")
    public void exportUserExcel(@Valid UserExportReqVO exportReqVO,
                                 HttpServletResponse response) throws IOException {
        List<UserExcelVO> list = userService.getUserExcelList(exportReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "用户列表.xls", "数据", UserExcelVO.class, list);
    }
}
```

---

## 关键注解

### 类级别注解

```java
@Tag(name = "管理后台 - 用户管理")  // Swagger 文档标签
@RestController                   // RESTful Controller
@RequestMapping("/psychology/user") // 请求路径
@Validated                        // 启用参数校验
@Slf4j                           // Lombok 日志
```

### 方法级别注解

```java
// HTTP 方法映射
@PostMapping("/create")    // POST 请求
@PutMapping("/update")     // PUT 请求
@DeleteMapping("/delete")  // DELETE 请求
@GetMapping("/get")        // GET 请求

// Swagger 文档
@Operation(summary = "创建用户")           // 接口说明
@Parameter(name = "id", description = "编号") // 参数说明

// 权限控制
@PreAuthorize("@ss.hasPermission('psychology:user:create')")

// 参数校验
@Valid                     // 校验 @RequestBody
@Validated                 // 校验 @RequestParam
```

---

## 参数接收方式

### 1. RequestBody（POST/PUT - JSON）

```java
@PostMapping("/create")
public CommonResult<Long> createUser(@Valid @RequestBody UserCreateReqVO createReqVO) {
    return success(userService.createUser(createReqVO));
}
```

### 2. RequestParam（GET/DELETE - 查询参数）

```java
@DeleteMapping("/delete")
public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
    userService.deleteUser(id);
    return success(true);
}
```

### 3. PathVariable（RESTful 路径参数）

```java
@GetMapping("/get/{id}")
public CommonResult<UserRespVO> getUser(@PathVariable("id") Long id) {
    return success(userService.getUser(id));
}
```

### 4. 分页查询（GET - 对象接收）

```java
@GetMapping("/page")
public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO pageReqVO) {
    return success(userService.getUserPage(pageReqVO));
}
```

---

## 响应规范

### 统一响应格式

```java
// 使用 CommonResult 包装
return success(data);              // 成功响应
return success(true);              // 成功（无数据）
return error(500, "错误信息");      // 错误响应（一般不需要手动处理）
```

### 响应数据类型

```java
// 单个对象
CommonResult<UserRespVO>

// 列表
CommonResult<List<UserRespVO>>

// 分页
CommonResult<PageResult<UserRespVO>>

// ID
CommonResult<Long>

// Boolean
CommonResult<Boolean>

// 无数据
CommonResult<Void>
```

---

## 权限控制

### 使用 @PreAuthorize

```java
// 检查权限
@PreAuthorize("@ss.hasPermission('psychology:user:create')")

// 多个权限（任一）
@PreAuthorize("@ss.hasAnyPermissions('psychology:user:create', 'psychology:user:update')")

// 角色检查
@PreAuthorize("@ss.hasRole('admin')")

// 复杂表达式
@PreAuthorize("@ss.hasPermission('psychology:user:create') and @ss.hasRole('admin')")
```

### 权限标识规范

```
格式: {模块}:{实体}:{操作}

示例:
- psychology:user:create   # 创建用户
- psychology:user:update   # 更新用户
- psychology:user:delete   # 删除用户
- psychology:user:query    # 查询用户
- psychology:user:export   # 导出用户
```

---

## 参数校验

### VO 中定义校验规则

```java
@Data
public class UserCreateReqVO {
    
    @NotBlank(message = "用户名不能为空")
    @Length(min = 4, max = 20, message = "用户名长度为 4-20 位")
    private String username;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;
    
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄必须大于 0")
    @Max(value = 150, message = "年龄必须小于 150")
    private Integer age;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}
```

### Controller 中启用校验

```java
// @RequestBody - 使用 @Valid
public CommonResult<Long> createUser(@Valid @RequestBody UserCreateReqVO createReqVO)

// @RequestParam - 类上添加 @Validated
@Validated
public class UserController {
    public CommonResult<Boolean> deleteUser(@RequestParam("id") @NotNull Long id)
}
```

详细校验规范请参考：`06-validation.md`

---

## 异常处理

### Controller 不需要手动处理异常

```java
// ❌ 不要这样做
@PostMapping("/create")
public CommonResult<Long> createUser(@Valid @RequestBody UserCreateReqVO createReqVO) {
    try {
        return success(userService.createUser(createReqVO));
    } catch (Exception e) {
        log.error("创建用户失败", e);
        return error(500, "创建失败");
    }
}

// ✅ 应该这样做
@PostMapping("/create")
public CommonResult<Long> createUser(@Valid @RequestBody UserCreateReqVO createReqVO) {
    return success(userService.createUser(createReqVO));
}
```

**原因**：
- 全局异常处理器会自动捕获异常
- Service 层抛出业务异常（`ServiceException`）
- 自动转换为统一响应格式

详细异常处理请参考：`05-exception-handling.md`

---

## 日志记录

### 记录关键操作

```java
@PostMapping("/create")
public CommonResult<Long> createUser(@Valid @RequestBody UserCreateReqVO createReqVO) {
    log.info("[createUser][创建用户：{}]", createReqVO);
    return success(userService.createUser(createReqVO));
}

@DeleteMapping("/delete")
public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
    log.info("[deleteUser][删除用户：{}]", id);
    userService.deleteUser(id);
    return success(true);
}
```

### 日志级别

- `log.debug()` - 调试信息（开发环境）
- `log.info()` - 关键操作（创建、更新、删除）
- `log.warn()` - 警告信息（异常情况但不影响流程）
- `log.error()` - 错误信息（异常）

详细日志规范请参考：`09-logging.md`

---

## 常见场景

### 1. 文件上传

```java
@PostMapping("/upload")
@Operation(summary = "上传文件")
public CommonResult<String> uploadFile(@RequestParam("file") MultipartFile file) {
    String url = fileService.uploadFile(file);
    return success(url);
}
```

### 2. 批量操作

```java
@DeleteMapping("/batch-delete")
@Operation(summary = "批量删除用户")
public CommonResult<Boolean> batchDeleteUser(@RequestParam("ids") List<Long> ids) {
    userService.batchDeleteUser(ids);
    return success(true);
}
```

### 3. 导出 Excel

```java
@GetMapping("/export-excel")
@Operation(summary = "导出用户 Excel")
public void exportUserExcel(@Valid UserExportReqVO exportReqVO,
                             HttpServletResponse response) throws IOException {
    List<UserExcelVO> list = userService.getUserExcelList(exportReqVO);
    ExcelUtils.write(response, "用户列表.xls", "数据", UserExcelVO.class, list);
}
```

### 4. 下载文件

```java
@GetMapping("/download/{id}")
@Operation(summary = "下载文件")
public void downloadFile(@PathVariable("id") Long id, HttpServletResponse response) {
    FileInfo fileInfo = fileService.getFile(id);
    // 设置响应头
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition", 
        "attachment; filename=" + URLEncoder.encode(fileInfo.getName(), "UTF-8"));
    // 写入文件
    IoUtil.write(response.getOutputStream(), false, fileInfo.getContent());
}
```

---

## 检查清单

开发 Controller 时，请确保：

- [ ] 类上添加了 `@RestController`、`@RequestMapping`、`@Validated`、`@Tag`
- [ ] 方法上添加了 HTTP 方法注解（`@GetMapping` 等）
- [ ] 方法上添加了 `@Operation` 说明
- [ ] 需要权限的接口添加了 `@PreAuthorize`
- [ ] 参数添加了 `@Valid` 或 `@Validated`
- [ ] 使用了正确的参数注解（`@RequestBody`、`@RequestParam` 等）
- [ ] 返回值使用 `CommonResult` 包装
- [ ] 关键操作记录了日志
- [ ] **没有**在 Controller 中编写业务逻辑
- [ ] **没有**在 Controller 中直接调用 Mapper

---

## 相关资源

- [Service 层规范](02-service-guidelines.md)
- [VO 设计规范](04-entity-dto-vo.md)
- [参数校验规范](06-validation.md)
- [异常处理规范](05-exception-handling.md)
- [日志规范](09-logging.md)

