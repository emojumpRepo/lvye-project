# 曼朗-心之旅项目部署脚本说明

本目录下的部署脚本已统一为 JavaScript（Node.js）脚本，避免了 bat/ps1 的转义与编码问题。

## 可用脚本

### 前端部署
- **传统部署**：`script/windows/deploy-frontend.mjs` - SSH上传到服务器
- **COS+CDN部署**：`script/windows/deploy-frontend-cos.mjs` - 静态资源上传到COS，HTML部署到服务器

### 后端部署
- **后端部署**：`script/windows/deploy-backend.mjs`

### COS工具
- **COSCLI安装**：`script/windows/tools/install-coscli-official.bat`
- **COSCLI配置**：`script/windows/tools/quick-config-coscli.bat`

## 推荐使用方式

建议使用根目录的 npm scripts：

### 传统部署
- 前端：`npm run deploy:frontend`
- 后端：`npm run deploy:backend`
- 全部：`npm run deploy:all`

### COS+CDN部署（新增）
- 前端：`npm run deploy:frontend:cos`
- 全部：`npm run deploy:all:cos`

### COS设置
- 安装COSCLI：`npm run setup:cos`
- 配置认证：`npm run config:cos`

## COS+CDN部署特点

新增的 `deploy-frontend-cos.mjs` 脚本实现了混合部署方式：
- ✅ **静态资源** (JS/CSS/图片/字体) → 上传到腾讯云COS，通过CDN加速
- ✅ **HTML文件** → 部署到Nginx服务器，保持SEO和路由控制
- ✅ **自动验证** → 检查COS上传状态和关键文件
- ✅ **兼容现有** → 保持与传统部署脚本相同的交互方式

### COS+CDN配置信息
- 存储桶：`mindtrip-1305613707`
- 地域：`ap-guangzhou`  
- CDN域名：`cdn.mindtrip.emojump.com`
- 配置文件：`script/windows/cos-deployment-config.yaml`

## 服务器与路径（与脚本保持一致）

- 服务器 IP：42.194.163.176
- 前端路径：
  - Admin 管理后台：`/root/mindfront/work/nginx/html/admin`
  - Web 用户端：`/root/mindfront/work/nginx/html/web`
- 后端路径：后端脚本运行时会询问，默认：`/www/wwwroot/lvye-server`

## 本地构建产物位置

- 前端构建目录：
  - Admin：`yudao-ui/lvye-project-frontend/apps/admin/dist/`
  - Web：`yudao-ui/lvye-project-frontend/apps/web/dist/`
- 后端 JAR：`yudao-server/target/yudao-server.jar`

## 依赖要求

### 基础环境
- Node.js（建议 16+）
- 项目依赖已在根 `package.json` 中定义

### COS+CDN部署额外要求
- **COSCLI工具**：腾讯云官方命令行工具
  - 自动安装：`npm run setup:cos`
  - 手动下载：https://github.com/tencentyun/coscli/releases
  - 配置认证：`npm run config:cos`

### 传统部署要求
- WinSCP：用于SSH文件传输
  - Windows（推荐）：`scoop install winscp`
  - 官网下载：https://winscp.net

## COS+CDN部署流程（脚本自动完成）

1. **检查环境**：验证COSCLI工具和本地构建产物
2. **上传静态资源**：将JS/CSS/图片等上传到COS
3. **部署HTML文件**：将HTML文件部署到Nginx服务器
4. **验证部署**：检查COS文件和关键资源
5. **CDN刷新**：等待CDN缓存更新（5-10分钟）

## 前端部署流程（传统方式）

1. 检查本地 dist.zip 是否存在
2. 连接服务器并创建必要目录
3. 备份现有文件到 `_backup/时间戳` 目录，仅保留最近 3 个
4. 清理目标目录旧文件
5. 上传 dist.zip 到临时目录并在服务器解压
6. 拷贝解压结果到目标目录
7. 设置权限（755 与 www:www）

## 常用访问地址

- Admin 管理后台：http://42.194.163.176/admin/
- Web 用户端：http://42.194.163.176/
- CDN静态资源：https://cdn.mindtrip.emojump.com/

## 使用示例

### COS+CDN部署（推荐）
```bash
# 1. 首次使用：安装和配置COSCLI
npm run setup:cos
npm run config:cos

# 2. 构建前端
npm run build:frontend

# 3. 部署前端（COS+CDN方式）
npm run deploy:frontend:cos
```

### 传统部署
```bash
# Admin
cd yudao-ui/lvye-project-frontend/apps/admin 
pnpm install && pnpm build:antd  # 生成 dist 目录
cd ../../../..
npm run deploy:frontend
```

### 完整部署（后端+前端）
```bash
npm run build:all
npm run deploy:all:cos  # 或 deploy:all
```

## 常见问题

### COS+CDN相关
- **"未找到 COSCLI"**：运行 `npm run setup:cos` 安装
- **"配置验证失败"**：运行 `npm run config:cos` 重新配置
- **"CDN未生效"**：等待5-10分钟缓存刷新

### 传统部署相关
- **"未找到 WinSCP"**：请先安装，并确保 PATH 中包含 WinSCP.com
- **中文乱码**：请使用支持 UTF-8 的终端/PowerShell
