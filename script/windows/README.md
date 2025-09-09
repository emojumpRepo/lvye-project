# 芋道项目部署脚本说明

## 脚本文件

### 1. deploy-backend.bat - 后端部署脚本
部署Java后端服务到宝塔服务器

**功能特点:**
- 自动备份现有JAR文件到temp目录（带时间戳）
- 优雅停止运行中的Java进程
- 上传新JAR文件（先上传为临时文件，再替换）
- 自动启动服务
- 检查服务启动状态

**使用方法:**
1. 先构建后端: `mvn clean package -DskipTests`
2. 运行脚本: `deploy-backend.bat`
3. 输入服务器密码
4. 等待部署完成

### 2. deploy-frontend.bat - 前端部署脚本
部署Vue前端到宝塔服务器

**功能特点:**
- 自动备份现有前端文件（带时间戳）
- 同步上传dist目录到服务器
- 设置正确的文件权限
- 自动清理旧备份（保留最近3个）

**使用方法:**
1. 先构建前端:
   ```bash
   cd yudao-ui\yudao-ui-admin-vben
   pnpm install
   pnpm build:antd
   ```
2. 运行脚本: `deploy-frontend.bat`
3. 输入服务器密码
4. 等待部署完成

## 服务器配置

**服务器信息:**
- IP地址: 8.130.43.71
- 后端路径: /www/wwwroot/lvye-server
- 前端路径: /www/wwwroot/lvye-frontend

## 依赖要求

**必须安装WinSCP:**
- 下载地址: https://winscp.net
- 安装后确保WinSCP.com在系统PATH中

## 备份策略

### 后端备份
- 位置: `/www/wwwroot/lvye-server/temp/`
- 命名: `yudao-server.jar.YYYYMMDD_HHMMSS.bak`
- 每次部署前自动备份

### 前端备份
- 位置: `/www/wwwroot/lvye-frontend_backup/`
- 命名: `YYYYMMDD_HHMMSS/`
- 自动保留最近3个备份

## 注意事项

1. **首次使用:** 运行脚本时需要输入服务器密码
2. **构建要求:** 部署前确保已构建最新代码
3. **网络要求:** 确保能访问服务器的22端口
4. **权限要求:** 服务器用户需要有相应目录的读写权限

## 故障排查

如果部署失败，请检查:
1. WinSCP是否正确安装
2. 服务器密码是否正确
3. 网络连接是否正常
4. 目标目录权限是否正确
5. JAR文件或dist目录是否存在