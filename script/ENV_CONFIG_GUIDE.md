# 环境变量配置指南

## 📝 概述

本项目使用环境变量来管理敏感配置信息，提高安全性。所有 API Key、Webhook 地址等敏感信息都应配置在 `.env` 文件中，而不是硬编码在代码里。

---

## 🚀 快速开始

### 1. 复制配置模板

```bash
cp .env.example .env
```

### 2. 编辑 `.env` 文件

打开 `.env` 文件，填入真实的配置值：

```env
# Dify AI 配置（用于生成发布日志）
DIFY_API_KEY=app-xxxxxxxxxxxxxx
DIFY_API_URL=http://your_dify_server/v1

# 飞书通知配置
FEISHU_WEBHOOK=https://open.feishu.cn/open-apis/bot/v2/hook/xxxxxxxx

# 服务器配置
SERVER_HOST=your_server_ip
SERVER_BACKEND_PATH=/path/to/backend
SERVER_FRONTEND_ADMIN_PATH=/path/to/frontend/admin
SERVER_FRONTEND_WEB_PATH=/path/to/frontend/web

# 腾讯云COS配置
TENCENT_SECRET_ID=AKIDxxxxxxxx
TENCENT_SECRET_KEY=xxxxxxxx
```

### 3. 验证配置

运行测试命令验证配置是否正确：

```bash
npm run dev:test-dify
```

---

## 🔑 配置项说明

### Dify AI 配置 (必需)

| 配置项 | 说明 | 如何获取 | 示例 |
|--------|------|----------|------|
| `DIFY_API_KEY` | Dify API 密钥 | 在 Dify 平台创建 Workflow 后获取 | `app-LTUF7HU291Ug9LAKD4ZC4ZHO` |
| `DIFY_API_URL` | Dify API 地址 | Dify 服务器地址 | `http://154.9.255.162/v1` |

