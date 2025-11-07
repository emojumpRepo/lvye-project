#!/usr/bin/env node

// Mindtrip Frontend Rollback Script
// Features:
// - List archived versions from COS
// - Interactive version selection
// - Download and redeploy selected version

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import readline from 'readline-sync';
import chalk from 'chalk';
import { execSync } from 'child_process';
import dotenv from 'dotenv';
import COS from 'cos-nodejs-sdk-v5';
import { createWriteStream } from 'fs';

// 设置 Windows 控制台编码为 UTF-8
if (process.platform === 'win32') {
    try {
        execSync('chcp 65001', { stdio: 'ignore' });
    } catch (e) {
        // 忽略错误
    }
}

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 加载 .env 文件
const envPath = path.join(__dirname, '../../.env');
dotenv.config({ path: envPath });

console.log(chalk.cyan('========================================'));
console.log(chalk.cyan('   Mindtrip Frontend Rollback'));
console.log(chalk.cyan('========================================'));
console.log();

// COS 配置
const COS_CONFIG = {
    bucket: 'mindtrip-1305613707',
    region: 'ap-guangzhou',
    secretId: process.env.TENCENT_SECRET_ID || '',
    secretKey: process.env.TENCENT_SECRET_KEY || ''
};

// 检查环境变量
if (!COS_CONFIG.secretId || !COS_CONFIG.secretKey) {
    console.log(chalk.red('[ERROR] Missing Tencent Cloud credentials!'));
    console.log(chalk.yellow('Please configure in .env file'));
    process.exit(1);
}

// 初始化 COS SDK
const cosClient = new COS({
    SecretId: COS_CONFIG.secretId,
    SecretKey: COS_CONFIG.secretKey
});

// 项目配置
const PROJECTS = {
    '1': {
        name: 'Admin Management Backend',
        localDistPath: path.join(__dirname, '../../mindtrip-ui/lvye-project-frontend/apps/admin/dist'),
        cosArchivePath: '/archives/admin',
    },
    '2': {
        name: 'Web User Frontend',
        localDistPath: path.join(__dirname, '../../mindtrip-ui/lvye-project-frontend/apps/web/dist'),
        cosArchivePath: '/archives/web',
    }
};

// 列出归档版本
async function listArchives(project) {
    return new Promise((resolve, reject) => {
        const prefix = project.cosArchivePath.substring(1) + '/';

        cosClient.getBucket({
            Bucket: COS_CONFIG.bucket,
            Region: COS_CONFIG.region,
            Prefix: prefix,
            MaxKeys: 50
        }, (err, data) => {
            if (err) {
                reject(err);
            } else {
                const archives = (data.Contents || [])
                    .filter(item => item.Key.endsWith('.zip'))
                    .map(item => ({
                        key: item.Key,
                        fileName: path.basename(item.Key),
                        size: item.Size,
                        lastModified: new Date(item.LastModified),
                    }))
                    .sort((a, b) => b.lastModified - a.lastModified);

                resolve(archives);
            }
        });
    });
}

// 下载归档文件
async function downloadArchive(archiveKey, localPath) {
    return new Promise((resolve, reject) => {
        console.log(chalk.cyan(`[Download] Downloading archive...`));

        let downloadedSize = 0;

        cosClient.getObject({
            Bucket: COS_CONFIG.bucket,
            Region: COS_CONFIG.region,
            Key: archiveKey,
            onProgress: (progressData) => {
                downloadedSize = progressData.loaded;
                const percent = Math.round((progressData.loaded / progressData.total) * 100);
                process.stdout.write(`\r[Download] Progress: ${percent}% (${(downloadedSize / 1024 / 1024).toFixed(2)} MB)`);
            }
        }, (err, data) => {
            if (err) {
                console.log();
                reject(err);
            } else {
                console.log();
                console.log(chalk.green(`[Download] Download completed`));

                // data.Body 是 Buffer，直接写入文件
                fs.writeFileSync(localPath, data.Body);
                resolve();
            }
        });
    });
}

