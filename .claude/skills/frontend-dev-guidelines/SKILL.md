# Vue 3 前端开发指南

> **技能类型**: 开发规范和最佳实践  
> **适用范围**: yudao-ui/lvye-project-frontend  
> **技术栈**: Vue 3, TypeScript, Vite, Tailwind CSS, Pinia, Vue Router

---

## 📋 目录

本技能提供完整的 Vue 3 前端开发规范，采用模块化设计，按需加载：

### 核心模块
- [架构概览](#架构概览) - 项目整体架构说明
- [项目结构](#项目结构) - 目录组织和命名
- [代码风格](#代码风格) - 命名、格式化、注释

### 详细指南（按需查看资源文件）
- `01-component-guidelines.md` - 组件开发规范
- `02-api-guidelines.md` - API 调用和 HTTP 请求规范
- `03-router-guidelines.md` - 路由配置规范
- `04-state-management.md` - Pinia 状态管理规范
- `05-typescript-usage.md` - TypeScript 使用规范
- `06-styling-guidelines.md` - 样式和 Tailwind CSS 规范
- `07-form-validation.md` - 表单处理和校验规范
- `08-error-handling.md` - 错误处理规范
- `09-performance.md` - 性能优化建议
- `10-testing.md` - 单元测试和 E2E 测试

---

## 架构概览

### 技术栈

- **核心框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **语言**: TypeScript
- **UI 框架**: 基于 Vben Admin (自定义组件库)
- **样式**: Tailwind CSS + SCSS
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP**: Axios
- **图标**: Iconify

### 项目特点

- 📦 Monorepo 架构 (pnpm workspace)
- 🎨 多应用支持 (admin / web / mobile)
- 🔧 高度模块化和可复用
- 🚀 基于 Vben Admin 4.x
- 💪 完整的 TypeScript 支持

---

## 项目结构

### 整体结构

```
yudao-ui/lvye-project-frontend/
├── apps/                      # 应用目录
│   ├── admin/                # 管理后台
│   ├── web/                  # 用户端 Web
│   ├── student-h5/           # H5 应用
│   └── backend-mock/         # Mock 服务
├── packages/                  # 共享包
│   ├── @core/               # 核心包
│   │   ├── base/           # 基础工具
│   │   ├── ui-kit/         # UI 组件
│   │   ├── composables/    # 组合式函数
│   │   └── preferences/    # 偏好设置
│   ├── effects/             # 副作用包
│   │   ├── access/         # 权限
│   │   ├── common-ui/      # 通用 UI
│   │   ├── hooks/          # 自定义 Hooks
│   │   ├── layouts/        # 布局
│   │   ├── plugins/        # 插件
│   │   └── request/        # 请求
│   ├── constants/           # 常量
│   ├── icons/              # 图标
│   ├── locales/            # 国际化
│   ├── stores/             # 状态管理
│   ├── styles/             # 样式
│   ├── types/              # 类型定义
│   └── utils/              # 工具函数
└── internal/                # 内部配置
    ├── lint-configs/       # Lint 配置
    ├── vite-config/        # Vite 配置
    └── tsconfig/           # TS 配置
```

### 单个应用结构 (apps/admin/)

```
apps/admin/
├── src/
│   ├── api/               # API 接口
│   │   ├── core/         # 核心接口
│   │   └── modules/      # 模块接口
│   ├── views/            # 页面视图
│   │   ├── psychology/   # 心理模块
│   │   └── system/       # 系统模块
│   ├── router/           # 路由配置
│   │   ├── routes/       # 路由定义
│   │   └── index.ts      # 路由入口
│   ├── store/            # 应用级状态
│   ├── components/       # 应用级组件
│   ├── layouts/          # 布局组件
│   ├── utils/            # 工具函数
│   ├── types/            # 类型定义
│   ├── locales/          # 国际化
│   ├── assets/           # 静态资源
│   ├── styles/           # 样式文件
│   ├── app.vue           # 根组件
│   ├── main.ts           # 入口文件
│   └── preferences.ts    # 应用配置
├── public/               # 公共资源
├── index.html           # HTML 模板
├── vite.config.mts      # Vite 配置
├── tsconfig.json        # TS 配置
└── package.json         # 依赖配置
```

---

## 代码风格

### 文件命名

```
# 组件文件 - PascalCase
UserProfile.vue
DataTable.vue
SearchForm.vue

# 工具文件 - kebab-case
date-utils.ts
api-helper.ts
auth-service.ts

# 类型文件 - kebab-case
user-types.ts
api-types.ts

# API 文件 - kebab-case
user-api.ts
psychology-api.ts
```

### 组件命名

```vue
<!-- ✅ 推荐：多词组件名 -->
<script setup lang="ts">
defineOptions({ name: 'UserProfile' });
</script>

<!-- ✅ 使用 PascalCase -->
<UserProfile />
<DataTable />

<!-- ❌ 避免：单词组件名 -->
<Profile />
<Table />

<!-- ❌ 避免：kebab-case（除非是 HTML 原生标签） -->
<user-profile />
```

### 变量命名

```typescript
// ✅ 推荐：语义化命名
const userList = ref<User[]>([]);
const isLoading = ref(false);
const currentPage = ref(1);

// ✅ 布尔值用 is/has/can 前缀
const isVisible = ref(true);
const hasPermission = computed(() => ...);
const canEdit = ref(false);

// ✅ 函数用动词开头
const fetchUserList = async () => { ... };
const handleSubmit = () => { ... };
const onPageChange = (page: number) => { ... };

// ❌ 避免：无意义命名
const data = ref([]);
const flag = ref(false);
const temp = ref(null);
```

### 常量命名

```typescript
// ✅ 使用 UPPER_SNAKE_CASE
export const MAX_PAGE_SIZE = 100;
export const DEFAULT_PAGE_SIZE = 20;
export const API_BASE_URL = '/api';

// ✅ 枚举使用 PascalCase
export enum UserStatus {
  Active = 1,
  Inactive = 2,
  Banned = 3,
}
```

---

## Vue 3 Composition API

### script setup 基础结构

```vue
<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import type { User } from '@/types/user';

// 1. 组件名定义
defineOptions({ name: 'UserProfile' });

// 2. Props 定义
interface Props {
  userId: number;
  showActions?: boolean;
}
const props = withDefaults(defineProps<Props>(), {
  showActions: true,
});

// 3. Emits 定义
interface Emits {
  (e: 'update', user: User): void;
  (e: 'delete', id: number): void;
}
const emit = defineEmits<Emits>();

// 4. 响应式数据
const user = ref<User | null>(null);
const isLoading = ref(false);

// 5. 计算属性
const fullName = computed(() => {
  return user.value ? `${user.value.firstName} ${user.value.lastName}` : '';
});

// 6. 方法
const fetchUser = async () => {
  isLoading.value = true;
  try {
    user.value = await getUserApi(props.userId);
  } finally {
    isLoading.value = false;
  }
};

// 7. 侦听器
watch(
  () => props.userId,
  (newId) => {
    if (newId) {
      fetchUser();
    }
  },
);

// 8. 生命周期
onMounted(() => {
  fetchUser();
});
</script>

<template>
  <div class="user-profile">
    <div v-if="isLoading">加载中...</div>
    <div v-else-if="user">
      <h2>{{ fullName }}</h2>
      <button v-if="showActions" @click="emit('delete', user.id)">
        删除
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.user-profile {
  padding: 20px;
}
</style>
```

---

## 常见模式

### 1. 列表页面（CRUD）

```vue
<script setup lang="ts">
import { ref, reactive } from 'vue';
import { getUserPage, deleteUser } from '@/api/modules/user';
import type { User, UserPageParams } from '@/types/user';

defineOptions({ name: 'UserList' });

// 查询参数
const queryParams = reactive<UserPageParams>({
  pageNo: 1,
  pageSize: 20,
  username: '',
});

// 列表数据
const userList = ref<User[]>([]);
const total = ref(0);
const loading = ref(false);

// 获取列表
const fetchList = async () => {
  loading.value = true;
  try {
    const { list, total: totalCount } = await getUserPage(queryParams);
    userList.value = list;
    total.value = totalCount;
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  queryParams.pageNo = 1;
  fetchList();
};

// 重置
const handleReset = () => {
  Object.assign(queryParams, {
    pageNo: 1,
    pageSize: 20,
    username: '',
  });
  fetchList();
};

// 删除
const handleDelete = async (id: number) => {
  await deleteUser(id);
  fetchList();
};

// 初始化
onMounted(() => {
  fetchList();
});
</script>
```

### 2. 表单页面

```vue
<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { createUser, updateUser, getUser } from '@/api/modules/user';
import type { UserForm } from '@/types/user';

defineOptions({ name: 'UserForm' });

const router = useRouter();
const props = defineProps<{ id?: number }>();

// 表单数据
const formData = reactive<UserForm>({
  username: '',
  mobile: '',
  email: '',
});

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' },
  ],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
};

const loading = ref(false);
const formRef = ref();

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate();
  
  loading.value = true;
  try {
    if (props.id) {
      await updateUser(props.id, formData);
    } else {
      await createUser(formData);
    }
    router.back();
  } finally {
    loading.value = false;
  }
};

// 如果是编辑，加载数据
onMounted(async () => {
  if (props.id) {
    const user = await getUser(props.id);
    Object.assign(formData, user);
  }
});
</script>
```

### 3. 详情页面

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getUser } from '@/api/modules/user';
import type { User } from '@/types/user';

defineOptions({ name: 'UserDetail' });

const props = defineProps<{ id: number }>();

const user = ref<User | null>(null);
const loading = ref(false);

const fetchDetail = async () => {
  loading.value = true;
  try {
    user.value = await getUser(props.id);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchDetail();
});
</script>

<template>
  <div v-loading="loading">
    <div v-if="user">
      <h2>{{ user.username }}</h2>
      <p>手机号：{{ user.mobile }}</p>
      <p>邮箱：{{ user.email }}</p>
    </div>
  </div>
</template>
```

---

## API 调用规范

### API 文件组织

```typescript
// src/api/modules/user-api.ts
import { requestClient } from '@/utils/request';
import type { User, UserPageParams, UserForm } from '@/types/user';
import type { PageResult } from '@/types/common';

/**
 * 获取用户分页
 */
export function getUserPage(params: UserPageParams) {
  return requestClient.get<PageResult<User>>('/psychology/user/page', { params });
}

/**
 * 获取用户详情
 */
export function getUser(id: number) {
  return requestClient.get<User>(`/psychology/user/get?id=${id}`);
}

/**
 * 创建用户
 */
export function createUser(data: UserForm) {
  return requestClient.post<number>('/psychology/user/create', data);
}

/**
 * 更新用户
 */
export function updateUser(data: UserForm) {
  return requestClient.put<boolean>('/psychology/user/update', data);
}

/**
 * 删除用户
 */
export function deleteUser(id: number) {
  return requestClient.delete<boolean>(`/psychology/user/delete?id=${id}`);
}
```

详细 API 规范请参考：`02-api-guidelines.md`

---

## TypeScript 规范

### 类型定义

```typescript
// src/types/user.ts

/**
 * 用户实体
 */
export interface User {
  id: number;
  username: string;
  mobile: string;
  email?: string;
  status: number;
  createTime: string;
}

/**
 * 用户分页查询参数
 */
export interface UserPageParams {
  pageNo: number;
  pageSize: number;
  username?: string;
  mobile?: string;
}

/**
 * 用户表单
 */
export interface UserForm {
  id?: number;
  username: string;
  mobile: string;
  email?: string;
}
```

详细 TypeScript 规范请参考：`05-typescript-usage.md`

---

## 关键原则

### ✅ 遵循的原则

1. **组件化**: 合理拆分组件，单一职责
2. **类型安全**: 充分利用 TypeScript
3. **组合式 API**: 使用 `<script setup>`
4. **响应式设计**: 适配不同屏幕尺寸
5. **性能优化**: 懒加载、虚拟滚动等
6. **代码复用**: 提取通用逻辑到 composables
7. **错误处理**: 全局错误拦截 + 局部处理
8. **可维护性**: 清晰的目录结构和命名

### ❌ 避免的做法

1. **不要在模板中写复杂逻辑** - 使用计算属性
2. **不要直接修改 props** - 使用 emit 或 v-model
3. **不要忘记清理副作用** - onUnmounted 中清理定时器等
4. **不要过度使用 any** - 定义明确的类型
5. **不要在 setup 中使用 this** - Composition API 不需要
6. **不要忽略响应式** - 使用 ref/reactive
7. **不要滥用全局状态** - 优先组件内状态

---

## 资源文件索引

当你需要深入了解某个主题时，参考对应的资源文件：

| 场景 | 资源文件 |
|------|----------|
| 开发组件 | `01-component-guidelines.md` |
| 调用后端 API | `02-api-guidelines.md` |
| 配置路由 | `03-router-guidelines.md` |
| 状态管理 | `04-state-management.md` |
| TypeScript | `05-typescript-usage.md` |
| 样式和 UI | `06-styling-guidelines.md` |
| 表单处理 | `07-form-validation.md` |
| 错误处理 | `08-error-handling.md` |
| 性能优化 | `09-performance.md` |
| 测试 | `10-testing.md` |

---

## 激活此技能

在以下情况下，此技能会自动激活：
- 编辑 `.vue`、`.ts` 文件
- 在 `yudao-ui/lvye-project-frontend` 目录工作
- 询问关于前端、组件、页面的问题

手动激活：`@frontend-dev-guidelines`

---

> **提示**: 此技能采用模块化设计，核心概览保持在 500 行以内，详细内容分散在资源文件中。

