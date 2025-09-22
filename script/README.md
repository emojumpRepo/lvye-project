# 心之旅项目脚本目录

## 📁 目录结构

```
script/
├── release.mjs             # 主发布脚本（版本管理）
├── release-with-deploy.mjs # 完整发布脚本（包含构建和部署）
├── test-dify.mjs           # Dify API 测试工具
├── config/
│   └── deploy.config.js   # 部署配置文件
├── windows/
│   ├── deploy-backend.mjs  # 后端部署脚本
│   └── deploy-frontend.mjs # 前端部署脚本
├── docker/                 # Docker 相关配置
├── jenkins/                # CI/CD 配置
└── shell/                  # Shell 脚本
```

## 🚀 快速使用

### 版本发布（推荐）

```bash
# 使用独立版本管理发布
npm run release

# 交互式选择：
# - 选择项目（后端/前端/全栈）
# - 选择版本类型（patch/minor/major）
# - 确认发布
```

### 完整发布流程（包含部署）

```bash
# 包含构建、部署、通知的完整流程
npm run release:deploy
```

### 其他命令

```bash
# 构建命令
npm run build:backend       # 构建后端
npm run build:frontend      # 构建前端
npm run build:all          # 构建全部

# 部署命令
npm run deploy:backend      # 部署后端
npm run deploy:frontend     # 部署前端
npm run deploy:all         # 部署全部

# 测试命令
npm run test:dify         # 测试 Dify API
```

## 📝 脚本说明

### release.mjs
- **功能**：主发布脚本，管理版本号
- **特点**：
  - 前后端独立版本号（从 v0.0.1 开始）
  - 智能 Git commits 获取
  - 自动生成 AI 发布日志
  - 飞书通知集成
  - Master 分支保护
  - 前端标签在独立仓库创建

### release-with-deploy.mjs
- **功能**：完整的发布流程脚本
- **特点**：
  - 包含构建和部署步骤
  - 自动压缩和上传
  - 服务器自动重启
  - 完整的错误处理

### deploy-backend.mjs / deploy-frontend.mjs
- **功能**：独立的部署脚本
- **特点**：
  - SSH 远程部署
  - 自动备份
  - 服务管理
  - 进度显示

## ⚙️ 配置说明

### 环境变量
所有敏感配置都在 `config/deploy.config.js` 中管理：

```javascript
{
  dify: {
    apiKey: 'app-xxx',      // Dify API Key
    apiUrl: 'http://xxx'    // Dify API 地址
  },
  feishu: {
    webhook: 'https://xxx'  // 飞书 Webhook
  },
  server: {
    host: 'xxx',           // 服务器地址
    username: 'xxx',       // SSH 用户名
    password: 'xxx'        // SSH 密码
  }
}
```

### 版本文件
- **后端**：`/version.properties`
- **前端**：`/yudao-ui/lvye-project-frontend/version.json`

## 🏷️ Git 标签规范

- **后端标签**：`mindtrip-backend-v{version}`（主仓库）
- **前端标签**：`mindtrip-frontend-v{version}`（前端独立仓库）
- **全栈标签**：`mindtrip-v{version}`（主仓库）

## 🔐 安全说明

- 所有敏感信息都在配置文件中管理
- 配置文件不应提交到 Git
- 使用环境变量覆盖默认配置
- SSH 连接使用加密传输

## 📚 更多信息

- [部署文档](./windows/README.md)
- [Docker 使用](./docker/Docker-HOWTO.md)
- [项目总结](./REFACTOR_SUMMARY.md)