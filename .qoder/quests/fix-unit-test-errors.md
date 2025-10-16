# 单元测试错误修复设计

## 概述

本设计文档旨在修复lvye-project中的单元测试错误，包括权限服务测试失败和OAuth2令牌服务测试的数据库插入错误。

## 架构

这是一个基于Spring Boot的后端应用，使用MyBatis Plus进行数据持久化，采用H2内存数据库进行单元测试。

## 问题分析

### 1. PermissionServiceTest测试失败

**错误信息:**
```
PermissionServiceTest.testGetDeptDataPermission_DeptAndChild:497 expected: <2> but was: <1>
```

**问题分析:**
- 测试方法`testGetDeptDataPermission_DeptAndChild`期望返回2个部门ID，但实际只返回了1个
- 原因是在`PermissionServiceImpl.getDeptDataPermission`方法的`DEPT_AND_CHILD`分支中，代码错误地调用了`getUserRoleIdListByDeptIdFromCache(userId)`而不是`deptService.getChildDeptIdListFromCache(userDeptId.get())`
- 当前代码返回的是用户的部门ID列表，而不是子部门ID列表

### 2. OAuth2TokenServiceImplTest数据库错误

**错误信息:**
```
org.h2.jdbc.JdbcSQLDataException: Numeric value out of range: "1175630667" in column "is_parent"
```

**问题分析:**
- H2数据库中`is_parent`字段定义为`tinyint`类型
- `randomPojo`生成的随机Integer值超出了tinyint的范围(-128到127)
- 需要在测试中明确设置`is_parent`字段为有效值

## 修复方案

### 1. 修复权限服务测试错误

#### 1.1 修复PermissionServiceImpl代码
在`PermissionServiceImpl.getDeptDataPermission`方法中，修复`DEPT_AND_CHILD`分支的逻辑：

```java
// 情况四，DEPT_DEPT_AND_CHILD
if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_AND_CHILD.getScope())) {
    // 修复：使用正确的方法获取子部门ID列表
    CollUtil.addAll(result.getDeptIds(), deptService.getChildDeptIdListFromCache(userDeptId.get()));
    // 添加本身部门编号
    CollectionUtils.addIfNotNull(result.getDeptIds(), userDeptId.get());
    continue;
}
```

### 2. 修复OAuth2测试数据库错误

#### 2.1 修复OAuth2AccessTokenDO测试数据生成
在所有OAuth2相关测试中，明确设置`isParent`字段为有效值：

**影响的测试方法:**
- `testCheckAccessToken_expired`
- `testCheckAccessToken_refreshToken` 
- `testCheckAccessToken_success`
- `testGetAccessToken`
- `testGetAccessTokenPage`
- `testRefreshAccessToken_clientIdError`
- `testRefreshAccessToken_expired`
- `testRefreshAccessToken_success`
- `testRemoveAccessToken_success`

**修复策略:**
在创建OAuth2AccessTokenDO和OAuth2RefreshTokenDO测试数据时，明确设置isParent字段：

```java
// 对于OAuth2AccessTokenDO
OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class, o -> {
    o.setIsParent(0); // 明确设置为有效值
    // 其他设置...
});

// 对于OAuth2RefreshTokenDO  
OAuth2RefreshTokenDO refreshTokenDO = randomPojo(OAuth2RefreshTokenDO.class, o -> {
    o.setIsParent(0); // 明确设置为有效值
    // 其他设置...
});
```

## 实现细节

### 权限服务修复

**修改文件:** `yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/permission/PermissionServiceImpl.java`

**修改位置:** 第315行左右的`DEPT_AND_CHILD`处理分支

**修改内容:**
- 将`getUserRoleIdListByDeptIdFromCache(userId)`改为`deptService.getChildDeptIdListFromCache(userDeptId.get())`
- 这样可以正确获取子部门ID列表，而不是用户的部门关联列表

### OAuth2测试修复

**修改文件:** `yudao-module-system/src/test/java/cn/iocoder/yudao/module/system/service/oauth2/OAuth2TokenServiceImplTest.java`

**修改策略:**
1. 在所有创建OAuth2AccessTokenDO的地方，明确设置isParent字段
2. 在所有创建OAuth2RefreshTokenDO的地方，明确设置isParent字段
3. 确保值在tinyint范围内（0或1）

## 测试验证

### 验证权限服务修复
运行`PermissionServiceTest.testGetDeptDataPermission_DeptAndChild`测试：
- 验证返回的部门ID数量为2
- 验证包含用户本身的部门ID (3L)
- 验证包含子部门ID

### 验证OAuth2修复
运行所有OAuth2TokenServiceImplTest测试：
- 验证没有数据库插入错误
- 验证所有测试方法正常通过
- 验证isParent字段值正确设置

## 数据库设计考虑

### is_parent字段类型
当前H2测试数据库中`is_parent`字段定义为`tinyint`，建议：
- 保持现有定义不变
- 在测试中确保只使用0和1两个值
- 0表示非家长登录，1表示家长登录

## 错误处理

### 防止类似错误
1. 在测试数据生成时，对于有范围限制的字段要明确设置值
2. 使用类型安全的枚举值而不是随机整数
3. 添加数据库约束验证

### 代码审查要点
1. 确保数据权限相关的方法调用正确的服务方法
2. 验证测试数据的字段值在合法范围内
3. 检查mock对象的方法调用是否与实际业务逻辑一致

## 影响范围

### 功能影响
- 修复后部门及子部门数据权限功能将正常工作
- OAuth2令牌管理功能测试将通过

### 性能影响
- 修复不会对性能产生负面影响
- 正确的部门权限查询可能会提升数据过滤效率

## 风险评估

### 低风险
- 权限服务修复是bug修复，恢复原有设计意图
- OAuth2测试修复只影响测试代码，不影响生产代码

### 注意事项
- 确保修复后的权限逻辑与业务需求一致
- 验证所有相关的单元测试都能通过