#!/usr/bin/env node

// Mindtrip COS Upload Test Script
// Usage: node deploy-frontend-cos-test.mjs
// Purpose: Test COS upload functionality only, no server deployment

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import readline from 'readline-sync';
import chalk from 'chalk';
import { execSync } from 'child_process';
import { promisify } from 'util';
import pLimit from 'p-limit';

// 设置 Windows 控制台编码为 UTF-8
if (process.platform === 'win32') {
    try {
        execSync('chcp 65001', { stdio: 'ignore' });
    } catch (e) {
        // 忽略错误
    }
}

const execAsync = promisify(execSync);
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

console.log(chalk.cyan('========================================'));
console.log(chalk.cyan('   Mindtrip COS Upload Test'));
console.log(chalk.cyan('========================================'));
console.log();

// COS配置（使用直链地址）
const COS_CONFIG = {
    bucket: 'mindtrip-1305613707',
    region: 'ap-guangzhou',
    cosUrl: 'https://mindtrip-1305613707.cos.ap-guangzhou.myqcloud.com',
    testPath: '/admin-test',  // 测试路径，避免影响生产环境
    secretId: process.env.TENCENT_SECRET_ID || '',
    secretKey: process.env.TENCENT_SECRET_KEY || ''
};

// 项目配置
const TEST_PROJECT = {
    name: 'Admin Frontend Test',
    localDistPath: path.join(__dirname, '../../yudao-ui/lvye-project-frontend/apps/admin/dist'),
    cosPath: COS_CONFIG.testPath,
    directUrl: `${COS_CONFIG.cosUrl}${COS_CONFIG.testPath}`
};

// 支持的静态资源文件扩展名
const STATIC_FILE_EXTENSIONS = [
    '.js', '.css', '.png', '.jpg', '.jpeg', '.gif', '.svg', '.ico', 
    '.woff', '.woff2', '.ttf', '.eot', '.map', '.br', '.gz',
    '.webp', '.mp4', '.mp3', '.pdf', '.zip'
];

// 排除的文件扩展名和文件名
const EXCLUDED_FILES = [
    '.html', '.json', '.txt', '.md', '.xml',
    'robots.txt', 'sitemap.xml', 'manifest.json'
];

// 路径标准化函数
function normalizePath(filePath) {
    return filePath.replace(/\\/g, '/').replace(/\/+/g, '/');
}

// 智能进度跟踪器
class UploadProgressTracker {
    constructor(totalFiles, totalSize) {
        this.totalFiles = totalFiles;
        this.totalSize = totalSize;
        this.completed = 0;
        this.uploadedSize = 0;
        this.startTime = Date.now();
        this.failed = 0;
    }
    
    update(fileSize, success = true) {
        this.completed++;
        if (success) {
            this.uploadedSize += fileSize;
        } else {
            this.failed++;
        }
        
        const elapsed = (Date.now() - this.startTime) / 1000;
        const percent = Math.round((this.completed / this.totalFiles) * 100);
        const speed = this.uploadedSize / 1024 / 1024 / elapsed; // MB/s
        const remaining = this.totalFiles - this.completed;
        const eta = remaining > 0 ? remaining / (this.completed / elapsed) : 0;
        
        process.stdout.write(
            `\\r[上传] ${percent}% (${this.completed}/${this.totalFiles}) ` +
            `${speed.toFixed(1)}MB/s ETA:${eta.toFixed(0)}s ` +
            `${this.failed > 0 ? chalk.red(`失败:${this.failed}`) : ''}`
        );
    }
    
    finish() {
        console.log(); // 换行
        const duration = ((Date.now() - this.startTime) / 1000).toFixed(1);
        const avgSpeed = (this.uploadedSize / 1024 / 1024 / (duration / 60)).toFixed(2);
        
        return {
            duration,
            avgSpeed,
            totalSize: this.uploadedSize,
            completed: this.completed - this.failed,
            failed: this.failed
        };
    }
}

// 文件优化排序
function optimizeFileOrder(staticFiles) {
    // 按文件大小排序，小文件优先（更快看到进度）
    return staticFiles.sort((a, b) => a.size - b.size);
}

// 文件分组
function groupFilesBySize(staticFiles) {
    const smallFiles = staticFiles.filter(f => f.size < 100 * 1024); // <100KB
    const mediumFiles = staticFiles.filter(f => f.size >= 100 * 1024 && f.size < 1024 * 1024); // 100KB-1MB  
    const largeFiles = staticFiles.filter(f => f.size >= 1024 * 1024); // >1MB
    
    return { smallFiles, mediumFiles, largeFiles };
}

