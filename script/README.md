#!/usr/bin/env node

/**
 * README - 心之旅项目发布系统使用指南
 * 
 * 本项目提供了完整的自动化发布解决方案，包括构建、部署、版本管理和通知。
 */

# 心之旅项目发布系统

## 📋 功能特性

- ✅ **一键发布**：自动完成构建、部署、通知全流程
- ✅ **版本管理**：自动管理版本号，支持语义化版本
- ✅ **AI 日志生成**：通过 Dify AI 生成运营友好的更新日志
- ✅ **飞书通知**：发布完成自动发送飞书群通知
- ✅ **灵活配置**：支持选择性发布前端/后端
- ✅ **备份机制**：自动备份，支持快速回滚
- ✅ **本地构建**：保持本地控制，便于调试

## 🚀 快速开始

### 安装依赖

```bash
# 在项目根目录执行
npm install
```

### 配置环境（可选）

如果需要修改默认配置，可以设置环境变量：

```bash
# Windows
set SSH_PASSWORD=your_password
set DIFY_API_KEY=your_api_key
set FEISHU_WEBHOOK_URL=your_webhook_url

# Linux/Mac
export SSH_PASSWORD=your_password
export DIFY_API_KEY=your_api_key
export FEISHU_WEBHOOK_URL=your_webhook_url
```

## 📝 使用方法

### 1. 完整发布（推荐）

交互式发布，手动输入版本号：

```bash
npm run release
```

系统会提示：
- 输入版本号（如 1.2.0）
- 选择发布内容（前端/后端/全部）
- 确认发布

### 2. 快速发布

自动计算版本号并发布：

```bash
# 发布补丁版本 (1.0.0 -> 1.0.1)
npm run release:patch

# 发布次要版本 (1.0.0 -> 1.1.0)
npm run release:minor

# 发布主要版本 (1.0.0 -> 2.0.0)
npm run release:major

# 自动发布（相当于 patch）
npm run release:auto
```

### 3. 仅构建

只构建不部署：

```bash
# 构建后端
npm run build:backend

# 构建前端
npm run build:frontend

# 构建全部
npm run build:all
```

### 4. 仅部署

使用已构建的文件进行部署：

```bash
# 部署后端
npm run deploy:backend

# 部署前端
npm run deploy:frontend

# 部署全部
npm run deploy:all
```

## 📁 项目结构

```
mindtrip-project/
├── script/
│   ├── release.mjs          # 主发布脚本
│   ├── quick-release.mjs    # 快速发布脚本
│   └── config/
│       └── deploy.config.js # 部署配置文件
├── script/windows/
│   ├── deploy-backend.mjs   # 后端部署脚本
│   └── deploy-frontend.mjs  # 前端部署脚本
├── releases.log             # 发布历史记录
└── package.json            # 项目配置和脚本
```

## ⚙️ 配置说明

### 部署配置 (script/config/deploy.config.js)

主要配置项：

- **server**: 服务器连接信息
- **api.dify**: Dify AI 配置
- **api.feishu**: 飞书通知配置
- **backup**: 备份设置
- **oss**: OSS 配置（预留）

### 版本号规则

- **主版本号 (Major)**: 重大更新，不兼容的 API 修改
- **次版本号 (Minor)**: 功能性新增，向下兼容
- **修订号 (Patch)**: 修复问题，向下兼容

## 🔄 发布流程

1. **版本检查**：确认版本号和发布内容
2. **构建项目**：
   - 后端：Maven 打包生成 JAR
   - 前端：pnpm build 生成 dist.zip
3. **部署到服务器**：
   - 上传构建产物
   - 备份旧版本
   - 更新服务
4. **生成日志**：
   - 获取 Git 提交记录
   - 调用 Dify AI 生成运营日志
5. **发送通知**：
   - 发送飞书群通知
   - 记录发布历史

## 📊 发布记录

发布记录保存在 `releases.log` 文件中，格式：

```json
{"version":"v1.0.0","content":"前端+后端","timestamp":"2025-01-22T10:00:00.000Z","deployer":"user","server":"42.194.163.176"}
```

## 🔧 故障处理

### 构建失败

1. 检查 Maven 和 Node.js 环境
2. 确保依赖已安装：`npm install` 和 `pnpm install`
3. 查看具体错误信息

### 部署失败

1. 检查服务器连接：`ping 42.194.163.176`
2. 验证 SSH 密码是否正确
3. 确认服务器磁盘空间充足

### 通知失败

1. 检查网络连接
2. 验证 API 密钥和 Webhook URL
3. 查看错误日志

## 🔙 回滚操作

如果发布后出现问题，可以快速回滚：

```bash
# SSH 登录服务器
ssh root@42.194.163.176

# 后端回滚
cd /root/mindfront/work/project/mindtrip_server
cp backup/yudao-server.jar.最新时间戳.bak yudao-server.jar
docker-compose restart

# 前端回滚
cd /root/mindfront/work/project/mindtrip_apps/admin_backup
cp -r backup_最新时间戳/* ../admin/
```

## 🎯 最佳实践

1. **发布前检查**：
   - 确保代码已提交
   - 本地测试通过
   - 查看是否有未完成的功能

2. **版本号管理**：
   - Bug 修复使用 patch
   - 新功能使用 minor
   - 架构调整使用 major

3. **发布时机**：
   - 避免在高峰期发布
   - 预留回滚时间
   - 通知相关人员

## 📮 技术支持

- 服务器：42.194.163.176
- Dify API：http://154.9.255.162/v1
- 飞书群：通过 Webhook 接收通知

## 🔐 安全提示

1. 不要将密码提交到 Git
2. 生产环境使用环境变量配置
3. 定期更新依赖包
4. 保护好 API 密钥

---

*最后更新：2025-01-22*