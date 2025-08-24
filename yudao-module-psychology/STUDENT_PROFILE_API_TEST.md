# 学生档案不分页接口测试文档

## 新增接口说明

为 `/psychology/student-profile` 添加了一个不分页的接口，用于获取学生档案列表。

### 后端接口

#### 接口地址
```
GET /psychology/student-profile/simple-list
```

#### 接口描述
获得学生档案精简列表（不分页），主要用于前端的下拉选项

#### 请求参数
支持所有 `StudentProfilePageReqVO` 中的查询条件：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| studentNo | String | 否 | 学号 |
| name | String | 否 | 姓名 |
| sex | Integer | 否 | 性别 |
| gradeDeptId | Long | 否 | 年级部门编号 |
| classDeptId | Long | 否 | 班级部门编号 |
| graduationStatus | Integer | 否 | 毕业状态 |
| psychologicalStatus | Integer | 否 | 心理状态 |
| riskLevel | Integer | 否 | 风险等级 |

#### 响应数据
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "userId": 123,
      "studentNo": "2024001",
      "name": "张三",
      "sex": 1,
      "mobile": "13800138000",
      "gradeDeptId": 1,
      "classDeptId": 2,
      "graduationStatus": 0,
      "psychologicalStatus": 1,
      "riskLevel": 1,
      "remark": "备注信息",
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-01T00:00:00",
      "gradeName": "高一年级",
      "className": "高一(1)班"
    }
  ],
  "msg": "操作成功"
}
```

### 前端API函数

#### 函数签名
```typescript
export function getStudentProfileSimpleList(params?: Partial<PsychologyStudentProfileApi.StudentProfilePageReq>)
```

#### 使用示例

**获取所有学生档案：**
```typescript
import { getStudentProfileSimpleList } from '#/api/psychology/student-profile';

// 获取所有学生档案
const allStudents = await getStudentProfileSimpleList();
```

**按条件筛选：**
```typescript
// 获取特定年级的学生
const gradeStudents = await getStudentProfileSimpleList({
  gradeDeptId: 1
});

// 获取特定班级的学生
const classStudents = await getStudentProfileSimpleList({
  classDeptId: 2
});

// 按姓名搜索
const searchResults = await getStudentProfileSimpleList({
  name: '张'
});

// 组合条件查询
const filteredStudents = await getStudentProfileSimpleList({
  gradeDeptId: 1,
  psychologicalStatus: 1,
  riskLevel: 2
});
```

## 实现细节

### 后端实现

1. **Service接口** (`StudentProfileService.java`)
   - 新增 `getStudentProfileList(StudentProfilePageReqVO reqVO)` 方法

2. **Service实现** (`StudentProfileServiceImpl.java`)
   - 使用 `Integer.MAX_VALUE` 作为页面大小来获取所有数据
   - 复用现有的 `selectPageList` 方法

3. **Controller** (`StudentProfileController.java`)
   - 新增 `/simple-list` 端点
   - 支持所有分页查询的参数，但不返回分页信息

### 前端实现

1. **API函数更新**
   - `getStudentProfileSimpleList` 函数现在支持可选的查询参数
   - 保持向后兼容性，参数为可选

## 测试建议

### 功能测试
1. **基础功能**
   - [ ] 无参数调用，获取所有学生档案
   - [ ] 验证返回数据格式正确
   - [ ] 验证不包含分页信息

2. **筛选功能**
   - [ ] 按学号筛选
   - [ ] 按姓名筛选
   - [ ] 按年级筛选
   - [ ] 按班级筛选
   - [ ] 按心理状态筛选
   - [ ] 组合条件筛选

3. **边界测试**
   - [ ] 空结果集处理
   - [ ] 大数据量处理
   - [ ] 无效参数处理

### 性能测试
- [ ] 大量数据时的响应时间
- [ ] 内存使用情况
- [ ] 并发请求处理

## 使用场景

这个不分页接口主要适用于：

1. **下拉选择框** - 选择学生时的选项列表
2. **数据导出** - 导出所有符合条件的学生数据
3. **批量操作** - 需要对所有学生进行批量处理
4. **统计分析** - 需要获取完整数据集进行分析
5. **缓存预加载** - 前端缓存学生列表数据

## 注意事项

1. **数据量控制** - 建议在数据量较大时添加合适的筛选条件
2. **权限控制** - 接口继承了原有的权限控制机制
3. **性能监控** - 建议监控接口的响应时间和内存使用
4. **缓存策略** - 可以考虑在前端或后端添加适当的缓存机制

## 兼容性

- ✅ 向后兼容：现有代码无需修改
- ✅ 参数兼容：支持所有原有的查询参数
- ✅ 响应兼容：返回数据格式与分页接口中的 `records` 一致