// 检查COSCLI工具
function checkCOSCLI() {
    console.log(chalk.blue('[检查] 正在检查COSCLI工具...'));
    
    const possiblePaths = [
        path.join(process.env.USERPROFILE, 'coscli.exe'),
        'coscli.exe',
        'coscli'
    ];
    
    for (const coscliPath of possiblePaths) {
        try {
            const result = execSync(`"${coscliPath}" --version`, { encoding: 'utf8', stdio: 'pipe' });
            console.log(chalk.green(`[找到] COSCLI: ${coscliPath}`));
            console.log(chalk.gray(`[版本] ${result.trim()}`));
            return coscliPath;
        } catch (e) {
            continue;
        }
    }
    
    throw new Error(
        'COSCLI工具未找到！请先安装：\n' +
        '1. 运行: npm run setup:cos\n' + 
        '2. 或手动下载: https://github.com/tencentyun/coscli/releases\n' +
        '3. 配置认证: npm run config:cos'
    );
}

// 扫描并分类文件
function scanAndClassifyFiles(distPath) {
    console.log(chalk.blue('[扫描] 正在扫描构建产物...'));
    
    if (!fs.existsSync(distPath)) {
        throw new Error(`构建目录不存在: ${distPath}`);
    }
    
    const staticFiles = [];
    const htmlFiles = [];
    const excludedFiles = [];
    let totalSize = 0;
    
    function walkDirectory(dir, relativePath = '') {
        const items = fs.readdirSync(dir);
        
        for (const item of items) {
            const fullPath = path.join(dir, item);
            const relativeFilePath = normalizePath(path.join(relativePath, item));
            const stat = fs.statSync(fullPath);
            
            if (stat.isDirectory()) {
                walkDirectory(fullPath, relativeFilePath);
            } else {
                const ext = path.extname(item).toLowerCase();
                const fileSize = stat.size;
                totalSize += fileSize;
                
                const fileInfo = {
                    localPath: fullPath,
                    relativePath: relativeFilePath,
                    size: fileSize,
                    extension: ext
                };
                
                if (EXCLUDED_FILES.includes(ext) || EXCLUDED_FILES.includes(item)) {
                    excludedFiles.push(fileInfo);
                } else if (ext === '.html') {
                    htmlFiles.push(fileInfo);
                } else if (STATIC_FILE_EXTENSIONS.includes(ext) || ext === '') {
                    staticFiles.push(fileInfo);
                } else {
                    excludedFiles.push(fileInfo);
                }
            }
        }
    }
    
    walkDirectory(distPath);
    
    // 输出扫描结果
    console.log(chalk.green(`[扫描完成] 找到文件:`));
    console.log(`  静态资源: ${chalk.cyan(staticFiles.length)} 个文件`);
    console.log(`  HTML文件: ${chalk.yellow(htmlFiles.length)} 个文件`);
    console.log(`  排除文件: ${chalk.gray(excludedFiles.length)} 个文件`);
    console.log(`  总大小: ${chalk.magenta((totalSize / 1024 / 1024).toFixed(2))} MB`);
    console.log();
    
    return { staticFiles, htmlFiles, excludedFiles, totalSize };
}

