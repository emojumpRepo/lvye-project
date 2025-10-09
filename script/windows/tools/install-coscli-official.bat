@echo off
chcp 65001 >nul
REM COSCLI Official Installation Script
REM Following Tencent Cloud Official Documentation
REM Author: Mindtrip Team

echo === COSCLI 官方安装方式 ===
echo.

REM Get current user directory
set USER_DIR=%USERPROFILE%
echo 当前用户目录: %USER_DIR%
echo.

REM Check if already installed
if exist "%USER_DIR%\coscli.exe" (
    echo 检测到已安装的 COSCLI
    "%USER_DIR%\coscli.exe" --version
    echo.
    set /p REINSTALL="是否要重新安装? (y/n): "
    if not "!REINSTALL!"=="y" if not "!REINSTALL!"=="Y" (
        echo 跳过安装，直接进行配置...
        goto :configure
    )
)

echo 按照腾讯云官方文档进行安装...
echo.

REM Manual download instructions (following official documentation)
echo ========================================
echo 手动下载 COSCLI（官方推荐方式）
echo ========================================
echo.
echo 请按照以下步骤操作：
echo.
echo 1. 打开浏览器访问：
echo    https://github.com/tencentyun/coscli/releases
echo.
echo 2. 下载正确的版本：
echo    - Windows 64位: coscli-windows-amd64.exe
echo    - Windows 32位: coscli-windows-386.exe
echo.
echo 3. 将下载的文件重命名为: coscli.exe
echo.
echo 4. 将文件移动到: %USER_DIR%\coscli.exe
echo.
echo 官方文档链接：
echo https://cloud.tencent.com/document/product/436/63143
echo.

set /p DOWNLOADED="完成下载后按回车继续..."

REM Verify download
if not exist "%USER_DIR%\coscli.exe" (
    echo.
    echo 错误: 在 %USER_DIR% 未找到 coscli.exe
    echo.
    echo 请确保：
    echo 1. 文件已下载并重命名为 coscli.exe
    echo 2. 文件位置：%USER_DIR%\coscli.exe
    echo.
    pause
    exit /b 1
)

echo.
echo 成功: COSCLI 已安装到用户目录
echo 位置: %USER_DIR%\coscli.exe
echo.

REM Verify installation
echo 验证安装...
"%USER_DIR%\coscli.exe" --version
if %errorlevel%==0 (
    echo 安装验证成功！
) else (
    echo 错误: 安装验证失败
    pause
    exit /b 1
)

:configure
echo.
echo === 配置 COSCLI 认证信息 ===
echo.
echo 请设置环境变量：
echo set TENCENT_SECRET_ID=your_secret_id
echo set TENCENT_SECRET_KEY=your_secret_key
echo Region: ap-guangzhou
echo Bucket: mindtrip-1305613707
echo.
if "%TENCENT_SECRET_ID%"=="" (
    echo 错误: 请设置 TENCENT_SECRET_ID 环境变量
    pause
    exit /b 1
)
if "%TENCENT_SECRET_KEY%"=="" (
    echo 错误: 请设置 TENCENT_SECRET_KEY 环境变量
    pause
    exit /b 1
)
echo.

REM Create configuration file
set CONFIG_FILE=%USER_DIR%\.cos.yaml

echo 创建配置文件: %CONFIG_FILE%
echo.

(
echo cos:
echo   base:
echo     secretid: %TENCENT_SECRET_ID%
echo     secretkey: %TENCENT_SECRET_KEY%
echo     sessiontoken: ""
echo     protocol: https
echo   buckets:
echo   - name: mindtrip-1305613707
echo     alias: mindtrip
echo     region: ap-guangzhou
echo     endpoint: ""
) > "%CONFIG_FILE%"

echo 配置文件创建成功
echo.

REM Test configuration
echo 测试配置...
"%USER_DIR%\coscli.exe" config show
if %errorlevel% neq 0 (
    echo 错误: 配置验证失败
    pause
    exit /b 1
)

echo.
echo 配置验证成功
echo.

REM Test bucket connection
echo 测试存储桶连接...
"%USER_DIR%\coscli.exe" ls cos://mindtrip-1305613707/
if %errorlevel% equ 0 (
    echo 存储桶连接成功
) else (
    echo 警告: 存储桶连接失败
    echo 这可能是由于网络问题或权限问题
)

echo.

REM Test file operations
echo 测试文件上传功能...
echo COSCLI test file - %date% %time% > test-file.txt
"%USER_DIR%\coscli.exe" cp test-file.txt cos://mindtrip-1305613707/test/
if %errorlevel% equ 0 (
    echo 文件上传成功
    
    REM Test download
    "%USER_DIR%\coscli.exe" cp cos://mindtrip-1305613707/test/test-file.txt test-file-download.txt
    if %errorlevel% equ 0 (
        echo 文件下载成功
        
        REM Clean up test files
        "%USER_DIR%\coscli.exe" rm cos://mindtrip-1305613707/test/test-file.txt >nul 2>&1
        del test-file.txt test-file-download.txt >nul 2>&1
        echo 测试文件已清理
    ) else (
        echo 警告: 文件下载测试失败
    )
) else (
    echo 警告: 文件上传测试失败
    echo 请检查账号权限
)

echo.
echo === 安装配置完成 ===
echo.
echo COSCLI 已成功安装并配置！
echo 安装位置: %USER_DIR%\coscli.exe
echo 配置文件: %CONFIG_FILE%
echo.

echo 常用命令（需要使用完整路径）：
echo - 查看配置: "%USER_DIR%\coscli.exe" config show
echo - 列出文件: "%USER_DIR%\coscli.exe" ls cos://mindtrip-1305613707/
echo - 上传文件: "%USER_DIR%\coscli.exe" cp localfile cos://mindtrip-1305613707/path/
echo.

echo 项目部署命令：
echo 1. 构建项目: pnpm build:admin
echo 2. 测试上传: pnpm run deploy:admin-only
echo 3. 完整部署: pnpm run deploy
echo.

echo 注意事项：
echo - 如果 coscli 命令无法直接使用，请使用完整路径
echo - 或者将 %USER_DIR% 添加到系统 PATH 环境变量
echo.

pause