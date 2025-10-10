@echo off
chcp 65001 >nul
REM COSCLI Quick Configuration Script for Project Root
REM Author: Mindtrip Team

echo === COSCLI 快速配置（项目根目录版本）===
echo.

REM Get current user directory
set USER_DIR=%USERPROFILE%
set COSCLI_PATH=%USER_DIR%\coscli.exe

echo COSCLI 路径: %COSCLI_PATH%
echo.

REM Check if COSCLI is installed in user directory
if not exist "%COSCLI_PATH%" (
    echo 错误: 在用户目录中未找到 COSCLI
    echo 请先运行安装脚本: script\windows\tools\install-coscli-official.bat
    pause
    exit /b 1
)

echo COSCLI 已安装
"%COSCLI_PATH%" --version
echo.

REM Configuration details for Mindtrip project - using environment variables
if "%TENCENT_SECRET_ID%"=="" (
    echo 错误: 请设置 TENCENT_SECRET_ID 环境变量
    echo 运行: set TENCENT_SECRET_ID=your_secret_id
    pause
    exit /b 1
)
if "%TENCENT_SECRET_KEY%"=="" (
    echo 错误: 请设置 TENCENT_SECRET_KEY 环境变量
    echo 运行: set TENCENT_SECRET_KEY=your_secret_key
    pause
    exit /b 1
)

set SECRETID=%TENCENT_SECRET_ID%
set SECRETKEY=%TENCENT_SECRET_KEY%
set REGION=ap-guangzhou
set BUCKET=mindtrip-1305613707

echo 配置 Mindtrip 项目认证信息...
echo SecretId: %SECRETID%
echo Region: %REGION%
echo Bucket: %BUCKET%
echo.

REM Create configuration file
set CONFIG_FILE=%USER_DIR%\.cos.yaml

echo 创建配置文件: %CONFIG_FILE%

(
echo cos:
echo   base:
echo     secretid: %SECRETID%
echo     secretkey: %SECRETKEY%
echo     sessiontoken: ""
echo     protocol: https
echo   buckets:
echo   - name: %BUCKET%
echo     alias: mindtrip
echo     region: %REGION%
echo     endpoint: ""
) > "%CONFIG_FILE%"

echo 配置文件创建成功
echo.

REM Test configuration
echo 测试 COSCLI 配置...
"%COSCLI_PATH%" config show
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
"%COSCLI_PATH%" ls cos://%BUCKET%/
if %errorlevel% equ 0 (
    echo 存储桶连接成功
) else (
    echo 警告: 存储桶连接失败
    echo 这可能是由于网络问题或权限问题
)

echo.
echo === 配置完成 ===
echo.
echo COSCLI 已成功配置用于 Mindtrip 项目！
echo.

echo 现在可以使用项目部署脚本：
echo   npm run deploy:frontend:cos  # COS+CDN 部署
echo   npm run deploy:frontend      # 传统部署
echo.

echo 或直接使用：
echo   npx zx script\windows\deploy-frontend-cos.mjs
echo.

pause