#!/usr/bin/env node

// Mindtrip Frontend Deployment Script (SSH2 Version)
// Usage: node deploy-frontend-ssh2.mjs
// Dependencies: npm install ssh2 archiver readline-sync chalk

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
console.log(chalk.cyan('   Mindtrip Frontend Deployment (SSH2)'));
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

let PROJECT_NAME, REMOTE_PATH, NGINX_PATH, LOCAL_ZIP, BUILD_PATH;

switch (choice) {
    case '1':
        PROJECT_NAME = 'Admin Management Backend';
        REMOTE_PATH = '/root/mindfront/work/project/mindtrip_apps/admin';
        NGINX_PATH = '/root/mindfront/work/nginx/html/admin';
        LOCAL_ZIP = path.join(__dirname, '../../yudao-ui/lvye-project-frontend/apps/admin/dist.zip');
        BUILD_PATH = 'yudao-ui/lvye-project-frontend/apps/admin';
        break;
    case '2':
        PROJECT_NAME = 'Web User Frontend';
        REMOTE_PATH = '/root/mindfront/work/project/mindtrip_apps/web';
        NGINX_PATH = '/root/mindfront/work/nginx/html/web';
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
console.log(`Nginx Path: ${NGINX_PATH}`);
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

// 询问服务器密码（提供默认值）
console.log('[Info] Using default password, press Enter to continue or input custom password:');
const password = readline.question('Server password (default: MF@Luye!996): ', { hideEchoBack: true }) || 'MF@Luye!996';
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
                `mkdir -p "${NGINX_PATH}_backup"`,
                `mkdir -p /tmp/frontend_deploy`,
                `mkdir -p /root/mindfront/work/nginx/html`,

                // Backup existing dist.zip if it exists
                `if [ -f "${REMOTE_PATH}/dist.zip" ]; then cp "${REMOTE_PATH}/dist.zip" "${REMOTE_PATH}_backup/dist_${timestamp}.zip"; echo "[Backup] Created project backup: ${REMOTE_PATH}_backup/dist_${timestamp}.zip"; fi`,
                
                // Backup existing nginx directory if it exists
                `if [ -d "${NGINX_PATH}" ]; then cp -r "${NGINX_PATH}" "${NGINX_PATH}_backup/backup_${timestamp}"; echo "[Backup] Created nginx backup: ${NGINX_PATH}_backup/backup_${timestamp}"; fi`,
                
                // Clean old project backups (keep only last 4)
                `cd "${REMOTE_PATH}_backup" 2>/dev/null && ls -t dist_*.zip 2>/dev/null | tail -n +5 | xargs -r rm -f && echo "[Cleanup] Old project backups cleaned, keeping last 4"`,
                
                // Clean old nginx backups (keep only last 4)
                `cd "${NGINX_PATH}_backup" 2>/dev/null && ls -dt backup_* 2>/dev/null | tail -n +5 | xargs -r rm -rf && echo "[Cleanup] Old nginx backups cleaned, keeping last 4"`,

                // Clean target directory
                `if [ -d "${REMOTE_PATH}" ]; then rm -rf "${REMOTE_PATH}"/*; echo "[Clean] Project directory cleaned"; fi`,
                
                // Clean nginx directory
                `if [ -d "${NGINX_PATH}" ]; then rm -rf "${NGINX_PATH}"/*; echo "[Clean] Nginx directory cleaned"; fi`
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

                        // Extract and deploy
                        const deployCommands = [
                            // Extract files
                            'cd /tmp/frontend_deploy && unzip -o dist.zip',
                            // Check extraction result and copy to target directory
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
                            // Save original zip file
                            `cp /tmp/frontend_deploy/dist.zip "${REMOTE_PATH}/dist.zip" && echo "[Save] Saved dist.zip to project directory"`,
                            
                            // Copy to nginx directory
                            `echo "[Nginx] Starting to copy files to nginx directory..."`,
                            `if [ -f "${REMOTE_PATH}/index.html" ]; then ` +
                            `cp -r "${REMOTE_PATH}"/* "${NGINX_PATH}/"; ` +
                            `echo "[Nginx] Successfully copied files to ${NGINX_PATH}"; ` +
                            `else ` +
                            `echo "[Error] No index.html found in project directory, nginx copy skipped"; ` +
                            `fi`,
                            
                            // Clean temporary files
                            'rm -rf /tmp/frontend_deploy/*',
                            
                            // Set permissions
                            `chmod -R 755 "${REMOTE_PATH}"`,
                            `chown -R www:www "${REMOTE_PATH}"`,
                            `chmod -R 755 "${NGINX_PATH}"`,
                            `chown -R www:www "${NGINX_PATH}"`,
                            
                            // Verify deployment
                            `if [ -f "${REMOTE_PATH}/index.html" ] && [ -f "${NGINX_PATH}/index.html" ]; then ` +
                            `echo "[Success] Frontend deployment completed successfully"; ` +
                            `echo "[Files] Project directory contains:"; ` +
                            `ls -la "${REMOTE_PATH}/" | head -5; ` +
                            `echo "[Files] Nginx directory contains:"; ` +
                            `ls -la "${NGINX_PATH}/" | head -5; ` +
                            `else ` +
                            `echo "[Warning] Deployment may be incomplete"; ` +
                            `echo "[Check] Project directory:"; ` +
                            `ls -la "${REMOTE_PATH}/"; ` +
                            `echo "[Check] Nginx directory:"; ` +
                            `ls -la "${NGINX_PATH}/"; ` +
                            `fi`
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

        // Connect to server
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
        console.log(`Nginx Path: ${NGINX_PATH}`);
        console.log(`Project Backup: ${REMOTE_PATH}_backup/`);
        console.log(`Nginx Backup: ${NGINX_PATH}_backup/`);
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