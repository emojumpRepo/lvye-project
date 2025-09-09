@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

title 芋道后端部署到宝塔

:: ===== 服务器配置 =====
set SERVER_HOST=8.130.43.71
set SERVER_USER=root
set SERVER_PORT=22
set REMOTE_PATH=/www/wwwroot/lvye-server
set JAR_NAME=yudao-server.jar

:: ===== 本地路径 =====
set LOCAL_JAR=%~dp0..\..\yudao-server\target\%JAR_NAME%

echo ╔═══════════════════════════════════════╗
echo ║         芋道后端部署到宝塔             ║
echo ╚═══════════════════════════════════════╝
echo.
echo 服务器: %SERVER_HOST%
echo 目标路径: %REMOTE_PATH%
echo 本地文件: %LOCAL_JAR%
echo.

:: 检查本地JAR文件
if not exist "%LOCAL_JAR%" (
    echo [错误] JAR文件不存在，请先执行: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo [信息] 找到JAR文件，准备部署...

:: 询问服务器密码
set /p SERVER_PASSWORD="请输入服务器密码: "

:: 创建临时SFTP脚本
set SFTP_SCRIPT=%TEMP%\deploy_%RANDOM%.txt

(
echo open sftp://%SERVER_USER%:%SERVER_PASSWORD%@%SERVER_HOST%:%SERVER_PORT%
echo option batch abort
echo option confirm off
echo.
echo # 获取时间戳用于备份
echo call TIMESTAMP=$(date +%%Y%%m%%d_%%H%%M%%S^)
echo.
echo # 创建必要的目录
echo call mkdir -p %REMOTE_PATH%/temp
echo.
echo # 备份现有文件到temp目录
echo call if [ -f "%REMOTE_PATH%/%JAR_NAME%" ]; then cp "%REMOTE_PATH%/%JAR_NAME%" "%REMOTE_PATH%/temp/%JAR_NAME%.$TIMESTAMP.bak"; echo "[备份] 已备份到 temp/%JAR_NAME%.$TIMESTAMP.bak"; fi
echo.
echo # 停止运行中的服务
echo call PID=$(ps aux ^| grep "%JAR_NAME%" ^| grep -v grep ^| awk '{print $2}'^)
echo call if [ ! -z "$PID" ]; then kill -15 $PID; sleep 3; echo "[停止] 已停止服务 PID: $PID"; fi
echo.
echo # 上传新文件（先上传为临时文件名）
echo put "%LOCAL_JAR%" "%REMOTE_PATH%/%JAR_NAME%.new"
echo.
echo # 替换文件
echo call if [ -f "%REMOTE_PATH%/%JAR_NAME%.new" ]; then mv "%REMOTE_PATH%/%JAR_NAME%.new" "%REMOTE_PATH%/%JAR_NAME%"; echo "[上传] 文件部署成功"; fi
echo.
echo # 设置权限
echo call chmod 755 "%REMOTE_PATH%/%JAR_NAME%"
echo.
echo # 启动服务（使用宝塔的方式）
echo call cd %REMOTE_PATH%
echo call nohup java -jar %JAR_NAME% --spring.profiles.active=prod ^> logs/app.log 2^>^&1 ^&
echo call sleep 3
echo.
echo # 检查服务状态
echo call NEW_PID=$(ps aux ^| grep "%JAR_NAME%" ^| grep -v grep ^| awk '{print $2}'^)
echo call if [ ! -z "$NEW_PID" ]; then echo "[启动] 服务已启动 PID: $NEW_PID"; else echo "[警告] 服务可能未启动成功"; fi
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
echo [部署] 正在部署到服务器...
WinSCP.com /script="%SFTP_SCRIPT%"

if !errorlevel! equ 0 (
    echo.
    echo ╔═══════════════════════════════════════╗
    echo ║            部署成功完成！              ║
    echo ╚═══════════════════════════════════════╝
    echo.
    echo 服务器: %SERVER_HOST%
    echo 部署路径: %REMOTE_PATH%/%JAR_NAME%
    echo 备份位置: %REMOTE_PATH%/temp/
    echo.
) else (
    echo [错误] 部署失败，请检查日志
)

:: 清理临时文件
del "%SFTP_SCRIPT%" 2>nul

pause