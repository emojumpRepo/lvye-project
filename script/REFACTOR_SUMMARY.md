# 心之旅项目脚本重构完成

## 📝 更改总结

### 1. 名称统一更新
- ✅ 所有"绿叶"已改为"心之旅"
- ✅ 项目标识：
  - `lvye-backend` → `mindtrip-backend`
  - `lvye-frontend` → `mindtrip-frontend`
  - `lvye-v` → `mindtrip-v`

### 2. 目录结构优化
- ✅ 脚本统一放置在 `/script` 目录（原 `/scripts`）
- ✅ 避免了多个脚本目录的混乱

### 3. 版本管理升级
- ✅ 建立独立版本体系（从 v0.0.1 开始）
- ✅ 前后端独立版本文件：
  - 后端：`version.properties`
  - 前端：`yudao-ui/lvye-project-frontend/version.json`
- ✅ Git tag 命名规范：
  - 后端：`mindtrip-backend-v0.0.1`
  - 前端：`mindtrip-frontend-v0.0.1`
  - 全栈：`mindtrip-v0.0.1`

### 4. 核心脚本文件

| 文件 | 功能 |
|------|------|
| `script/release.mjs` | 主发布脚本（原版） |
| `script/release-v2.mjs` | 独立版本发布脚本（新版） |
| `script/test-dify.mjs` | Dify API 测试工具 |
| `script/config/deploy.config.js` | 集中配置文件 |

### 5. Dify 集成优化
- ✅ API Key: `app-27WVHbSe1uUxcd54gDGKySl1`
- ✅ 简化输入变量到 4 个：
  - `query`: Git commit 信息（自动获取）
  - `version`: 版本号
  - `release_type`: 发布类型
  - `target_audience`: 目标受众
- ✅ 输出控制在 50-100 字

### 6. 自动化改进
- ✅ 自动获取 Git commits 作为发布内容
- ✅ 智能判断：优先使用 tag 之间的 commits，否则获取最近 10 条
- ✅ 移除 commit hash，只保留 message

## 🚀 使用方法

### 新版发布（推荐）
```bash
# 使用独立版本管理
node script/release-v2.mjs

# 会显示交互菜单：
# 1. 选择项目（后端/前端/全栈）
# 2. 选择版本类型（patch/minor/major）
# 3. 确认发布
```

### 原版发布
```bash
# 使用原有发布流程
node script/release.mjs

# 或使用 npm 脚本
npm run release
```

### 测试 Dify
```bash
node script/test-dify.mjs
```

## 📁 目录结构
```
心之旅项目/
├── script/                  # 统一脚本目录
│   ├── release.mjs         # 主发布脚本
│   ├── release-v2.mjs      # 独立版本发布脚本
│   ├── test-dify.mjs       # Dify测试工具
│   ├── config/
│   │   └── deploy.config.js # 配置中心
│   └── windows/
│       ├── deploy-backend.mjs
│       └── deploy-frontend.mjs
├── version.properties       # 后端版本文件
└── yudao-ui/
    └── lvye-project-frontend/
        └── version.json     # 前端版本文件
```

## ✨ 优势
1. **品牌统一**：心之旅品牌名称贯穿整个系统
2. **版本独立**：前后端可独立发版，版本清晰
3. **自动智能**：基于真实 commits 生成发布说明
4. **目录简洁**：统一使用 `/script` 目录

## 🎯 下一步
1. 运行 `node script/release-v2.mjs` 创建第一个心之旅版本
2. 版本将从 `v0.0.1` 开始全新计数
3. 前后端可独立管理和发布