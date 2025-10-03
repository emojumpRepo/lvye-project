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

// 项目配置
const PROJECTS = {
    '1': {
        name: 'Admin Management Backend',
        remotePath: '/root/mindfront/work/project/mindtrip_apps/admin',
        nginxPath: '/root/mindfront/work/nginx/html/admin',
        localZip: path.join(__dirname, '../../yudao-ui/lvye-project-frontend/apps/admin/dist.zip'),
        buildPath: 'yudao-ui/lvye-project-frontend/apps/admin',
        accessUrl: (host) => `http://${host}/admin/`
    },
    '2': {
        name: 'Web User Frontend',
        remotePath: '/root/mindfront/work/project/mindtrip_apps/web',
        nginxPath: '/root/mindfront/work/nginx/html/web',
        localZip: path.join(__dirname, '../../yudao-ui/lvye-project-frontend/apps/web/dist.zip'),
        buildPath: 'yudao-ui/lvye-project-frontend/apps/web',
        accessUrl: (host) => `http://${host}/`
    }
};

// 选择项目
console.log('Please select the frontend project to deploy:');
console.log('');
console.log('  [1] Admin Management Backend');
console.log('  [2] Web User Frontend');
console.log('  [3] All Projects (Admin + Web)');
console.log('');

const projectChoice = readline.question('Please enter your choice (1, 2, or 3): ');
const choice = projectChoice.trim();

let projectsToDeploy = [];

switch (choice) {
    case '1':
        projectsToDeploy = [PROJECTS['1']];
        break;
    case '2':
        projectsToDeploy = [PROJECTS['2']];
        break;
    case '3':
        projectsToDeploy = [PROJECTS['1'], PROJECTS['2']];
        break;
    default:
        console.log(chalk.red('[Error] Invalid choice!'));
        process.exit(1);
}

console.log();
console.log('========================================');
console.log(`Selected: ${choice === '3' ? 'All Projects' : projectsToDeploy[0].name}`);
console.log(`Server: ${SERVER_HOST}`);
if (choice === '3') {
    projectsToDeploy.forEach(project => {
        console.log(`  - ${project.name}`);
    });
}
console.log('========================================');
console.log();

// 检查所有项目的本地ZIP文件
let missingBuilds = [];
projectsToDeploy.forEach(project => {
    if (!fs.existsSync(project.localZip)) {
        missingBuilds.push(project);
    }
});

if (missingBuilds.length > 0) {
    console.log(chalk.red('[Error] dist.zip file(s) not found, please build the following frontend project(s) first:'));
    console.log();
    missingBuilds.forEach(project => {
        console.log(`For ${project.name}:`);
        console.log(`  cd ${project.buildPath}`);
        console.log('  pnpm install');
        console.log('  pnpm build');
        console.log();
    });
    console.log('Make sure dist.zip file is generated for each project');
    process.exit(1);
}

console.log('[Info] All required dist.zip files found, preparing to deploy...');

// 询问服务器密码（提供默认值）
console.log('[Info] Using default password, press Enter to continue or input custom password:');
const password = readline.question('Server password (default: MF@Luye!996): ', { hideEchoBack: true }) || 'MF@Luye!996';
if (!password) {
    console.log(chalk.red('[Error] Password cannot be empty'));
    process.exit(1);
}

