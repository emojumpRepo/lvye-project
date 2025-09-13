#!/usr/bin/env node

// 曼朗-心之旅 前端部署脚本（SSH2 版本）
// 使用方法: node deploy-frontend-ssh2.mjs
// 需要先安装依赖: npm install ssh2 archiver readline-sync chalk

import { Client } from 'ssh2';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import readline from 'readline-sync';
import chalk from 'chalk';
import { exec, execSync } from 'child_process';
import { promisify } from 'util';

// 设置 Windows 控制台编码为 UTF-8
if (process.platform === 'win32') {
    try {
        execSync('chcp 65001', { stdio: 'ignore' });
    } catch (e) {
        // 忽略错误
    }
}

const execAsync = promisify(exec);
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 服务器配置
const SERVER_HOST = '42.194.163.176';
const SERVER_USER = 'root';
const SERVER_PORT = 22;

console.log(chalk.cyan('========================================'));
console.log(chalk.cyan('      曼朗-心之旅前端部署（SSH2版）'));
console.log(chalk.cyan('========================================'));
console.log();

// 选择项目
console.log('Please select the frontend project to deploy:');
console.log('');
console.log('  [1] Admin Management Backend');
console.log('  [2] Web User Frontend');
console.log('');

const projectChoice = readline.question('Please enter your choice (1 or 2): ');
const choice = projectChoice.trim();

let PROJECT_NAME, REMOTE_PATH, LOCAL_ZIP, BUILD_PATH;

switch (choice) {
    case '1':
        PROJECT_NAME = 'Admin Management Backend';
        REMOTE_PATH = '/root/mindfront/work/project/mindtrip_apps/admin';
        LOCAL_ZIP = path.join(__dirname, '../../yudao-ui/lvye-project-frontend/apps/admin/dist.zip');
        BUILD_PATH = 'yudao-ui/lvye-project-frontend/apps/admin';
        break;
    case '2':
        PROJECT_NAME = 'Web User Frontend';
        REMOTE_PATH = '/root/mindfront/work/project/mindtrip_apps/web';
        LOCAL_ZIP = path.join(__dirname, '../../yudao-ui/lvye-project-frontend/apps/web/dist.zip');
        BUILD_PATH = 'yudao-ui/lvye-project-frontend/apps/web';
        break;
    default:
        console.log(chalk.red('[Error] Invalid choice!'));
        process.exit(1);
}

console.log();
console.log('========================================');
console.log(`Selected: ${PROJECT_NAME}`);
console.log(`Server: ${SERVER_HOST}`);
console.log(`Target Path: ${REMOTE_PATH}`);
console.log(`Local File: ${LOCAL_ZIP}`);
console.log('========================================');
console.log();

// 检查本地ZIP文件
if (!fs.existsSync(LOCAL_ZIP)) {
    console.log(chalk.red('[Error] dist.zip file not found, please build frontend first'));
    console.log();
    console.log('Please enter the directory and build:');
    console.log(`  cd ${BUILD_PATH}`);
    console.log('  pnpm install');
    console.log('  pnpm build');
    console.log();
    console.log('Make sure dist.zip file is generated');
    process.exit(1);
}

console.log('[Info] Found dist.zip file, preparing to deploy...');

// 询问服务器密码
const password = readline.question('Please enter server password: ', { hideEchoBack: true });
if (!password) {
    console.log(chalk.red('[Error] Password cannot be empty'));
    process.exit(1);
}

// 创建SSH连接并部署
const conn = new Client();

