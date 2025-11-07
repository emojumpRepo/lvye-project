#!/usr/bin/env node

// Mindtrip Backend Deployment Script (SSH2 Version)
// Usage: node script/windows/deploy-backend.mjs
// or: npm run deploy:backend

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

// 本地 JAR 路径
const LOCAL_JAR = path.join(__dirname, '../../mindtrip-server/target/mindtrip-server.jar');
const JAR_NAME = 'mindtrip-server.jar';

console.log(chalk.cyan('========================================'));
console.log(chalk.cyan('    Mindtrip Backend Deployment (SSH2)'));
console.log(chalk.cyan('========================================'));
console.log();

// Ask for remote deployment directory with default value
let REMOTE_DIR = readline.question('Enter backend remote directory (default: /root/mindfront/work/project/mindtrip_server): ');
if (!REMOTE_DIR.trim()) REMOTE_DIR = '/root/mindfront/work/project/mindtrip_server';

// Ask for deployment method
console.log();
console.log('Select deployment method:');
console.log('  [1] Use deploy.sh script (default)');
console.log('  [2] Direct Docker management (stop, rebuild, restart)');
const deployMethod = readline.question('Enter choice (1 or 2, default: 1): ').trim() || '1';

console.log();
console.log('========================================');
console.log(`Server: ${SERVER_HOST}`);
console.log(`Remote directory: ${REMOTE_DIR}`);
console.log(`Local JAR: ${LOCAL_JAR}`);
console.log('========================================');
console.log();

// Check if local JAR exists
if (!fs.existsSync(LOCAL_JAR)) {
    console.log(chalk.red('[Error] Local JAR file not found, please build first:'));
    console.log('  mvn -f mindtrip-server clean package -DskipTests');
    process.exit(1);
}

console.log('[Info] JAR file found, preparing deployment...');

// Ask for server password with default value
console.log('[Info] Press Enter to use default password or input custom password:');
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
                `mkdir -p "${REMOTE_DIR}"`,
                `mkdir -p "${REMOTE_DIR}/backup"`,
                `mkdir -p "${REMOTE_DIR}/build"`,
                `mkdir -p "${REMOTE_DIR}/target"`,

                // Backup existing JAR
                `if [ -f "${REMOTE_DIR}/${JAR_NAME}" ]; then cp "${REMOTE_DIR}/${JAR_NAME}" "${REMOTE_DIR}/backup/${JAR_NAME}.${timestamp}.bak"; echo "[Backup] Created backup: backup/${JAR_NAME}.${timestamp}.bak"; fi`,
                
                // Clean old backups (keep only last 4)
                `cd "${REMOTE_DIR}/backup" 2>/dev/null && ls -t ${JAR_NAME}.*.bak 2>/dev/null | tail -n +5 | xargs -r rm -f && echo "[Cleanup] Old backups cleaned, keeping last 4"`
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

                    console.log('[Upload] Starting to upload JAR file...');
                    const fileSize = fs.statSync(LOCAL_JAR).size;
                    let uploaded = 0;

                    const readStream = fs.createReadStream(LOCAL_JAR);
                    const writeStream = sftp.createWriteStream(`${REMOTE_DIR}/target/${JAR_NAME}.new`);

                    readStream.on('data', (chunk) => {
                        uploaded += chunk.length;
                        const percent = Math.round((uploaded / fileSize) * 100);
                        process.stdout.write(`\r[Upload] Progress: ${percent}% (${(uploaded / 1024 / 1024).toFixed(2)}MB / ${(fileSize / 1024 / 1024).toFixed(2)}MB)`);
                    });

                    writeStream.on('close', () => {
                        console.log('\n[Upload] File upload completed');

                        let deployCommands;
                        
                        if (deployMethod === '2') {
                            // Direct Docker management
                            deployCommands = [
                                // Atomic replacement of JAR in target directory
                                `mv -f "${REMOTE_DIR}/target/${JAR_NAME}.new" "${REMOTE_DIR}/target/${JAR_NAME}"`,
                                // Copy to build directory (for consistency)
                                `cp -f "${REMOTE_DIR}/target/${JAR_NAME}" "${REMOTE_DIR}/build/${JAR_NAME}"`,
                                // IMPORTANT: Also copy to root directory (where Dockerfile expects it)
                                `cp -f "${REMOTE_DIR}/target/${JAR_NAME}" "${REMOTE_DIR}/${JAR_NAME}"`,
                                `echo "[Copy] JAR file copied to: ${REMOTE_DIR}/${JAR_NAME}"`,
                                
                                // Stop existing container first
                                `echo "[Docker] Stopping existing container..." && docker stop mindtrip-server 2>/dev/null || true`,
                                `docker rm mindtrip-server 2>/dev/null || true`,
                                
                                // Build Docker image with no cache to ensure fresh build
                                `cd "${REMOTE_DIR}" && echo "[Docker] Building fresh image (no cache)..." && docker build --no-cache -t mindtrip-server .`,
                                
                                // Run new container
                                `echo "[Docker] Starting new container..." && docker run -d --name mindtrip-server --restart=always -p 48080:48080 mindtrip-server:latest`,
                                
                                // Verify container is running
                                `sleep 3 && docker ps | grep mindtrip-server && echo "[Success] Container is running" || echo "[Error] Container failed to start"`
                            ];
                        } else {
                            // Use deploy.sh script (original method)
                            deployCommands = [
                                // Atomic replacement of JAR in target directory
                                `mv -f "${REMOTE_DIR}/target/${JAR_NAME}.new" "${REMOTE_DIR}/target/${JAR_NAME}"`,
                                // Copy to build directory (for deploy.sh)
                                `cp -f "${REMOTE_DIR}/target/${JAR_NAME}" "${REMOTE_DIR}/build/${JAR_NAME}"`,
                                // Also copy to root directory to be safe
                                `cp -f "${REMOTE_DIR}/target/${JAR_NAME}" "${REMOTE_DIR}/${JAR_NAME}"`,
                                // Build Docker image
                                `cd "${REMOTE_DIR}" && echo "[Docker] Building image..." && docker build -t mindtrip-server .`,
                                // Execute deployment script
                                `cd "${REMOTE_DIR}" && chmod +x deploy.sh && echo "[Deploy] Running deploy.sh..." && IMAGE=mindtrip-server:latest bash deploy.sh`
                            ];
                        }

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
        console.log(chalk.green('    Backend Deployment Successful!'));
        console.log(chalk.green('========================================'));
        console.log();
        console.log(`Server: ${SERVER_HOST}`);
        console.log(`Remote directory: ${REMOTE_DIR}`);
        console.log(`Backup location: ${REMOTE_DIR}/backup/`);
        console.log();
        console.log(`View service status with:`);
        console.log(`  docker logs -f mindtrip-server`);
        console.log();
    })
    .catch((err) => {
        console.log();
        console.log(chalk.red('[Error] Deployment failed:'), err.message);
        process.exit(1);
    });