// 解压 ZIP 文件
async function extractZip(zipPath, destPath) {
    console.log(chalk.cyan(`[Extract] Extracting archive...`));

    // 清理目标目录
    if (fs.existsSync(destPath)) {
        fs.rmSync(destPath, { recursive: true, force: true });
    }
    fs.mkdirSync(destPath, { recursive: true });

    // 使用 unzipper 或系统命令解压
    try {
        // Windows: 使用 PowerShell 解压
        if (process.platform === 'win32') {
            execSync(`powershell -command "Expand-Archive -Path '${zipPath}' -DestinationPath '${destPath}' -Force"`, {
                stdio: 'ignore'
            });
        } else {
            // Linux/Mac: 使用 unzip
            execSync(`unzip -q -o "${zipPath}" -d "${destPath}"`);
        }

        console.log(chalk.green(`[Extract] Extraction completed`));
    } catch (error) {
        throw new Error(`Failed to extract ZIP: ${error.message}`);
    }
}

// 主函数
async function main() {
    try {
        // 选择项目
        console.log('Select project to rollback:');
        console.log('');
        console.log('  [1] Admin Management Backend');
        console.log('  [2] Web User Frontend');
        console.log('');

        const choice = readline.question('Your choice (1/2): ').trim();

        const project = PROJECTS[choice];
        if (!project) {
            console.log(chalk.red('[ERROR] Invalid choice!'));
            process.exit(1);
        }

        console.log();
        console.log(chalk.cyan(`[Archive] Loading archive versions for ${project.name}...`));

        // 列出归档版本
        const archives = await listArchives(project);

        if (archives.length === 0) {
            console.log(chalk.yellow('[Archive] No archived versions found'));
            process.exit(0);
        }

        // 显示版本列表
        console.log();
        console.log('Available versions:');
        console.log();

        archives.forEach((archive, index) => {
            const timeAgo = getTimeAgo(archive.lastModified);
            const size = (archive.size / 1024 / 1024).toFixed(2);
            const isCurrent = index === 0 ? chalk.green(' (current)') : '';
            console.log(`  [${index + 1}] ${archive.fileName}`);
            console.log(`      ${timeAgo} ago • ${size} MB${isCurrent}`);
            console.log();
        });

        // 选择版本
        const versionChoice = readline.question('Select version to rollback (number or 0 to cancel): ').trim();

        if (versionChoice === '0') {
            console.log(chalk.yellow('Rollback cancelled'));
            process.exit(0);
        }

        const versionIndex = parseInt(versionChoice) - 1;
        if (versionIndex < 0 || versionIndex >= archives.length) {
            console.log(chalk.red('[ERROR] Invalid version!'));
            process.exit(1);
        }

        const selectedArchive = archives[versionIndex];

        // 确认回滚
        console.log();
        console.log(chalk.yellow('========================================'));
        console.log(chalk.yellow(`WARNING: Rolling back to ${selectedArchive.fileName}`));
        console.log(chalk.yellow('This will replace the current deployment!'));
        console.log(chalk.yellow('========================================'));
        console.log();

        const confirm = readline.question('Are you sure? (yes/no): ').trim().toLowerCase();

        if (confirm !== 'yes') {
            console.log(chalk.yellow('Rollback cancelled'));
            process.exit(0);
        }

        // 执行回滚
        console.log();
        console.log(chalk.cyan('========================================'));
        console.log(chalk.cyan('  Starting Rollback Process'));
        console.log(chalk.cyan('========================================'));
        console.log();

        // 下载归档
        const tempZipPath = path.join(__dirname, '../../temp-rollback.zip');
        await downloadArchive(selectedArchive.key, tempZipPath);

        // 解压到 dist 目录
        await extractZip(tempZipPath, project.localDistPath);

        // 删除临时文件
        fs.unlinkSync(tempZipPath);

        // 提示重新部署
        console.log();
        console.log(chalk.green('========================================'));
        console.log(chalk.green('  Rollback Preparation Completed!'));
        console.log(chalk.green('========================================'));
        console.log();
        console.log(chalk.yellow('Next steps:'));
        console.log('  1. The selected version has been extracted to dist/');
        console.log('  2. Run deployment command to complete rollback:');
        console.log();
        console.log(chalk.cyan('     npm run deploy:frontend'));
        console.log();
        console.log(chalk.gray('Note: Select the same project during deployment'));

    } catch (error) {
        console.log();
        console.log(chalk.red('[ERROR] Rollback failed:', error.message));
        process.exit(1);
    }
}

// 计算时间差
function getTimeAgo(date) {
    const seconds = Math.floor((new Date() - date) / 1000);

    if (seconds < 60) return `${seconds} seconds`;
    if (seconds < 3600) return `${Math.floor(seconds / 60)} minutes`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)} hours`;
    return `${Math.floor(seconds / 86400)} days`;
}

main();
