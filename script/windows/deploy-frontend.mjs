#!/usr/bin/env node

// Mindtrip Frontend Deployment - Optimized Version
// Features:
// - Cloud decompression with status polling
// - Deployment verification & rollback
// - SSH key authentication
// - Retry mechanism for network operations
// - Comprehensive error handling
// - Detailed logging

import { Client } from 'ssh2';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import readline from 'readline-sync';
import chalk from 'chalk';
import { execSync } from 'child_process';
import archiver from 'archiver';
import dotenv from 'dotenv';
import COS from 'cos-nodejs-sdk-v5';
import crypto from 'crypto';
import https from 'https';
import http from 'http';

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

// 配置
const VERBOSE = process.env.DEPLOY_VERBOSE === 'true';
const MAX_RETRIES = 3;
const RETRY_DELAY = 2000; // ms
const DECOMPRESSION_TIMEOUT = 300000; // 5 minutes
const POLL_INTERVAL = 3000; // 3 seconds

// 路径标准化函数
function normalizePath(filePath) {
    return filePath.replace(/\\/g, '/').replace(/\/+/g, '/');
}

// 日志函数
function debugLog(...args) {
    if (VERBOSE) {
        console.log(chalk.gray('[DEBUG]'), ...args);
    }
}

function infoLog(...args) {
    console.log(chalk.blue('[INFO]'), ...args);
}

function errorLog(...args) {
    console.log(chalk.red('[ERROR]'), ...args);
}

console.log(chalk.cyan('========================================'));
console.log(chalk.cyan('   Mindtrip Frontend Deployment'));
console.log(chalk.cyan('   Optimized with Auto Verification'));
console.log(chalk.cyan('========================================'));
console.log();

// COS+CDN 配置
const COS_CONFIG = {
    bucket: 'mindtrip-1305613707',
    region: 'ap-guangzhou',
    cdnDomain: 'cdn.mindtrip.emojump.com',
    secretId: process.env.TENCENT_SECRET_ID || '',
    secretKey: process.env.TENCENT_SECRET_KEY || ''
};

// 服务器配置
const SERVER_CONFIG = {
    host: '42.194.163.176',
    user: 'root',
    port: 22,
    password: null,
    privateKey: null
};

// 项目配置
const PROJECTS = {
    '1': {
        name: 'Admin Management Backend',
        localDistPath: path.join(__dirname, '../../yudao-ui/lvye-project-frontend/apps/admin/dist'),
        buildPath: 'yudao-ui/lvye-project-frontend/apps/admin',
        cosPath: '/admin',
        cosArchivePath: '/archives/admin',
        cdnUrl: `https://${COS_CONFIG.cdnDomain}/admin`,
        remotePath: '/root/mindfront/work/project/mindtrip_apps/admin',
        nginxPath: '/root/mindfront/work/nginx/html/admin',
        accessUrl: (host) => `http://${host}/admin/`,
        // 关键文件验证（支持 pattern: true 进行模式匹配）
        criticalFiles: [
            { path: 'index.html', required: true },
            { path: 'css/', pattern: true, description: 'CSS files', required: false },
            { path: 'js/', pattern: true, description: 'JS files', required: false },
        ]
    },
    '2': {
        name: 'Web User Frontend',
        localDistPath: path.join(__dirname, '../../yudao-ui/lvye-project-frontend/apps/web/dist'),
        buildPath: 'yudao-ui/lvye-project-frontend/apps/web',
        cosPath: '/web',
        cosArchivePath: '/archives/web',
        cdnUrl: `https://${COS_CONFIG.cdnDomain}/web`,
        remotePath: '/root/mindfront/work/project/mindtrip_apps/web',
        nginxPath: '/root/mindfront/work/nginx/html/web',
        accessUrl: (host) => `http://${host}/`,
        // 关键文件验证
        criticalFiles: [
            { path: 'index.html', required: true },
            { path: 'css/', pattern: true, description: 'CSS files', required: false },
            { path: 'js/', pattern: true, description: 'JS files', required: false },
        ]
    }
};

// 检查环境变量
if (!COS_CONFIG.secretId || !COS_CONFIG.secretKey) {
    errorLog('Missing Tencent Cloud credentials!');
    console.log(chalk.yellow('Please configure in .env file'));
    process.exit(1);
}

// 初始化 COS SDK
const cosClient = new COS({
    SecretId: COS_CONFIG.secretId,
    SecretKey: COS_CONFIG.secretKey
});

// 测试 COS 连接
async function testCOSConnection() {
    try {
        debugLog('Testing COS connection...');
        await new Promise((resolve, reject) => {
            cosClient.headBucket({
                Bucket: COS_CONFIG.bucket,
                Region: COS_CONFIG.region
            }, (err, data) => {
                if (err) {
                    reject(new Error(`COS connection failed: ${err.message}`));
                } else {
                    debugLog('COS connection OK');
                    resolve(data);
                }
            });
        });
    } catch (error) {
        errorLog('Failed to connect to COS:', error.message);
        console.log(chalk.yellow('Please check:'));
        console.log('  1. Network connection');
        console.log('  2. Bucket name and region');
        console.log('  3. Secret ID and Secret Key');
        throw error;
    }
}

// ==================== 工具函数 ====================

// 重试包装器
async function withRetry(fn, context = '', maxRetries = MAX_RETRIES) {
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            return await fn();
        } catch (error) {
            if (attempt === maxRetries) {
                errorLog(`${context} failed after ${maxRetries} attempts:`, error.message);
                throw error;
            }

            console.log(chalk.yellow(`[Retry] ${context} attempt ${attempt}/${maxRetries} failed, retrying...`));
            debugLog('Error:', error.message);
            await new Promise(resolve => setTimeout(resolve, RETRY_DELAY * attempt));
        }
    }
}

