#!/usr/bin/env node

// 曼朗-心之旅 后端部署脚本（SSH2 版本）
// 使用方法: node script/windows/deploy-backend.mjs
// 或: npm run deploy:backend

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
const LOCAL_JAR = path.join(__dirname, '../../yudao-server/target/yudao-server.jar');
const JAR_NAME = 'yudao-server.jar';

console.log(chalk.cyan('========================================'));
console.log(chalk.cyan('      曼朗-心之旅后端部署（SSH2版）'));
console.log(chalk.cyan('========================================'));
console.log();

// 询问远程部署目录，提供常见默认值
let REMOTE_DIR = readline.question('请输入后端远程目录（默认: /root/mindfront/work/project/mindtrip_server）: ');
if (!REMOTE_DIR.trim()) REMOTE_DIR = '/root/mindfront/work/project/mindtrip_server';

console.log();
console.log('========================================');
console.log(`服务器: ${SERVER_HOST}`);
console.log(`远程目录: ${REMOTE_DIR}`);
console.log(`本地JAR: ${LOCAL_JAR}`);
console.log('========================================');
console.log();

// 检查本地 JAR 是否存在
if (!fs.existsSync(LOCAL_JAR)) {
    console.log(chalk.red('[Error] 未找到本地 JAR 文件，请先构建：'));
    console.log('  mvn -f yudao-server clean package -DskipTests');
    process.exit(1);
}

console.log('[Info] 找到JAR文件，准备部署...');

// 询问服务器密码
const password = readline.question('请输入服务器密码: ', { hideEchoBack: true });
if (!password) {
    console.log(chalk.red('[Error] 密码不能为空'));
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

                // 备份现有JAR（基础目录中的 yudao-server.jar）
                `if [ -f "${REMOTE_DIR}/${JAR_NAME}" ]; then cp "${REMOTE_DIR}/${JAR_NAME}" "${REMOTE_DIR}/backup/${JAR_NAME}.${timestamp}.bak"; echo "[Backup] 备份为 backup/${JAR_NAME}.${timestamp}.bak"; fi`
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

                        // 部署命令
                        const deployCommands = [
                            // 原子替换 target 下的 jar
                            `mv -f "${REMOTE_DIR}/target/${JAR_NAME}.new" "${REMOTE_DIR}/target/${JAR_NAME}"`,
                            // 同步一份到 build 目录（供 deploy.sh 使用）
                            `cp -f "${REMOTE_DIR}/target/${JAR_NAME}" "${REMOTE_DIR}/build/${JAR_NAME}"`,
                            // 在远程目录构建 Docker 镜像
                            `cd "${REMOTE_DIR}" && echo "[Docker] Building image..." && docker build -t yudao-server .`,
                            // 执行部署脚本（deploy.sh 负责停止/替换/启动等）
                            `cd "${REMOTE_DIR}" && chmod +x deploy.sh && echo "[Deploy] Running deploy.sh..." && IMAGE=yudao-server:latest bash deploy.sh`
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
        console.log(chalk.green('    Backend Deployment Successful!'));
        console.log(chalk.green('========================================'));
        console.log();
        console.log(`服务器: ${SERVER_HOST}`);
        console.log(`远程目录: ${REMOTE_DIR}`);
        console.log(`备份位置: ${REMOTE_DIR}/backup/`);
        console.log();
        console.log(`可通过以下命令查看服务状态:`);
        console.log(`  docker logs -f mindtrip-server`);
        console.log();
    })
    .catch((err) => {
        console.log();
        console.log(chalk.red('[Error] Deployment failed:'), err.message);
        process.exit(1);
    });