**获取步骤**：
1. 访问 [Dify 平台](https://dify.ai)
2. 创建或选择 Workflow 应用
3. 在应用设置中找到 API Key
4. 复制 API Key 和 API URL

### 飞书通知配置 (必需)

| 配置项 | 说明 | 如何获取 |
|--------|------|----------|
| `FEISHU_WEBHOOK` | 飞书机器人 Webhook 地址 | 在飞书群聊中添加机器人 |

**获取步骤**：
1. 在飞书群聊中点击「设置」→「群机器人」
2. 添加「自定义机器人」
3. 复制生成的 Webhook 地址

### 服务器配置 (可选)

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `SERVER_HOST` | 部署服务器 IP 或域名 | `42.194.163.176` |
| `SERVER_BACKEND_PATH` | 后端部署路径 | `/root/mindfront/work/project/mindtrip_server` |
| `SERVER_FRONTEND_ADMIN_PATH` | 管理后台部署路径 | `/root/mindfront/work/project/mindtrip_apps/admin` |
| `SERVER_FRONTEND_WEB_PATH` | Web前端部署路径 | `/root/mindfront/work/project/mindtrip_apps/web` |

### 腾讯云 COS 配置 (可选)

| 配置项 | 说明 | 如何获取 |
|--------|------|----------|
| `TENCENT_SECRET_ID` | 腾讯云 SecretId | [腾讯云控制台](https://console.cloud.tencent.com/cam/capi) |
| `TENCENT_SECRET_KEY` | 腾讯云 SecretKey | [腾讯云控制台](https://console.cloud.tencent.com/cam/capi) |

---

## ✅ 配置验证

### 自动验证

发布脚本会自动验证必需的环境变量：

```bash
npm run release
```

如果缺少必需配置，会显示：

```
❌ Missing required environment variables:
   - DIFY_API_KEY
   - FEISHU_WEBHOOK

Please configure in .env file
See .env.example for reference
```

### 手动测试

#### 测试 Dify API

```bash
npm run dev:test-dify
```

成功输出：
```
✓ 请求成功
✓ 发布日志生成成功
```

#### 测试飞书通知

运行一次发布看是否收到飞书通知：
```bash
npm run release:frontend
```

---

## 🔒 安全建议

### 1. 保护 .env 文件

`.env` 文件包含敏感信息，**绝不应该提交到版本控制系统**。

检查 `.gitignore` 确保包含：
```
.env
.env.local
.env.*.local
```

### 2. 权限设置

在 Linux/Mac 上，建议设置严格的文件权限：

```bash
chmod 600 .env
```

### 3. 环境隔离

不同环境使用不同的配置：

```
.env.development   # 开发环境
.env.staging       # 测试环境
.env.production    # 生产环境
```

加载指定环境的配置：

```bash
NODE_ENV=production node script/windows/release.mjs
```

### 4. CI/CD 环境

在 GitHub Actions、Jenkins 等 CI/CD 环境中，使用环境变量或密钥管理：

**GitHub Actions 示例**：
```yaml
env:
  DIFY_API_KEY: ${{ secrets.DIFY_API_KEY }}
  FEISHU_WEBHOOK: ${{ secrets.FEISHU_WEBHOOK }}
```

---

## 🛠️ 故障排除

### 问题 1：找不到环境变量

**症状**：
```
❌ 错误：DIFY_API_KEY 未配置
```

**解决**：
1. 确认 `.env` 文件在项目根目录
2. 检查变量名拼写是否正确
3. 确认没有多余的空格或引号

**正确格式**：
```env
DIFY_API_KEY=app-xxxxxx
```

**错误格式**：
```env
DIFY_API_KEY = app-xxxxxx  ❌ 有空格
DIFY_API_KEY="app-xxxxxx" ❌ 有引号（不需要）
```

### 问题 2：API Key 认证失败

**症状**：
```
Error status: 401
```

**解决**：
1. 验证 API Key 是否正确
2. 确认 Workflow 已发布
3. 检查 API URL 是否正确

### 问题 3：飞书通知发送失败

**症状**：
```
⚠ 飞书通知发送失败
```

**解决**：
1. 验证 Webhook 地址是否正确
2. 确认机器人未被删除
3. 检查网络连接

---

## 📚 代码示例

### 在脚本中使用配置

```javascript
import { config } from './lib/release-utils.mjs';

// 验证配置
if (!config.validate()) {
  process.exit(1);
}

// 使用配置
console.log('Dify API URL:', config.dify.apiUrl);
console.log('Server Host:', config.server.host);

// 发送飞书通知
await feishu.notifySuccess(version, notes, projectType);
```

### 自定义配置验证

```javascript
import { Config } from './lib/release-utils.mjs';

// 检查特定配置
if (!Config.dify.apiKey) {
  console.error('Dify API Key is required');
  process.exit(1);
}
```

---

## 🔄 迁移指南

### 从硬编码迁移

如果你之前使用硬编码的配置，按以下步骤迁移：

1. **找出所有硬编码的配置**
   ```javascript
   // 旧代码
   const API_KEY = 'app-xxxxxx';
   ```

2. **添加到 .env 文件**
   ```env
   DIFY_API_KEY=app-xxxxxx
   ```

3. **更新代码使用环境变量**
   ```javascript
   // 新代码
   import { config } from './lib/release-utils.mjs';
   const apiKey = config.dify.apiKey;
   ```

4. **删除旧的硬编码**

5. **测试功能是否正常**

---

## 📖 相关文档

- [.env.example](./.env.example) - 配置模板
- [SCRIPTS_OPTIMIZATION.md](./script/SCRIPTS_OPTIMIZATION.md) - 脚本优化说明
- [RELEASE_SCRIPTS_ANALYSIS.md](./script/RELEASE_SCRIPTS_ANALYSIS.md) - 脚本分析文档

---

## 💡 常见问题

**Q: 为什么要使用环境变量？**

A:
- ✅ 安全性：避免敏感信息泄露到版本控制
- ✅ 灵活性：不同环境使用不同配置
- ✅ 可维护性：集中管理配置

**Q: .env 文件会被提交到 Git 吗？**

A: 不会。`.env` 已在 `.gitignore` 中，不会被提交。但 `.env.example` 会被提交作为模板。

**Q: 如何在团队中共享配置？**

A: 通过安全的方式（如加密的文档、密钥管理工具）分享 `.env` 文件内容，不要通过 Git 或公开渠道分享。

**Q: 可以使用系统环境变量吗？**

A: 可以。脚本会优先读取 `.env` 文件，如果没有则使用系统环境变量。

---

## 🎉 最佳实践

1. ✅ **永远不要提交 `.env` 文件**
2. ✅ **保持 `.env.example` 更新**
3. ✅ **定期轮换 API Key**
4. ✅ **使用最小权限原则**
5. ✅ **记录配置更改**
6. ✅ **在 CI/CD 中使用密钥管理**