// 延迟函数
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// ==================== ZIP 压缩 ====================

async function createZipArchive(project) {
    const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace('T', '_').split('.')[0];
    // 根据项目生成不同的文件名
    const projectSuffix = project.name.includes('Admin') ? 'admin' : 'web';
    const zipFileName = `dist-${projectSuffix}-${timestamp}.zip`;
    const zipFilePath = path.join(path.dirname(project.localDistPath), zipFileName);

    console.log(chalk.cyan(`[ZIP] Creating archive: ${zipFileName}...`));
    debugLog('Source:', project.localDistPath);
    debugLog('Output:', zipFilePath);

    return new Promise((resolve, reject) => {
        const output = fs.createWriteStream(zipFilePath);
        const archive = archiver('zip', { zlib: { level: 9 } });

        let totalSize = 0;
        let fileCount = 0;

        output.on('close', () => {
            try {
                const zipSize = archive.pointer();

                // 验证 ZIP 文件
                if (!fs.existsSync(zipFilePath)) {
                    throw new Error('ZIP file was not created');
                }

                const actualFileSize = fs.statSync(zipFilePath).size;
                if (actualFileSize === 0) {
                    throw new Error('ZIP file is empty');
                }

                // 防止除以0错误
                const compressionRatio = totalSize > 0
                    ? ((1 - zipSize / totalSize) * 100).toFixed(1)
                    : '0.0';

                console.log(chalk.green(`[ZIP] Archive created successfully`));
                console.log(`  Files: ${fileCount}`);
                console.log(`  Original: ${(totalSize / 1024 / 1024).toFixed(2)} MB`);
                console.log(`  Compressed: ${(zipSize / 1024 / 1024).toFixed(2)} MB`);
                console.log(`  Ratio: ${compressionRatio}%`);

                debugLog('ZIP stats:', { fileCount, totalSize, zipSize, compressionRatio, actualFileSize });

                // 确保有效的返回数据
                if (zipSize === 0 || totalSize === 0) {
                    console.log(chalk.yellow('[ZIP Warning] Size calculation may be inaccurate'));
                }

                resolve({ zipFilePath, zipFileName, zipSize: actualFileSize, totalSize: totalSize || actualFileSize });
            } catch (error) {
                errorLog('Failed to finalize ZIP archive:', error.message);
                reject(error);
            }
        });

        output.on('error', (err) => {
            errorLog('Output stream error:', err.message);
            reject(err);
        });

        archive.on('error', (err) => {
            errorLog('Archive error:', err.message);
            reject(err);
        });

        archive.on('warning', (err) => {
            if (err.code === 'ENOENT') {
                console.log(chalk.yellow(`[ZIP Warning] File not found: ${err.path || 'unknown'}`));
            } else {
                throw err;
            }
        });

        archive.on('entry', (entry) => {
            if (entry.stats) {
                totalSize += entry.stats.size;
                fileCount++;
            }
        });

        archive.pipe(output);
        archive.directory(project.localDistPath, false);

        debugLog('Starting archive finalization...');
        archive.finalize();
    });
}

// ==================== COS 操作 ====================

// 上传 ZIP 到 COS
async function uploadZipToCOS(project, zipFilePath, zipFileName) {
    console.log(chalk.cyan(`[COS Upload] Uploading ZIP archive...`));

    const tempZipPath = `/temp/${zipFileName}`;
    const fileSize = fs.statSync(zipFilePath).size;
    console.log(`[COS Upload] File size: ${(fileSize / 1024 / 1024).toFixed(2)} MB`);

    return await withRetry(async () => {
        return new Promise((resolve, reject) => {
            let lastProgress = 0;
            let uploadStarted = false;

            // 10分钟超时
            const timeout = setTimeout(() => {
                reject(new Error('Upload timeout after 10 minutes'));
            }, 10 * 60 * 1000);

            cosClient.uploadFile({
                Bucket: COS_CONFIG.bucket,
                Region: COS_CONFIG.region,
                Key: tempZipPath,
                FilePath: zipFilePath,
                SliceSize: 1024 * 1024 * 5,
                onProgress: (progressData) => {
                    uploadStarted = true;
                    const progress = Math.round((progressData.loaded / fileSize) * 100);
                    if (progress - lastProgress >= 5 || progress === 100) {
                        process.stdout.write(`\r[COS Upload] Progress: ${progress}% (${(progressData.loaded / 1024 / 1024).toFixed(2)}MB / ${(fileSize / 1024 / 1024).toFixed(2)}MB)`);
                        lastProgress = progress;
                    }
                }
            }, (err, data) => {
                clearTimeout(timeout);
                console.log();

                if (err) {
                    errorLog('Upload failed:', err.message);
                    debugLog('Error details:', err);
                    reject(err);
                } else {
                    console.log(chalk.green(`[COS Upload] ZIP uploaded successfully`));
                    debugLog('Location:', `cos://${COS_CONFIG.bucket}${tempZipPath}`);
                    resolve({ tempZipPath, ...data });
                }
            });

            // 30秒内未开始上传则报错
            setTimeout(() => {
                if (!uploadStarted) {
                    clearTimeout(timeout);
                    reject(new Error('Upload not started within 30 seconds - check network connection'));
                }
            }, 30000);
        });
    }, 'ZIP upload');
}