// 并行上传静态文件到COS
async function uploadStaticFiles(coscliPath, staticFiles) {
    const CONCURRENT_LIMIT = 2; // 并发数限制，避免过载
    const limit = pLimit(CONCURRENT_LIMIT);
    
    console.log(chalk.blue(`[上传] 开始并行上传 ${staticFiles.length} 个静态文件到COS...`));
    console.log(chalk.gray(`[信息] 使用 ${CONCURRENT_LIMIT} 线程并发上传，COSCLI会自动创建必需的目录结构`));
    console.log();
    
    // 优化文件上传顺序（小文件优先）
    const optimizedFiles = optimizeFileOrder([...staticFiles]);
    
    // 分析文件分组情况
    const { smallFiles, mediumFiles, largeFiles } = groupFilesBySize(optimizedFiles);
    console.log(chalk.gray(`[文件分析] 小文件: ${smallFiles.length}个, 中等: ${mediumFiles.length}个, 大文件: ${largeFiles.length}个`));
    console.log();
    
    const directories = new Set();
    const totalSize = staticFiles.reduce((sum, file) => sum + file.size, 0);
    const progressTracker = new UploadProgressTracker(staticFiles.length, totalSize);
    
    // 创建上传任务
    const uploadTasks = optimizedFiles.map((file, index) =>
        limit(async () => {
            const normalizedRelativePath = normalizePath(file.relativePath);
            const cosPath = `cos://${COS_CONFIG.bucket}${TEST_PROJECT.cosPath}/${normalizedRelativePath}`;
            
            // 收集目录信息
            const dir = normalizedRelativePath.substring(0, normalizedRelativePath.lastIndexOf('/'));
            if (dir) directories.add(dir);
            
            try {
                const cmd = `"${coscliPath}" cp "${file.localPath}" "${cosPath}"`;
                await new Promise((resolve, reject) => {
                    try {
                        execSync(cmd, { stdio: 'pipe' });
                        resolve();
                    } catch (error) {
                        reject(error);
                    }
                });
                
                // 更新进度
                progressTracker.update(file.size, true);
                
                return { success: true, file, index };
            } catch (error) {
                // 更新进度（失败）
                progressTracker.update(file.size, false);
                
                return { 
                    success: false, 
                    file, 
                    error: error.message, 
                    index 
                };
            }
        })
    );
    
    // 等待所有上传完成
    const results = await Promise.all(uploadTasks);
    const stats = progressTracker.finish();
    
    // 统计结果
    const successResults = results.filter(r => r.success);
    const failedResults = results.filter(r => !r.success);
    
    console.log(chalk.green(`[上传完成] 统计信息:`));
    console.log(`  成功: ${chalk.green(successResults.length)} 个文件`);
    console.log(`  失败: ${chalk.red(failedResults.length)} 个文件`);
    console.log(`  上传大小: ${chalk.cyan((stats.totalSize / 1024 / 1024).toFixed(2))} MB`);
    console.log(`  创建目录: ${chalk.magenta(directories.size)} 个`);
    console.log(`  用时: ${chalk.magenta(stats.duration)} 秒`);
    console.log(`  平均速度: ${chalk.blue(stats.avgSpeed)} MB/分钟`);
    console.log(`  并发线程: ${chalk.cyan(CONCURRENT_LIMIT)} 个`);
    
    // 显示创建的目录结构
    if (directories.size > 0) {
        console.log();
        console.log(chalk.blue(`[目录结构] 自动创建了以下目录:`));
        Array.from(directories).sort().forEach(dir => {
            console.log(chalk.gray(`  ${TEST_PROJECT.cosPath}/${dir}/`));
        });
    }
    console.log();
    
    // 显示失败文件
    if (failedResults.length > 0) {
        console.log(chalk.red(`[失败文件列表]:`));
        failedResults.slice(0, 10).forEach(({ file, error }) => { // 只显示前10个
            console.log(`  ${chalk.red('✗')} ${file.relativePath} - ${error}`);
        });
        if (failedResults.length > 10) {
            console.log(chalk.gray(`  ... 还有 ${failedResults.length - 10} 个失败文件`));
        }
        console.log();
    }
    
    return { 
        uploadedCount: successResults.length, 
        failedCount: failedResults.length, 
        uploadedSize: stats.totalSize, 
        successFiles: successResults.map(r => r.file), 
        failedFiles: failedResults
    };
}

// 验证上传结果
async function verifyUpload(coscliPath) {
    console.log(chalk.blue('[验证] 检查COS上传结果...'));
    
    try {
        const listCmd = `"${coscliPath}" ls cos://${COS_CONFIG.bucket}${TEST_PROJECT.cosPath}/`;
        const result = execSync(listCmd, { encoding: 'utf8', stdio: 'pipe' });
        
        const lines = result.split('\\n').filter(line => line.trim() && !line.includes('TOTAL'));
        const fileCount = lines.length;
        
        console.log(chalk.green(`[验证成功] COS中找到 ${fileCount} 个文件`));
        
        // 检查几个关键文件类型
        const keyTypes = ['css', 'js', 'png', 'ico'];
        for (const type of keyTypes) {
            try {
                const typeCmd = `"${coscliPath}" ls cos://${COS_CONFIG.bucket}${TEST_PROJECT.cosPath}/ | grep "\\.${type}$"`;
                const typeResult = execSync(typeCmd, { encoding: 'utf8', stdio: 'pipe' });
                const typeCount = typeResult.split('\\n').filter(line => line.trim()).length;
                console.log(`  ${type.toUpperCase()}文件: ${chalk.cyan(typeCount)} 个`);
            } catch (e) {
                console.log(`  ${type.toUpperCase()}文件: ${chalk.gray('0')} 个`);
            }
        }
        
        return fileCount;
    } catch (error) {
        console.log(chalk.red(`[验证失败] ${error.message}`));
        return 0;
    }
}

// 生成访问URL列表
function generateAccessUrls(successFiles) {
    console.log(chalk.blue('[URL] 生成访问链接...'));
    
    const urls = successFiles.map(file => {
        const url = `${TEST_PROJECT.directUrl}/${file.relativePath}`;
        return {
            file: file.relativePath,
            url: url,
            type: file.extension,
            size: (file.size / 1024).toFixed(1) + ' KB'
        };
    });
    
    // 按文件类型分组显示
    const groupedUrls = urls.reduce((groups, item) => {
        const type = item.type || 'other';
        if (!groups[type]) groups[type] = [];
        groups[type].push(item);
        return groups;
    }, {});
    
    console.log(chalk.green(`[URL列表] 按类型分组:`));
    Object.keys(groupedUrls).sort().forEach(type => {
        const items = groupedUrls[type];
        console.log(`\\n  ${chalk.cyan(type.toUpperCase())} (${items.length}个):`);
        items.slice(0, 5).forEach(item => { // 只显示前5个
            console.log(`    ${chalk.gray(item.file)} - ${item.size}`);
            console.log(`    ${chalk.blue(item.url)}`);
        });
        if (items.length > 5) {
            console.log(`    ${chalk.gray(`... 还有 ${items.length - 5} 个文件`)}`);
        }
    });
    
    return urls;
}

