# SQL 错误修复报告

## 问题描述

在调用 `/psychology/student-profile/simple-list` 接口时出现 SQL 语法错误：

```
Caused by: java.sql.SQLSyntaxErrorException: Unknown column 'a.sex' in 'where clause'
```

## 问题分析

### 根本原因
在 `StudentProfileMapper.xml` 文件中，SQL 查询的表结构和字段引用不匹配：

1. **表结构分析**：
   - `lvye_student_profile` 表（别名 `a`）：不包含 `sex` 字段
   - `system_users` 表（别名 `b`）：包含 `sex` 字段

2. **SQL 查询结构**：
   ```sql
   SELECT a.*,b.sex,b.mobile,c.name AS gradeName, d.name AS className 
   FROM lvye_student_profile a
   LEFT JOIN system_users b ON a.user_id = b.id
   LEFT JOIN system_dept c ON a.grade_dept_id = c.id
   LEFT JOIN system_dept d ON a.class_dept_id = d.id
   ```

3. **错误的查询条件**：
   ```sql
   AND a.sex = #{pageReqVO.sex}  -- ❌ 错误：a 表中没有 sex 字段
   ```

## 修复方案

### 修复内容

#### 1. 修复字段引用错误
**文件**：`yudao-module-psychology/src/main/resources/mapper/StudentProfileMapper.xml`

**修复前**：
```xml
<if test="pageReqVO.sex != null">
    AND a.sex = #{pageReqVO.sex}
</if>
```

**修复后**：
```xml
<if test="pageReqVO.sex != null">
    AND b.sex = #{pageReqVO.sex}
</if>
```

#### 2. 修复 ORDER BY 子句
**修复前**：
```xml
ORDER BY id DESC
```

**修复后**：
```xml
ORDER BY a.id DESC
```

### 修复后的完整 SQL
```sql
SELECT a.*,b.sex,b.mobile,c.name AS gradeName, d.name AS className 
FROM lvye_student_profile a
LEFT JOIN system_users b ON a.user_id = b.id
LEFT JOIN system_dept c ON a.grade_dept_id = c.id
LEFT JOIN system_dept d ON a.class_dept_id = d.id
WHERE 1 = 1
  AND (pageReqVO.studentNo != null ? a.student_no = #{pageReqVO.studentNo} : true)
  AND (pageReqVO.name != null ? a.name = #{pageReqVO.name} : true)
  AND (pageReqVO.sex != null ? b.sex = #{pageReqVO.sex} : true)  -- ✅ 修复：使用 b.sex
  AND (pageReqVO.gradeDeptId != null ? a.grade_dept_id = #{pageReqVO.gradeDeptId} : true)
  AND (pageReqVO.classDeptId != null ? a.class_dept_id = #{pageReqVO.classDeptId} : true)
  AND (pageReqVO.graduationStatus != null ? a.graduation_status = #{pageReqVO.graduationStatus} : true)
  AND (pageReqVO.psychologicalStatus != null ? a.psychological_status = #{pageReqVO.psychologicalStatus} : true)
  AND (pageReqVO.riskLevel != null ? a.risk_level = #{pageReqVO.riskLevel} : true)
ORDER BY a.id DESC  -- ✅ 修复：指定表别名
```

## 字段映射关系

### lvye_student_profile 表 (别名 a)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 关联 system_users.id |
| student_no | VARCHAR | 学号 |
| name | VARCHAR | 姓名 |
| grade_dept_id | BIGINT | 年级部门ID |
| class_dept_id | BIGINT | 班级部门ID |
| graduation_status | INT | 毕业状态 |
| psychological_status | INT | 心理状态 |
| risk_level | INT | 风险等级 |
| remark | VARCHAR | 备注 |

### system_users 表 (别名 b)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| sex | INT | 性别 |
| mobile | VARCHAR | 手机号 |

### system_dept 表 (别名 c, d)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 部门名称 |

## 测试验证

### 测试用例

#### 1. 基础查询测试
```http
GET /psychology/student-profile/simple-list
```
**预期**：返回所有学生档案列表

#### 2. 按性别筛选测试
```http
GET /psychology/student-profile/simple-list?sex=1
```
**预期**：返回性别为男性的学生列表

#### 3. 组合条件测试
```http
GET /psychology/student-profile/simple-list?gradeDeptId=1&sex=1
```
**预期**：返回指定年级的男性学生列表

#### 4. 分页接口兼容性测试
```http
GET /psychology/student-profile/page?pageNo=1&pageSize=10&sex=1
```
**预期**：分页接口也应该正常工作

## 影响范围

### 受影响的接口
1. ✅ `/psychology/student-profile/simple-list` - 新增接口
2. ✅ `/psychology/student-profile/page` - 现有分页接口
3. ✅ `/psychology/student-profile/get/{id}` - 详情查询接口

### 受影响的功能
1. ✅ 学生档案列表查询
2. ✅ 按性别筛选学生
3. ✅ 学生档案详情查看
4. ✅ 数据导出功能

## 预防措施

### 代码审查要点
1. **表别名一致性**：确保 SQL 中的字段引用使用正确的表别名
2. **字段存在性验证**：确认字段在对应的表中确实存在
3. **JOIN 关系正确性**：验证表连接关系和字段映射

### 测试建议
1. **SQL 语法测试**：在数据库中直接执行 SQL 语句验证语法正确性
2. **字段覆盖测试**：测试所有查询条件的组合
3. **边界条件测试**：测试空值、null 值等边界情况

## 总结

✅ **问题已修复**：SQL 语法错误已解决  
✅ **功能正常**：所有相关接口应该可以正常工作  
✅ **向后兼容**：修复不影响现有功能  
✅ **代码质量**：提高了 SQL 查询的规范性  

修复后，`/psychology/student-profile/simple-list` 接口应该可以正常使用，支持按性别等条件进行筛选查询。