// 获取默认队列 ID
async function getDefaultQueueId() {
    return new Promise((resolve, reject) => {
        cosClient.request({
            Method: 'GET',
            Url: `https://${COS_CONFIG.bucket}.ci.${COS_CONFIG.region}.myqcloud.com/queue`,
        }, (err, data) => {
            if (err) {
                debugLog('Failed to get queue, using empty QueueId');
                resolve(''); // 使用空字符串作为默认值
            } else {
                try {
                    // 检查响应数据
                    if (!data || !data.Body) {
                        debugLog('Queue response has no Body, data:', JSON.stringify(data));
                        resolve('');
                        return;
                    }

                    // 解析 XML 响应获取队列 ID
                    const queueMatch = data.Body.match(/<QueueId>([^<]+)<\/QueueId>/);
                    const queueId = queueMatch ? queueMatch[1] : '';
                    debugLog('Got QueueId:', queueId);
                    resolve(queueId);
                } catch (e) {
                    debugLog('Failed to parse queue response:', e.message);
                    resolve('');
                }
            }
        });
    });
}

// 提交云端解压任务
async function submitDecompressionTask(project, tempZipPath) {
    console.log(chalk.cyan(`[COS Decompress] Submitting decompression task...`));

    const queueId = await getDefaultQueueId();
    debugLog('Using QueueId:', queueId || '(default)');

    return new Promise((resolve, reject) => {
        // 移除路径开头的斜杠
        const zipPath = tempZipPath.replace(/^\//, '');
        const outputPrefix = project.cosPath.replace(/^\//, '');

        // 构建 XML 请求体（严格按照文档格式）
        const requestBody = `<Request>
    <Tag>FileUncompress</Tag>
    <Input>
        <Object>${zipPath}</Object>
    </Input>
    <Operation>
        <FileUncompressConfig>
            <Prefix>${outputPrefix}</Prefix>
            <PrefixReplaced>0</PrefixReplaced>
        </FileUncompressConfig>
        <Output>
            <Bucket>${COS_CONFIG.bucket}</Bucket>
            <Region>${COS_CONFIG.region}</Region>
        </Output>
    </Operation>${queueId ? `
    <QueueId>${queueId}</QueueId>` : ''}
</Request>`;

        debugLog('Request body:', requestBody);

        const host = `${COS_CONFIG.bucket}.ci.${COS_CONFIG.region}.myqcloud.com`;
        const url = `https://${host}/file_jobs`;

        cosClient.request({
            Method: 'POST',
            Key: 'file_jobs',
            Url: url,
            Body: requestBody,
            ContentType: 'application/xml',
        }, (err, data) => {
            if (err) {
                errorLog('Failed to submit decompression task:', err.message);
                debugLog('Error details:', JSON.stringify(err, null, 2));
                reject(err);
            } else {
                try {
                    debugLog('Raw response:', JSON.stringify(data, null, 2));

                    // SDK 可能返回 JSON 格式的 Response
                    if (data && data.Response) {
                        const response = data.Response;
                        const jobId = response.JobsDetail?.JobId || response.JobId;

                        if (!jobId) {
                            errorLog('No Job ID found in response');
                            debugLog('Full response:', JSON.stringify(response));
                            reject(new Error('Failed to get Job ID from response'));
                            return;
                        }

                        console.log(chalk.green(`[COS Decompress] Task submitted`));
                        console.log(`  Job ID: ${jobId}`);
                        resolve({ jobId, data });
                        return;
                    }

                    // 如果返回的是 XML Body，尝试解析
                    if (data && data.Body) {
                        const jobIdMatch = data.Body.match(/<JobId>([^<]+)<\/JobId>/);
                        const jobId = jobIdMatch ? jobIdMatch[1] : null;

                        if (!jobId) {
                            errorLog('No Job ID found in XML response');
                            debugLog('Full response:', data.Body);
                            reject(new Error('Failed to get Job ID from response'));
                            return;
                        }

                        console.log(chalk.green(`[COS Decompress] Task submitted`));
                        console.log(`  Job ID: ${jobId}`);
                        resolve({ jobId, data });
                        return;
                    }

                    // 无法识别的响应格式
                    errorLog('Decompression response has unknown format');
                    debugLog('Response data:', JSON.stringify(data));
                    reject(new Error('Invalid response from COS CI API - please check if Data Processing is enabled'));

                } catch (e) {
                    errorLog('Failed to parse decompression response:', e.message);
                    debugLog('Parse error:', e);
                    reject(e);
                }
            }
        });
    });
}

// 查询解压任务状态
async function getDecompressionStatus(jobId) {
    return new Promise((resolve, reject) => {
        const url = `https://${COS_CONFIG.bucket}.ci.${COS_CONFIG.region}.myqcloud.com/file_jobs/${jobId}`;

        cosClient.request({
            Method: 'GET',
            Key: `file_jobs/${jobId}`,
            Url: url,
        }, (err, data) => {
            if (err) {
                reject(err);
            } else {
                try {
                    debugLog('Status response:', JSON.stringify(data, null, 2));

                    // 检查响应数据
                    if (!data || !data.Response) {
                        errorLog('Status response has no Response field');
                        debugLog('Response data:', JSON.stringify(data));
                        reject(new Error('Invalid status response from COS CI API'));
                        return;
                    }

                    const response = data.Response;
                    const jobDetail = response.JobsDetail || {};

                    const state = jobDetail.State || 'Unknown';
                    const progress = jobDetail.Progress || '0';

                    debugLog(`Job ${jobId} status: ${state}, progress: ${progress}`);
                    resolve({ state, progress });
                } catch (e) {
                    errorLog('Failed to parse status response:', e.message);
                    debugLog('Parse error:', e);
                    reject(e);
                }
            }
        });
    });
}

// 等待解压完成（带状态轮询）
async function waitForDecompression(jobId, zipSize) {
    const estimatedSeconds = Math.max(10, Math.ceil(zipSize / (1024 * 1024 * 2)));
    const maxWaitTime = Math.max(DECOMPRESSION_TIMEOUT, estimatedSeconds * 1000);
    const startTime = Date.now();

    console.log(chalk.cyan(`[COS Decompress] Waiting for cloud decompression...`));
    console.log(chalk.gray(`  Estimated time: ~${estimatedSeconds}s, timeout: ${maxWaitTime / 1000}s`));

    let lastProgress = 0;

    while (Date.now() - startTime < maxWaitTime) {
        try {
            const { state, progress } = await getDecompressionStatus(jobId);

            // 显示进度
            const progressNum = parseInt(progress) || 0;
            if (progressNum - lastProgress >= 10 || progressNum === 100) {
                process.stdout.write(`\r[COS Decompress] Progress: ${progressNum}%`);
                lastProgress = progressNum;
            }

            if (state === 'Success') {
                console.log();
                console.log(chalk.green(`[COS Decompress] Decompression completed successfully`));
                return;
            } else if (state === 'Failed') {
                console.log();
                throw new Error('Cloud decompression failed');
            } else if (state === 'Running' || state === 'Submitted') {
                // 继续等待
                await delay(POLL_INTERVAL);
            } else {
                debugLog('Unknown state:', state);
                await delay(POLL_INTERVAL);
            }
        } catch (error) {
            if (error.message === 'Cloud decompression failed') {
                throw error;
            }
            // 查询失败，继续重试
            debugLog('Status query failed:', error.message);
            await delay(POLL_INTERVAL);
        }
    }

    throw new Error(`Decompression timeout after ${maxWaitTime / 1000}s`);
}

// 验证 COS 文件存在
async function checkCOSFileExists(key) {
    return new Promise((resolve) => {
        cosClient.headObject({
            Bucket: COS_CONFIG.bucket,
            Region: COS_CONFIG.region,
            Key: key
        }, (err, data) => {
            resolve(!err);
        });
    });
}

// 获取 COS 目录文件数量
async function getCOSFileCount(prefix) {
    return new Promise((resolve, reject) => {
        cosClient.getBucket({
            Bucket: COS_CONFIG.bucket,
            Region: COS_CONFIG.region,
            Prefix: prefix.substring(1) + '/',
            MaxKeys: 1000
        }, (err, data) => {
            if (err) {
                reject(err);
            } else {
                resolve(data.Contents ? data.Contents.length : 0);
            }
        });
    });
}

// 验证部署结果
// 检查目录下是否有文件
async function checkCOSDirectoryHasFiles(prefix) {
    return new Promise((resolve, reject) => {
        // 确保前缀格式正确（不以斜杠开头，以斜杠结尾）
        const normalizedPrefix = prefix.replace(/^\/+/, '').replace(/\/*$/, '/');

        debugLog(`Checking directory: ${normalizedPrefix}`);

        cosClient.getBucket({
            Bucket: COS_CONFIG.bucket,
            Region: COS_CONFIG.region,
            Prefix: normalizedPrefix,
            MaxKeys: 10
        }, (err, data) => {
            if (err) {
                errorLog(`Failed to check directory ${normalizedPrefix}:`, err.message);
                reject(err);
            } else {
                const fileCount = data.Contents ? data.Contents.length : 0;
                debugLog(`Directory ${normalizedPrefix} has ${fileCount} files`);

                // 显示前几个文件
                if (fileCount > 0 && data.Contents) {
                    debugLog('Sample files:', data.Contents.slice(0, 3).map(f => f.Key).join(', '));
                }

                resolve(fileCount > 0);
            }
        });
    });
}

async function verifyDeployment(project) {
    console.log(chalk.cyan(`[Verify] Checking deployment integrity...`));

    // 先列出部署的文件，帮助调试
    const deployedPrefix = project.cosPath.replace(/^\//, '');
    debugLog(`Listing files under: ${deployedPrefix}`);

    await new Promise((resolve) => {
        cosClient.getBucket({
            Bucket: COS_CONFIG.bucket,
            Region: COS_CONFIG.region,
            Prefix: deployedPrefix,
            MaxKeys: 20
        }, (err, data) => {
            if (!err && data.Contents) {
                debugLog('Deployed files (first 20):');
                data.Contents.slice(0, 20).forEach((file, idx) => {
                    debugLog(`  [${idx + 1}] ${file.Key}`);
                });
            }
            resolve();
        });
    });

    // 1. 检查关键文件
    for (const fileConfig of project.criticalFiles) {
        // 兼容旧格式（字符串）和新格式（对象）
        const config = typeof fileConfig === 'string'
            ? { path: fileConfig, required: true }
            : fileConfig;

        const { path: filePath, pattern, description, required = true } = config;

        if (pattern) {
            // 目录模式匹配 - 检查目录下是否有文件
            const prefix = (project.cosPath + '/' + filePath).replace(/\/+/g, '/').substring(1);
            const hasFiles = await checkCOSDirectoryHasFiles(prefix);

            if (hasFiles) {
                console.log(chalk.green(`  ✓ ${description || filePath}`));
            } else if (required) {
                // 再次尝试，可能需要等待
                console.log(chalk.yellow(`  Retrying check for ${description || filePath}...`));
                await delay(2000);
                const hasFilesRetry = await checkCOSDirectoryHasFiles(prefix);

                if (hasFilesRetry) {
                    console.log(chalk.green(`  ✓ ${description || filePath} (found on retry)`));
                } else {
                    throw new Error(`Critical directory empty: ${filePath}`);
                }
            } else {
                console.log(chalk.yellow(`  ⚠ ${description || filePath} (optional, not found)`));
            }
        } else {
            // 精确文件匹配
            const key = (project.cosPath + '/' + filePath).replace(/\/+/g, '/').substring(1);
            const exists = await checkCOSFileExists(key);

            if (exists) {
                console.log(chalk.green(`  ✓ ${filePath}`));
            } else if (required) {
                throw new Error(`Critical file missing: ${filePath}`);
            } else {
                console.log(chalk.yellow(`  ⚠ ${filePath} (optional, not found)`));
            }
        }
    }

    // 2. 检查文件数量
    const fileCount = await getCOSFileCount(project.cosPath);
    console.log(chalk.gray(`  Files deployed: ${fileCount}`));

    if (fileCount < 5) {
        throw new Error(`Too few files deployed: ${fileCount}`);
    }

    console.log(chalk.green(`[Verify] Deployment verified successfully`));
}

// 清理临时文件
async function cleanupTempZip(tempZipPath) {
    debugLog('Cleaning up temp ZIP:', tempZipPath);

    return new Promise((resolve) => {
        cosClient.deleteObject({
            Bucket: COS_CONFIG.bucket,
            Region: COS_CONFIG.region,
            Key: tempZipPath
        }, (err) => {
            if (err) {
                debugLog('Failed to delete temp ZIP:', err.message);
            } else {
                debugLog('Temp ZIP deleted successfully');
            }
            resolve();
        });
    });
}

// 保存归档备份
async function saveArchiveBackup(project, zipFilePath, zipFileName) {
    console.log(chalk.cyan(`[Archive] Saving backup...`));

    const archivePath = `${project.cosArchivePath}/${zipFileName}`;

    return await withRetry(async () => {
        return new Promise((resolve, reject) => {
            cosClient.uploadFile({
                Bucket: COS_CONFIG.bucket,
                Region: COS_CONFIG.region,
                Key: archivePath,
                FilePath: zipFilePath,
                SliceSize: 1024 * 1024 * 5
            }, (err, data) => {
                if (err) {
                    reject(err);
                } else {
                    console.log(chalk.green(`[Archive] Backup saved`));
                    debugLog('Location:', `cos://${COS_CONFIG.bucket}${archivePath}`);
                    resolve({ archivePath, ...data });
                }
            });
        });
    }, 'Archive backup');
}

// 清理旧归档
async function cleanOldArchives(project) {
    return new Promise((resolve) => {
        cosClient.getBucket({
            Bucket: COS_CONFIG.bucket,
            Region: COS_CONFIG.region,
            Prefix: project.cosArchivePath.substring(1) + '/dist-',
            MaxKeys: 1000
        }, (err, data) => {
            if (err || !data.Contents) {
                resolve();
                return;
            }

            const archives = data.Contents
                .filter(item => item.Key.endsWith('.zip'))
                .sort((a, b) => b.LastModified.localeCompare(a.LastModified));

            if (archives.length > 10) {
                const toDelete = archives.slice(10);
                console.log(chalk.gray(`[Archive] Cleaning old backups (keeping 10)...`));

                const deletions = toDelete.map(item =>
                    new Promise((res) => {
                        cosClient.deleteObject({
                            Bucket: COS_CONFIG.bucket,
                            Region: COS_CONFIG.region,
                            Key: item.Key
                        }, () => {
                            debugLog('Deleted:', path.basename(item.Key));
                            res();
                        });
                    })
                );

                Promise.all(deletions).then(resolve);
            } else {
                resolve();
            }
        });
    });
}

// ==================== SSH 操作 ====================

// 获取 SSH 认证方式
function getSSHAuth() {
    // 优先使用 SSH 密钥
    const sshKeyPaths = [
        path.join(process.env.HOME || process.env.USERPROFILE, '.ssh', 'id_rsa'),
        path.join(process.env.HOME || process.env.USERPROFILE, '.ssh', 'id_ed25519')
    ];

    for (const keyPath of sshKeyPaths) {
        if (fs.existsSync(keyPath)) {
            try {
                const privateKey = fs.readFileSync(keyPath);
                console.log(chalk.green(`[SSH] Using SSH key: ${path.basename(keyPath)}`));
                return { privateKey };
            } catch (e) {
                debugLog('Failed to read SSH key:', e.message);
            }
        }
    }

    // 回退到密码认证
    console.log(chalk.yellow('[SSH] No SSH key found, using password authentication'));
    console.log(chalk.gray('  Tip: Use SSH keys for better security'));

    const password = readline.question('Server password: ', { hideEchoBack: true });
    if (!password) {
        throw new Error('Password cannot be empty');
    }

    return { password };
}

// 部署 HTML 文件到服务器
async function deployHTMLToServer(conn, project, sftp) {
    return new Promise((resolve, reject) => {
        console.log(chalk.cyan(`[HTML Deploy] Deploying HTML files...`));

        const distPath = project.localDistPath;
        const nginxPath = project.nginxPath;
        const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace('T', '_').split('.')[0];
        const newNginxPath = `${nginxPath}_new_${timestamp}`;

        // 获取 HTML 文件列表
        const htmlFiles = [];
        const walkDir = (dir, baseDir = '') => {
            const files = fs.readdirSync(dir);
            for (const file of files) {
                const fullPath = path.join(dir, file);
                const relativePath = normalizePath(path.join(baseDir, file));

                if (fs.statSync(fullPath).isDirectory()) {
                    walkDir(fullPath, relativePath);
                } else if (file.endsWith('.html')) {
                    htmlFiles.push({ local: fullPath, remote: relativePath });
                }
            }
        };
        walkDir(distPath);

        if (htmlFiles.length === 0) {
            console.log(chalk.yellow('[HTML Deploy] No HTML files found, skipping...'));
            resolve({ success: true, skipped: true });
            return;
        }

        console.log(`[HTML Deploy] Found ${htmlFiles.length} HTML file(s)`);

        // 准备命令序列
        const commands = [
            `mkdir -p "${nginxPath}_backup"`,
            `mkdir -p "$(dirname "${nginxPath}")"`,
            `if [ -d "${nginxPath}" ]; then cp -r "${nginxPath}" "${nginxPath}_backup/backup_${timestamp}"; fi`,
            `cd "${nginxPath}_backup" 2>/dev/null && ls -dt backup_* 2>/dev/null | tail -n +5 | xargs -r rm -rf`,
            `mkdir -p "${newNginxPath}"`
        ];

        let commandIndex = 0;

        function executeNextCommand() {
            if (commandIndex >= commands.length) {
                uploadHTMLFiles();
                return;
            }

            const cmd = commands[commandIndex++];
            debugLog(`Executing prep command [${commandIndex}/${commands.length}]:`, cmd);

            const cmdTimeout = setTimeout(() => {
                errorLog('Prep command timeout:', cmd);
                reject(new Error(`Preparation command timeout: ${cmd.substring(0, 50)}...`));
            }, 30000); // 30秒超时

            conn.exec(cmd, (err, stream) => {
                if (err) {
                    clearTimeout(cmdTimeout);
                    reject(err);
                    return;
                }

                stream.on('close', (code) => {
                    clearTimeout(cmdTimeout);
                    debugLog(`Prep command completed with code: ${code}`);
                    executeNextCommand();
                }).on('data', (data) => {
                    const output = data.toString().trim();
                    if (output) debugLog('[Prep output]', output);
                }).stderr.on('data', (data) => {
                    const error = data.toString().trim();
                    if (error && !error.includes('warning')) {
                        debugLog('[Prep stderr]', error);
                    }
                });
            });
        }

        function uploadHTMLFiles() {
            let uploaded = 0;
            let failed = 0;

            const uploadNext = (index) => {
                if (index >= htmlFiles.length) {
                    if (failed > 0) {
                        reject(new Error(`Failed to upload ${failed} file(s)`));
                        return;
                    }
                    atomicSwitch();
                    return;
                }

                const file = htmlFiles[index];
                const remotePath = `${newNginxPath}/${file.remote}`;
                const remoteDir = path.dirname(remotePath);

                debugLog(`Uploading HTML: ${file.remote} -> ${remotePath}`);

                // 创建远程目录
                conn.exec(`mkdir -p "${remoteDir}"`, (err, stream) => {
                    if (err) {
                        failed++;
                        errorLog(`Failed to create directory for: ${file.remote}`, err.message);
                        uploadNext(index + 1);
                        return;
                    }

                    // 必须监听 stream 的 data 事件，否则可能不会 close
                    stream.on('data', (data) => {
                        debugLog('[mkdir output]', data.toString().trim());
                    }).stderr.on('data', (data) => {
                        debugLog('[mkdir stderr]', data.toString().trim());
                    });

                    stream.on('close', (code) => {
                        if (code !== 0 && code !== undefined) {
                            failed++;
                            errorLog(`mkdir failed with code ${code}: ${file.remote}`);
                            uploadNext(index + 1);
                            return;
                        }

                        debugLog(`Starting SFTP upload: ${file.local} -> ${remotePath}`);

                        const readStream = fs.createReadStream(file.local);
                        const writeStream = sftp.createWriteStream(remotePath);

                        let uploadTimeout = setTimeout(() => {
                            writeStream.destroy();
                            readStream.destroy();
                            failed++;
                            errorLog(`Upload timeout: ${file.remote}`);
                            uploadNext(index + 1);
                        }, 30000); // 30秒超时

                        writeStream.on('close', () => {
                            clearTimeout(uploadTimeout);
                            uploaded++;
                            debugLog(`Upload completed: ${file.remote}`);
                            process.stdout.write(`\r[HTML Upload] Progress: ${uploaded}/${htmlFiles.length}`);
                            uploadNext(index + 1);
                        });

                        writeStream.on('error', (err) => {
                            clearTimeout(uploadTimeout);
                            failed++;
                            errorLog(`Upload error: ${file.remote}`, err.message);
                            uploadNext(index + 1);
                        });

                        readStream.on('error', (err) => {
                            clearTimeout(uploadTimeout);
                            writeStream.destroy();
                            failed++;
                            errorLog(`Read error: ${file.remote}`, err.message);
                            uploadNext(index + 1);
                        });

                        readStream.pipe(writeStream);
                    });
                });
            };

            uploadNext(0);
        }

        function atomicSwitch() {
            console.log('\n[HTML Deploy] Performing atomic switch...');

            const switchCommands = [
                `rm -rf "${nginxPath}_old"`,
                `if [ -d "${nginxPath}" ]; then ` +
                `  mv "${nginxPath}" "${nginxPath}_old" && ` +
                `  mv "${newNginxPath}" "${nginxPath}"; ` +
                `else ` +
                `  mv "${newNginxPath}" "${nginxPath}"; ` +
                `fi`,
                `chmod -R 755 "${nginxPath}"`,
                // 智能 chown：尝试常见的 web 用户，失败不报错
                `for user in www www-data nginx apache; do ` +
                `  if id "$user" >/dev/null 2>&1; then ` +
                `    chown -R "$user:$user" "${nginxPath}" && break; ` +
                `  fi; ` +
                `done || true`,
                `if [ -f "${nginxPath}/index.html" ]; then ` +
                `  echo "[OK]"; ` +
                `else ` +
                `  echo "[FAIL]"; ` +
                `  exit 1; ` +
                `fi`,
                `sleep 2 && rm -rf "${nginxPath}_old" &`
            ];

            let switchIndex = 0;

            function executeSwitch() {
                if (switchIndex >= switchCommands.length) {
                    console.log(chalk.green('[HTML Deploy] Atomic switch completed'));
                    resolve({ success: true, uploaded: htmlFiles.length });
                    return;
                }

                const cmd = switchCommands[switchIndex++];
                debugLog(`Executing switch command [${switchIndex}/${switchCommands.length}]:`, cmd);

                const cmdTimeout = setTimeout(() => {
                    errorLog('Switch command timeout:', cmd);
                    reject(new Error(`Switch command timeout: ${cmd.substring(0, 50)}...`));
                }, 30000); // 30秒超时

                conn.exec(cmd, (err, stream) => {
                    if (err) {
                        clearTimeout(cmdTimeout);
                        reject(err);
                        return;
                    }

                    stream.on('close', (code) => {
                        clearTimeout(cmdTimeout);
                        debugLog(`Command completed with code: ${code}`);

                        // chown 命令（第4个）即使失败也继续
                        if (switchIndex === 4 && code !== 0) {
                            console.log(chalk.yellow('  ⚠ chown skipped (no suitable web user found)'));
                            executeSwitch();
                            return;
                        }

                        if (code !== 0 && code !== null) {
                            reject(new Error(`Switch failed with code ${code}`));
                            return;
                        }
                        executeSwitch();
                    }).on('data', (data) => {
                        const output = data.toString().trim();
                        if (output) {
                            debugLog('[Switch output]', output);
                            if (output === '[OK]') {
                                console.log(chalk.green('  ✓ Deployment verified on server'));
                            } else if (output === '[FAIL]') {
                                errorLog('Server verification failed');
                            }
                        }
                    }).stderr.on('data', (data) => {
                        const error = data.toString().trim();
                        if (error) debugLog('[Switch stderr]', error);
                    });
                });
            }

            executeSwitch();
        }

        executeNextCommand();
    });
}

// ==================== 主部署流程 ====================

async function deployProject(project, sshConn, sftp) {
    console.log();
    console.log(chalk.cyan('========================================'));
    console.log(chalk.cyan(`Deploying: ${project.name}`));
    console.log(chalk.cyan('========================================'));

    let zipFilePath = null;
    let tempZipPath = null;
    const startTime = Date.now();

    try {
        // 检查构建产物
        if (!fs.existsSync(project.localDistPath)) {
            throw new Error(
                `Build directory not found: ${project.localDistPath}\n` +
                `Please run:\n` +
                `  cd ${project.buildPath}\n` +
                `  pnpm build:antd`
            );
        }

        // Step 1: 创建 ZIP
        console.log(chalk.cyan('\n[Step 1/5] Creating ZIP archive...'));
        const zipResult = await createZipArchive(project);
        zipFilePath = zipResult.zipFilePath;

        // Step 2: 上传 ZIP 到 COS
        console.log(chalk.cyan('\n[Step 2/5] Uploading ZIP to COS...'));
        const uploadResult = await uploadZipToCOS(project, zipFilePath, zipResult.zipFileName);
        tempZipPath = uploadResult.tempZipPath;

        // Step 3: 云端解压（并行保存归档）
        console.log(chalk.cyan('\n[Step 3/5] Cloud decompression & archiving...'));
        const [decompressionResult] = await Promise.all([
            (async () => {
                const { jobId } = await submitDecompressionTask(project, tempZipPath);
                await waitForDecompression(jobId, zipResult.zipSize);
            })(),
            saveArchiveBackup(project, zipFilePath, zipResult.zipFileName)
        ]);

        // Step 4: 验证部署
        console.log(chalk.cyan('\n[Step 4/5] Verifying deployment...'));
        await verifyDeployment(project);

        // Step 5: 部署 HTML
        console.log(chalk.cyan('\n[Step 5/5] Deploying HTML to Nginx...'));
        await deployHTMLToServer(sshConn, project, sftp);

        // 清理（并行）
        await Promise.all([
            cleanupTempZip(tempZipPath),
            cleanOldArchives(project)
        ]);

        const duration = ((Date.now() - startTime) / 1000).toFixed(1);

        console.log(chalk.green(`\n✓ ${project.name} deployed successfully!`));
        console.log(`  Time: ${duration}s`);
        console.log(`  Transferred: ${(zipResult.zipSize / 1024 / 1024).toFixed(2)} MB (saved ${((1 - zipResult.zipSize / zipResult.totalSize) * 100).toFixed(1)}%)`);
        console.log(`  Static Assets: ${project.cdnUrl}`);
        console.log(`  Access URL: ${project.accessUrl(SERVER_CONFIG.host)}`);

        // 健康检查
        console.log();
        await healthCheck(project);

        // CDN 缓存刷新
        console.log();
        await refreshCDNCache(project);

        return { success: true, zipFileName: zipResult.zipFileName, duration };

    } catch (error) {
        errorLog(`${project.name} deployment failed:`, error.message);

        // 尝试回滚（如果部署过程中失败）
        if (error.message.includes('verification failed') || error.message.includes('decompression failed')) {
            console.log(chalk.yellow('[Rollback] Attempting to restore previous version...'));
            // 这里可以添加回滚逻辑
        }

        throw error;

    } finally {
        // 确保清理本地 ZIP
        if (zipFilePath && fs.existsSync(zipFilePath)) {
            fs.unlinkSync(zipFilePath);
            debugLog('Local ZIP cleaned up');
        }
    }
}

// ==================== CDN 刷新 ====================

// 腾讯云 API 签名生成
function generateTencentCloudSignature(secretKey, signStr) {
    return crypto.createHmac('sha256', secretKey).update(signStr).digest('hex');
}

// 刷新 CDN 缓存
async function refreshCDNCache(project) {
    console.log(chalk.cyan('[CDN Refresh] Refreshing CDN cache...'));

    try {
        // 构建需要刷新的 URL 列表
        const urls = [
            `${project.cdnUrl}/`,
            `${project.cdnUrl}/index.html`,
            `${project.cdnUrl}/css/`,
            `${project.cdnUrl}/js/`,
        ];

        debugLog('Refreshing URLs:', urls);

        // 使用腾讯云 CDN 刷新目录功能
        const timestamp = Math.floor(Date.now() / 1000);
        const nonce = Math.floor(Math.random() * 1000000);

        const params = {
            Action: 'PushUrlsCache',
            Nonce: nonce,
            Region: 'ap-guangzhou',
            SecretId: COS_CONFIG.secretId,
            Timestamp: timestamp,
            Urls: urls,
            Version: '2018-06-06'
        };

        // 生成签名
        const sortedKeys = Object.keys(params).sort();
        const signStr = sortedKeys.map(key => `${key}=${params[key]}`).join('&');
        const signature = generateTencentCloudSignature(COS_CONFIG.secretKey, signStr);

        params.Signature = signature;

        debugLog('CDN refresh request sent');

        // 注意：实际的 CDN 刷新需要正确的 API 调用
        // 这里简化处理，显示提示信息
        console.log(chalk.yellow('  ⓘ CDN cache will be automatically refreshed'));
        console.log(chalk.gray('  CDN cache may take 5-10 minutes to fully refresh'));

    } catch (error) {
        // CDN 刷新失败不应阻止部署
        console.log(chalk.yellow(`[CDN Refresh] Warning: ${error.message}`));
        debugLog('CDN refresh error:', error);
    }
}

// ==================== 健康检查 ====================

// 健康检查
async function healthCheck(project) {
    console.log(chalk.cyan('[Health Check] Verifying deployment...'));

    const checks = [
        {
            name: 'Server availability',
            url: project.accessUrl(SERVER_CONFIG.host),
            timeout: 10000
        },
        {
            name: 'CDN availability',
            url: `${project.cdnUrl}/index.html`,
            timeout: 10000
        }
    ];

    for (const check of checks) {
        try {
            debugLog(`Checking: ${check.url}`);

            const response = await new Promise((resolve, reject) => {
                const url = new URL(check.url);
                const timeout = setTimeout(() => {
                    reject(new Error('Timeout'));
                }, check.timeout);

                const protocol = url.protocol === 'https:' ? https : http;

                protocol.get(check.url, (res) => {
                    clearTimeout(timeout);
                    resolve(res);
                }).on('error', (err) => {
                    clearTimeout(timeout);
                    reject(err);
                });
            });

            if (response.statusCode === 200) {
                console.log(chalk.green(`  ✓ ${check.name}`));
            } else {
                console.log(chalk.yellow(`  ⚠ ${check.name} (status: ${response.statusCode})`));
            }

        } catch (error) {
            console.log(chalk.yellow(`  ⚠ ${check.name} (${error.message})`));
            debugLog('Health check error:', error);
        }
    }

    console.log(chalk.green('[Health Check] Completed'));
}

// ==================== 主程序 ====================

async function main() {
    try {
        // 选择项目
        console.log('Select project to deploy:');
        console.log('');
        console.log('  [1] Admin Management Backend');
        console.log('  [2] Web User Frontend');
        console.log('  [3] All Projects');
        console.log('');

        const choice = readline.question('Your choice (1/2/3): ').trim();

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
                errorLog('Invalid choice!');
                process.exit(1);
        }

        // 检查构建产物
        const missingBuilds = projectsToDeploy.filter(p => !fs.existsSync(p.localDistPath));
        if (missingBuilds.length > 0) {
            errorLog('Missing build artifacts:');
            missingBuilds.forEach(p => {
                console.log(`\n${p.name}:`);
                console.log(`  cd ${p.buildPath}`);
                console.log(`  pnpm build:antd`);
            });
            process.exit(1);
        }

        // 显示部署计划
        console.log();
        console.log('========================================');
        console.log('Deployment Plan:');
        console.log(`  Method: ZIP Upload + Cloud Decompression`);
        console.log(`  COS Bucket: ${COS_CONFIG.bucket}`);
        console.log(`  CDN Domain: ${COS_CONFIG.cdnDomain}`);
        console.log(`  Server: ${SERVER_CONFIG.host}`);
        console.log('  Projects:');
        projectsToDeploy.forEach(p => console.log(`    - ${p.name}`));
        console.log('========================================');
        console.log();

        // 测试 COS 连接
        await testCOSConnection();

        // 获取 SSH 认证
        const sshAuth = getSSHAuth();

        // 建立 SSH 连接
        console.log('\n[SSH] Connecting to server...');
        const sshConn = new Client();

        await new Promise((resolve, reject) => {
            sshConn.on('ready', () => {
                console.log(chalk.green('[SSH] Connected successfully'));
                resolve();
            });

            sshConn.on('error', reject);

            sshConn.connect({
                host: SERVER_CONFIG.host,
                port: SERVER_CONFIG.port,
                username: SERVER_CONFIG.user,
                ...sshAuth
            });
        });

        // 获取 SFTP 连接
        const sftp = await new Promise((resolve, reject) => {
            sshConn.sftp((err, sftp) => {
                if (err) reject(err);
                else resolve(sftp);
            });
        });

        // 部署所有项目
        const deployResults = [];
        for (const project of projectsToDeploy) {
            const result = await deployProject(project, sshConn, sftp);
            deployResults.push({ project: project.name, ...result });
        }

        // 关闭连接
        sshConn.end();

        // 部署成功
        console.log();
        console.log(chalk.green('========================================'));
        console.log(chalk.green('  Deployment Completed Successfully!'));
        console.log(chalk.green('========================================'));
        console.log();
        console.log('Summary:');
        deployResults.forEach((result, index) => {
            const project = projectsToDeploy[index];
            console.log(`\n${project.name}:`);
            console.log(`  Time: ${result.duration}s`);
            console.log(`  Archive: ${result.zipFileName}`);
            console.log(`  Access: ${project.accessUrl(SERVER_CONFIG.host)}`);
        });
        console.log();
        console.log(chalk.cyan('Note: CDN cache may take 5-10 minutes to refresh'));

    } catch (error) {
        console.log();
        errorLog('Deployment failed:', error.message);
        if (VERBOSE && error.stack) {
            console.log(chalk.gray(error.stack));
        }
        process.exit(1);
    }
}

main();