// 清理测试文件
async function cleanupTestFiles(coscliPath) {
    const shouldClean = readline.question('\\n是否清理测试文件？(y/N): ');
    
    if (shouldClean.toLowerCase() === 'y') {
        console.log(chalk.blue('[清理] 正在删除测试文件...'));
        
        try {
            const deleteCmd = `"${coscliPath}" rm cos://${COS_CONFIG.bucket}${TEST_PROJECT.cosPath}/ -r`;
            execSync(deleteCmd, { stdio: 'pipe' });
            console.log(chalk.green('[清理完成] 测试文件已删除'));
        } catch (error) {
            console.log(chalk.red(`[清理失败] ${error.message}`));
        }
    } else {
        console.log(chalk.yellow('[保留] 测试文件将保留在COS中'));
        console.log(chalk.gray(`访问路径: ${TEST_PROJECT.directUrl}/`));
    }
}

// 主程序
async function main() {
    try {
        console.log(`测试配置:`);
        console.log(`  COS存储桶: ${chalk.cyan(COS_CONFIG.bucket)}`);
        console.log(`  测试路径: ${chalk.yellow(TEST_PROJECT.cosPath)}`);
        console.log(`  直链地址: ${chalk.blue(TEST_PROJECT.directUrl)}`);
        console.log(`  本地路径: ${chalk.gray(TEST_PROJECT.localDistPath)}`);
        console.log();
        
        // 确认继续
        const confirm = readline.question('确认开始测试？(Y/n): ');
        if (confirm.toLowerCase() === 'n') {
            console.log('测试已取消');
            return;
        }
        
        // 1. 检查COSCLI工具
        const coscliPath = checkCOSCLI();
        console.log();
        
        // 2. 扫描和分类文件
        const { staticFiles, htmlFiles, excludedFiles, totalSize } = scanAndClassifyFiles(TEST_PROJECT.localDistPath);
        
        if (staticFiles.length === 0) {
            console.log(chalk.yellow('[警告] 没有找到需要上传的静态文件'));
            return;
        }
        
        // 3. 上传静态文件
        const uploadResult = await uploadStaticFiles(coscliPath, staticFiles);
        
        // 4. 验证上传结果
        const cosFileCount = await verifyUpload(coscliPath);
        console.log();
        
        // 5. 生成访问URL
        if (uploadResult.successFiles.length > 0) {
            generateAccessUrls(uploadResult.successFiles);
        }
        
        // 6. 显示最终统计
        console.log(chalk.green('\\n========================================'));
        console.log(chalk.green('   COS上传测试完成'));
        console.log(chalk.green('========================================'));
        console.log(`测试结果:`);
        console.log(`  ✅ 上传成功: ${chalk.green(uploadResult.uploadedCount)} 个文件`);
        console.log(`  ❌ 上传失败: ${chalk.red(uploadResult.failedCount)} 个文件`);
        console.log(`  📁 COS中文件: ${chalk.cyan(cosFileCount)} 个`);
        console.log(`  📊 上传大小: ${chalk.magenta((uploadResult.uploadedSize / 1024 / 1024).toFixed(2))} MB`);
        console.log();
        console.log(`访问测试:`);
        console.log(`  基础URL: ${chalk.blue(TEST_PROJECT.directUrl)}/`);
        console.log(`  例如CSS: ${chalk.blue(TEST_PROJECT.directUrl)}/css/index-xxx.css`);
        console.log(`  例如JS:  ${chalk.blue(TEST_PROJECT.directUrl)}/js/index-xxx.js`);
        console.log();
        
        // 7. 可选清理
        await cleanupTestFiles(coscliPath);
        
    } catch (error) {
        console.log();
        console.log(chalk.red('========================================'));
        console.log(chalk.red('   测试失败'));
        console.log(chalk.red('========================================'));
        console.log(chalk.red('错误信息:'), error.message);
        
        if (error.message.includes('COSCLI')) {
            console.log();
            console.log(chalk.yellow('解决方案:'));
            console.log('1. 安装COSCLI: npm run setup:cos');
            console.log('2. 配置认证: npm run config:cos');
            console.log('3. 手动下载: https://github.com/tencentyun/coscli/releases');
        }
        
        process.exit(1);
    }
}

// 运行主程序
main();