// 部署单个项目的函数
function deployProject(conn, project, sftp) {
    return new Promise((resolve, reject) => {
        console.log();
        console.log(chalk.cyan(`[Deploying] ${project.name}...`));
        console.log(`[Path] Project: ${project.remotePath}`);
        console.log(`[Path] Nginx: ${project.nginxPath}`);

        // 获取时间戳
        const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace('T', '_').split('.')[0];
        const tempDir = `/tmp/frontend_deploy_${timestamp}_${Math.random().toString(36).substring(7)}`;

        // 执行部署命令序列
        const commands = [
            // 创建必要的目录
            `mkdir -p "${project.remotePath}"`,
            `mkdir -p "${project.remotePath}_backup"`,
            `mkdir -p "${project.nginxPath}_backup"`,
            `mkdir -p "${tempDir}"`,
            `mkdir -p /root/mindfront/work/nginx/html`,

            // Backup existing dist.zip if it exists
            `if [ -f "${project.remotePath}/dist.zip" ]; then cp "${project.remotePath}/dist.zip" "${project.remotePath}_backup/dist_${timestamp}.zip"; echo "[Backup] Created project backup: ${project.remotePath}_backup/dist_${timestamp}.zip"; fi`,
            
            // Backup existing nginx directory if it exists
            `if [ -d "${project.nginxPath}" ]; then cp -r "${project.nginxPath}" "${project.nginxPath}_backup/backup_${timestamp}"; echo "[Backup] Created nginx backup: ${project.nginxPath}_backup/backup_${timestamp}"; fi`,
            
            // Clean old project backups (keep only last 4)
            `cd "${project.remotePath}_backup" 2>/dev/null && ls -t dist_*.zip 2>/dev/null | tail -n +5 | xargs -r rm -f && echo "[Cleanup] Old project backups cleaned, keeping last 4"`,
            
            // Clean old nginx backups (keep only last 4)
            `cd "${project.nginxPath}_backup" 2>/dev/null && ls -dt backup_* 2>/dev/null | tail -n +5 | xargs -r rm -rf && echo "[Cleanup] Old nginx backups cleaned, keeping last 4"`,

            // Clean target directory
            `if [ -d "${project.remotePath}" ]; then rm -rf "${project.remotePath}"/*; echo "[Clean] Project directory cleaned"; fi`,
            
            // Clean nginx directory
            `if [ -d "${project.nginxPath}" ]; then rm -rf "${project.nginxPath}"/*; echo "[Clean] Nginx directory cleaned"; fi`
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
            console.log(`[Upload] Starting to upload ${project.name} ZIP file...`);
            const fileSize = fs.statSync(project.localZip).size;
            let uploaded = 0;

            const readStream = fs.createReadStream(project.localZip);
            const writeStream = sftp.createWriteStream(`${tempDir}/dist.zip`);

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
                    `cd ${tempDir} && unzip -o dist.zip`,
                    // Check extraction result and copy to target directory
                    `if [ -d "${tempDir}/dist" ]; then ` +
                    `cp -r ${tempDir}/dist/* "${project.remotePath}/"; ` +
                    `echo "[Copy] Copied dist directory contents to ${project.remotePath}"; ` +
                    `elif [ -f "${tempDir}/index.html" ]; then ` +
                    `cp -r ${tempDir}/* "${project.remotePath}/"; ` +
                    `echo "[Copy] Copied extracted contents to ${project.remotePath}"; ` +
                    `else ` +
                    `echo "[Error] Expected file structure not found after extraction"; ` +
                    `ls -la ${tempDir}/; ` +
                    `fi`,
                    // Save original zip file
                    `cp ${tempDir}/dist.zip "${project.remotePath}/dist.zip" && echo "[Save] Saved dist.zip to project directory"`,
                    
                    // Copy to nginx directory
                    `echo "[Nginx] Starting to copy files to nginx directory..."`,
                    `if [ -f "${project.remotePath}/index.html" ]; then ` +
                    `cp -r "${project.remotePath}"/* "${project.nginxPath}/"; ` +
                    `echo "[Nginx] Successfully copied files to ${project.nginxPath}"; ` +
                    `else ` +
                    `echo "[Error] No index.html found in project directory, nginx copy skipped"; ` +
                    `fi`,
                    
                    // Clean temporary files
                    `rm -rf ${tempDir}/*`,
                    
                    // Set permissions
                    `chmod -R 755 "${project.remotePath}"`,
                    `chown -R www:www "${project.remotePath}"`,
                    `chmod -R 755 "${project.nginxPath}"`,
                    `chown -R www:www "${project.nginxPath}"`,
                    
                    // Verify deployment
                    `if [ -f "${project.remotePath}/index.html" ] && [ -f "${project.nginxPath}/index.html" ]; then ` +
                    `echo "[Success] ${project.name} deployment completed successfully"; ` +
                    `echo "[Files] Project directory contains:"; ` +
                    `ls -la "${project.remotePath}/" | head -5; ` +
                    `echo "[Files] Nginx directory contains:"; ` +
                    `ls -la "${project.nginxPath}/" | head -5; ` +
                    `else ` +
                    `echo "[Warning] Deployment may be incomplete"; ` +
                    `echo "[Check] Project directory:"; ` +
                    `ls -la "${project.remotePath}/"; ` +
                    `echo "[Check] Nginx directory:"; ` +
                    `ls -la "${project.nginxPath}/"; ` +
                    `fi`
                ];

                let deployIndex = 0;

                function executeDeployCommand() {
                    if (deployIndex >= deployCommands.length) {
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
        }

        executeNextCommand();
    });
}

// 创建SSH连接并部署所有项目
function deployWithSSH() {
    return new Promise((resolve, reject) => {
        const conn = new Client();
        
        conn.on('ready', () => {
            console.log('[Connect] SSH connection successful');
            
            // 获取SFTP连接
            conn.sftp(async (err, sftp) => {
                if (err) {
                    reject(err);
                    return;
                }
                
                try {
                    // 逐个部署项目
                    for (const project of projectsToDeploy) {
                        await deployProject(conn, project, sftp);
                    }
                    
                    conn.end();
                    resolve();
                } catch (error) {
                    conn.end();
                    reject(error);
                }
            });
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
        console.log(`Server: ${SERVER_HOST}`);
        console.log();
        console.log('Deployed Projects:');
        projectsToDeploy.forEach(project => {
            console.log(`  - ${project.name}`);
            console.log(`    Deploy Path: ${project.remotePath}`);
            console.log(`    Nginx Path: ${project.nginxPath}`);
            console.log(`    Access URL: ${project.accessUrl(SERVER_HOST)}`);
            console.log();
        });
        console.log('Backup Locations:');
        projectsToDeploy.forEach(project => {
            console.log(`  - ${project.name}:`);
            console.log(`    Project: ${project.remotePath}_backup/`);
            console.log(`    Nginx: ${project.nginxPath}_backup/`);
        });
        console.log();
    })
    .catch((err) => {
        console.log();
        console.log(chalk.red('[Error] Deployment failed:'), err.message);
        process.exit(1);
    });