@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

title 芋道前端部署到宝塔

:: ===== 服务器配置 =====
set SERVER_HOST=8.130.43.71
set SERVER_USER=root
set SERVER_PORT=22
set REMOTE_PATH=/www/wwwroot/lvye-frontend

:: ===== 本地路径 =====
set LOCAL_DIST=%~dp0..\..\yudao-ui\yudao-ui-admin-vben\dist

echo ╔═══════════════════════════════════════╗
echo ║         芋道前端部署到宝塔             ║
echo ╚═══════════════════════════════════════╝
echo.
echo 服务器: %SERVER_HOST%
echo 目标路径: %REMOTE_PATH%
echo 本地文件: %LOCAL_DIST%
echo.

:: 检查本地dist目录
if not exist "%LOCAL_DIST%" (
    echo [错误] dist目录不存在，请先执行前端构建
    echo.
    echo 请进入前端目录执行:
    echo   cd yudao-ui\yudao-ui-admin-vben
    echo   pnpm install
    echo   pnpm build:antd
    echo.
    pause
    exit /b 1
)

echo [信息] 找到dist目录，准备部署...

:: 询问服务器密码
set /p SERVER_PASSWORD="请输入服务器密码: "

:: 创建临时SFTP脚本
set SFTP_SCRIPT=%TEMP%\deploy_frontend_%RANDOM%.txt

(
echo open sftp://%SERVER_USER%:%SERVER_PASSWORD%@%SERVER_HOST%:%SERVER_PORT%
echo option batch abort
echo option confirm off
echo.
echo # 获取时间戳用于备份
echo call TIMESTAMP=$(date +%%Y%%m%%d_%%H%%M%%S^)
echo.
echo # 创建必要的目录
echo call mkdir -p %REMOTE_PATH%
echo call mkdir -p %REMOTE_PATH%_backup
echo.
echo # 备份现有文件
echo call if [ -d "%REMOTE_PATH%" ] ^&^& [ "$(ls -A %REMOTE_PATH%^)" ]; then echo "[备份] 开始备份现有文件..."; cp -r %REMOTE_PATH% %REMOTE_PATH%_backup/$TIMESTAMP; echo "[备份] 已备份到 %REMOTE_PATH%_backup/$TIMESTAMP"; fi
echo.
echo # 清理旧文件（保留备份）
echo call if [ -d "%REMOTE_PATH%" ]; then rm -rf %REMOTE_PATH%/*; echo "[清理] 已清理旧文件"; fi
echo.
echo # 上传新文件
echo synchronize remote "%LOCAL_DIST%\" "%REMOTE_PATH%/"
echo.
echo # 设置权限
echo call chmod -R 755 %REMOTE_PATH%
echo call chown -R www:www %REMOTE_PATH%
echo.
echo # 验证上传
echo call if [ -f "%REMOTE_PATH%/index.html" ]; then echo "[成功] 前端文件部署完成"; else echo "[警告] 可能部署不完整，请检查"; fi
echo.
echo # 清理旧备份（保留最近3个）
echo call cd %REMOTE_PATH%_backup ^&^& ls -t ^| tail -n +4 ^| xargs -r rm -rf
echo call echo "[清理] 已清理旧备份，保留最近3个"
echo.
echo close
echo exit
) > "%SFTP_SCRIPT%"

:: 检查WinSCP是否安装
where WinSCP.com >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到WinSCP，请先安装: https://winscp.net
    del "%SFTP_SCRIPT%" 2>nul
    pause
    exit /b 1
)

:: 执行部署
echo [部署] 正在部署前端文件到服务器...
echo [提示] 这可能需要几分钟时间，请耐心等待...
WinSCP.com /script="%SFTP_SCRIPT%"

if !errorlevel! equ 0 (
    echo.
    echo ╔═══════════════════════════════════════╗
    echo ║          前端部署成功完成！            ║
    echo ╚═══════════════════════════════════════╝
    echo.
    echo 服务器: %SERVER_HOST%
    echo 部署路径: %REMOTE_PATH%
    echo 备份位置: %REMOTE_PATH%_backup/
    echo.
    echo 访问地址: http://%SERVER_HOST%/
    echo.
) else (
    echo [错误] 部署失败，请检查日志
)

:: 清理临时文件
del "%SFTP_SCRIPT%" 2>nul

pause