function deployWithSSH() {
    return new Promise((resolve, reject) => {
        conn.on('ready', () => {
            console.log('[Connect] SSH connection successful');

            // 获取时间戳
            const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace('T', '_').split('.')[0];

            // 执行部署命令序列
            const commands = [
                // 创建必要的目录
                `mkdir -p "${REMOTE_PATH}"`,
                `mkdir -p "${REMOTE_PATH}_backup"`,
                `mkdir -p /tmp/frontend_deploy`,

                // 备份现有的 dist.zip（如果存在）
                `if [ -f "${REMOTE_PATH}/dist.zip" ]; then cp "${REMOTE_PATH}/dist.zip" "${REMOTE_PATH}_backup/dist_${timestamp}.zip"; echo "[Backup] Backed up dist.zip to ${REMOTE_PATH}_backup/dist_${timestamp}.zip"; fi`,

                // 清理目标目录中的所有文件
                `if [ -d "${REMOTE_PATH}" ]; then rm -rf "${REMOTE_PATH}"/*; echo "[Clean] Target directory cleaned"; fi`
            ];

            // 执行命令序列
            let commandIndex = 0;

            function executeNextCommand() {
                if (commandIndex >= commands.length) {
                    // 开始上传文件
                    uploadFile();
                    return;
                }

                const cmd = commands[commandIndex++];
                conn.exec(cmd, (err, stream) => {
                    if (err) {
                        reject(err);
                        return;
                    }

                    stream.on('close', (code) => {
                        if (code !== 0 && code !== null) {
                            console.log(chalk.yellow(`[Warning] Command return code: ${code}`));
                        }
                        executeNextCommand();
                    }).on('data', (data) => {
                        const output = data.toString().trim();
                        if (output) console.log(output);
                    }).stderr.on('data', (data) => {
                        const error = data.toString().trim();
                        if (error && !error.includes('warning')) {
                            console.log(chalk.yellow(`[Warning] ${error}`));
                        }
                    });
                });
            }

            function uploadFile() {
                conn.sftp((err, sftp) => {
                    if (err) {
                        reject(err);
                        return;
                    }

                    console.log('[Upload] Starting to upload ZIP file...');
                    const fileSize = fs.statSync(LOCAL_ZIP).size;
                    let uploaded = 0;

                    const readStream = fs.createReadStream(LOCAL_ZIP);
                    const writeStream = sftp.createWriteStream('/tmp/frontend_deploy/dist.zip');

                    readStream.on('data', (chunk) => {
                        uploaded += chunk.length;
                        const percent = Math.round((uploaded / fileSize) * 100);
                        process.stdout.write(`\r[Upload] Progress: ${percent}% (${(uploaded / 1024 / 1024).toFixed(2)}MB / ${(fileSize / 1024 / 1024).toFixed(2)}MB)`);
                    });

                    writeStream.on('close', () => {
                        console.log('\n[Upload] File upload completed');

                        // 解压和部署
                        const deployCommands = [
                            // 先解压文件
                            'cd /tmp/frontend_deploy && unzip -o dist.zip',
                            // 检查解压结果并复制到目标目录
                            `if [ -d "/tmp/frontend_deploy/dist" ]; then ` +
                            `cp -r /tmp/frontend_deploy/dist/* "${REMOTE_PATH}/"; ` +
                            `echo "[Copy] Copied dist directory contents to ${REMOTE_PATH}"; ` +
                            `elif [ -f "/tmp/frontend_deploy/index.html" ]; then ` +
                            `cp -r /tmp/frontend_deploy/* "${REMOTE_PATH}/"; ` +
                            `echo "[Copy] Copied extracted contents to ${REMOTE_PATH}"; ` +
                            `else ` +
                            `echo "[Error] Expected file structure not found after extraction"; ` +
                            `ls -la /tmp/frontend_deploy/; ` +
                            `fi`,
                            // 复制原始zip文件到目标目录（保留一份）
                            `cp /tmp/frontend_deploy/dist.zip "${REMOTE_PATH}/dist.zip" && echo "[Save] Saved dist.zip to target directory"`,
                            // 清理临时文件
                            'rm -rf /tmp/frontend_deploy/*',
                            // 设置权限
                            `chmod -R 755 "${REMOTE_PATH}"`,
                            `chown -R www:www "${REMOTE_PATH}"`,
                            // 验证部署
                            `if [ -f "${REMOTE_PATH}/index.html" ]; then ` +
                            `echo "[Success] Frontend deployment completed"; ` +
                            `echo "[Files] Target directory contains:"; ` +
                            `ls -la "${REMOTE_PATH}/" | head -10; ` +
                            `else ` +
                            `echo "[Warning] Deployment may be incomplete, target directory contents:"; ` +
                            `ls -la "${REMOTE_PATH}/"; ` +
                            `fi`,
                            // 清理旧备份（保留最近3个）
                            `cd "${REMOTE_PATH}_backup" && ls -t | tail -n +4 | xargs -r rm -rf && echo "[Cleanup] Old backups cleaned, keeping recent 3"`
                        ];

                        let deployIndex = 0;

                        function executeDeployCommand() {
                            if (deployIndex >= deployCommands.length) {
                                conn.end();
                                resolve();
                                return;
                            }

                            const cmd = deployCommands[deployIndex++];
                            conn.exec(cmd, (err, stream) => {
                                if (err) {
                                    reject(err);
                                    return;
                                }

                                stream.on('close', () => {
                                    executeDeployCommand();
                                }).on('data', (data) => {
                                    const output = data.toString().trim();
                                    if (output) console.log(output);
                                }).stderr.on('data', (data) => {
                                    const error = data.toString().trim();
                                    if (error && !error.includes('warning')) {
                                        console.log(chalk.yellow(`[Warning] ${error}`));
                                    }
                                });
                            });
                        }

                        executeDeployCommand();
                    });

                    writeStream.on('error', reject);
                    readStream.pipe(writeStream);
                });
            }

            executeNextCommand();
        });

        conn.on('error', (err) => {
            reject(err);
        });

        // 连接到服务器
        conn.connect({
            host: SERVER_HOST,
            port: SERVER_PORT,
            username: SERVER_USER,
            password: password
        });
    });
}

// 执行部署
deployWithSSH()
    .then(() => {
        console.log();
        console.log(chalk.green('========================================'));
        console.log(chalk.green('    Frontend Deployment Successful!'));
        console.log(chalk.green('========================================'));
        console.log();
        console.log(`Project: ${PROJECT_NAME}`);
        console.log(`Server: ${SERVER_HOST}`);
        console.log(`Deploy Path: ${REMOTE_PATH}`);
        console.log(`Backup Location: ${REMOTE_PATH}_backup/`);
        console.log();

        if (choice === '1') {
            console.log(`Access URL: http://${SERVER_HOST}/admin/`);
        } else {
            console.log(`Access URL: http://${SERVER_HOST}/`);
        }
        console.log();
    })
    .catch((err) => {
        console.log();
        console.log(chalk.red('[Error] Deployment failed:'), err.message);
        process.exit(1);
    });