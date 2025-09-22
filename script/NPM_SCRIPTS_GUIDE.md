# 心之旅项目 NPM Scripts 使用指南

## 📋 可用的 NPM 脚本

### 🚀 发布相关

| 命令 | 说明 | 示例 |
|------|------|------|
| `pnpm release` | 交互式发布（手动选择） | `pnpm release` |
| `pnpm release:v2` | 新版独立版本发布 | `pnpm release:v2` |
| `pnpm release:auto` | 自动发布（patch版本） | `pnpm release:auto` |
| `pnpm release:patch` | 发布补丁版本 (0.0.x) | `pnpm release:patch` |
| `pnpm release:minor` | 发布功能版本 (0.x.0) | `pnpm release:minor` |
| `pnpm release:major` | 发布重大版本 (x.0.0) | `pnpm release:major` |
| `pnpm test:dify` | 测试 Dify API 连接 | `pnpm test:dify` |

### 🏗️ 构建相关

| 命令 | 说明 | 示例 |
|------|------|------|
| `pnpm build:backend` | 构建后端（Maven） | `pnpm build:backend` |
| `pnpm build:frontend` | 构建前端（Admin + Web） | `pnpm build:frontend` |
| `pnpm build:all` | 构建前后端 | `pnpm build:all` |

### 📦 部署相关

| 命令 | 说明 | 示例 |
|------|------|------|
| `pnpm deploy:backend` | 部署后端到服务器 | `pnpm deploy:backend` |
| `pnpm deploy:frontend` | 部署前端到服务器 | `pnpm deploy:frontend` |
| `pnpm deploy:all` | 部署前后端 | `pnpm deploy:all` |

## 💡 使用场景

### 场景 1：快速发布补丁
```bash
# 自动发布 patch 版本（如：0.0.1 → 0.0.2）
pnpm release:auto
```

### 场景 2：发布新功能
```bash
# 发布 minor 版本（如：0.0.2 → 0.1.0）
pnpm release:minor
```

### 场景 3：重大更新
```bash
# 发布 major 版本（如：0.1.0 → 1.0.0）
pnpm release:major
```

### 场景 4：交互式发布（推荐）
```bash
# 手动选择版本类型和发布内容
pnpm release

# 新版独立版本体系
pnpm release:v2
```

### 场景 5：测试 AI 日志生成
```bash
# 测试 Dify Workflow API
pnpm test:dify
```

## 🔧 命令参数说明

### release.mjs 支持的参数
- `--auto`: 自动模式，跳过交互提示
- `--patch`: 指定发布 patch 版本
- `--minor`: 指定发布 minor 版本  
- `--major`: 指定发布 major 版本
- `--version=x.x.x`: 指定具体版本号

### 示例：
```bash
# 直接指定版本号
node script/release.mjs --version=1.0.0

# 自动模式 + patch
node script/release.mjs --auto --patch
```

## ⚠️ 注意事项

1. **分支限制**：所有发布命令必须在 `master` 分支执行
2. **权限要求**：需要有 Git push 权限和服务器部署权限
3. **网络要求**：需要能访问 Dify API 和飞书 Webhook
4. **版本冲突**：发布前会自动 `git pull` 避免冲突

## 📝 版本号规范

遵循语义化版本规范 (Semantic Versioning):

- **MAJOR** (x.0.0): 不兼容的 API 修改
- **MINOR** (0.x.0): 向下兼容的功能性新增
- **PATCH** (0.0.x): 向下兼容的问题修复

### 选择指南：
- 修复 bug → `release:patch`
- 新增功能 → `release:minor`
- 重大变更 → `release:major`
- 日常发布 → `release:auto`

## 🎯 最佳实践

1. **日常开发**：使用 `release:auto` 快速发布
2. **功能发布**：使用 `release:minor` 标记新功能
3. **重大更新**：使用 `release:major` 并写好升级指南
4. **测试先行**：发布前运行 `test:dify` 确保 API 正常

## 📚 相关文档

- [版本管理方案](./version-management-plan.md)
- [发布安全说明](./RELEASE_SECURITY.md)
- [重构总结](./REFACTOR_SUMMARY.md)