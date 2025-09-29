# 曼朗-心之旅项目部署脚本说明（zx 版本）

本目录下的部署脚本已统一为 zx（JavaScript）脚本，避免了 bat/ps1 的转义与编码问题。

## 可用脚本

- 前端：script/windows/deploy-frontend.mjs
- 后端：script/windows/deploy-backend.mjs

建议使用根目录的 npm scripts 来执行：
- 前端：`npm run deploy:frontend`
- 后端：`npm run deploy:backend`

也可直接使用 npx：
- `npx zx script/windows/deploy-frontend.mjs`
- `npx zx script/windows/deploy-backend.mjs`

## 服务器与路径（与脚本保持一致）

- 服务器 IP：42.194.163.176
- 前端路径：
  - Admin 管理后台：`/work/project/mindtrip_apps/admin`
  - Web 用户端：`/work/project/mindtrip_apps/web`
- 后端路径：后端脚本运行时会询问，默认：`/www/wwwroot/lvye-server`（可按需修改）

## 本地构建产物位置

- 前端 ZIP：
  - Admin：`yudao-ui/mindtrip-project-frontend/apps/admin/dist.zip`
  - Web：`yudao-ui/mindtrip-project-frontend/apps/web/dist.zip`
- 后端 JAR：`yudao-server/target/yudao-server.jar`

## 依赖要求

- Node.js（建议 16+）与 zx（脚本通过 npx 调用，无需全局安装）
- WinSCP：需安装，脚本会优先使用 WinSCP.com；若仅有 winscp.exe，则自动加 `/console`
  - Windows（推荐）：`scoop install winscp`
  - 官网下载：https://winscp.net
- 服务器需安装 unzip（用于前端解压）和 Java（用于后端运行）

## 前端部署流程（脚本自动完成）
1. 检查本地 dist.zip 是否存在
2. 连接服务器并创建必要目录
3. 备份现有文件到 `_backup/时间戳` 目录，仅保留最近 3 个
4. 清理目标目录旧文件
5. 上传 dist.zip 到临时目录并在服务器解压
6. 拷贝解压结果到目标目录
7. 设置权限（755 与 www:www）

## 后端部署流程（脚本自动完成）
1. 检查本地 JAR 是否存在
2. 连接服务器，准备目录与 temp
3. 停止旧的 Java 进程（TERM → KILL）
4. 备份旧 JAR 到 temp（带时间戳）
5. 上传新的 JAR 为 .new 并原子替换
6. 启动服务（`nohup java -jar`，可按需修改参数）
7. 简单校验进程是否启动

## 常用访问地址

- Admin 管理后台：http://42.194.163.176/admin/
- Web 用户端：http://42.194.163.176/

## 常见问题

- “未找到 WinSCP”：请先安装，并确保 PATH 中包含 WinSCP.com；脚本也会尝试常见安装路径
- “日志文件不存在”：之前使用 winscp.exe（GUI）执行脚本可能不产生日志，现已改为优先 WinSCP.com 或对 exe 自动加 `/console`
- 中文乱码：请使用支持 UTF-8 的终端/PowerShell；zx 输出为 UTF-8

## 使用示例

- 前端：
  ```bash
  # Admin
  cd yudao-ui/mindtrip-project-frontend/apps/admin && pnpm install && pnpm build  # 生成 dist.zip
  cd ../../../..
  npm run deploy:frontend
  # 选择 1（Admin），输入服务器密码，等待完成
  ```

- 后端：
  ```bash
  mvn -f yudao-server clean package -DskipTests  # 生成 yudao-server.jar
  npm run deploy:backend
  # 输入远程目录（或回车使用默认），输入服务器密码，等待完成
  ```
