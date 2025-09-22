#!/usr/bin/env node

/**
 * 绿叶项目统一发布工具
 * 功能：构建、部署、生成日志、发送通知
 * 使用：npm run release 或 node scripts/release.mjs
 */

import { execSync } from 'child_process';
import chalk from 'chalk';
import readline from 'readline-sync';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import axios from 'axios';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const PROJECT_ROOT = path.join(__dirname, '..');

// 配置
const CONFIG = {
  dify: {
    apiKey: 'app-zwnIyNUk3jqOeCJ5V38pIkvR',
    apiUrl: 'http://154.9.255.162/v1'
  },
  feishu: {
    webhook: 'https://open.feishu.cn/open-apis/bot/v2/hook/cd69ceec-aaa6-422f-b23a-fac71382ebb0'
  },
  server: {
    host: '42.194.163.176',
    backendPath: '/root/mindfront/work/project/mindtrip_server',
    frontendAdminPath: '/root/mindfront/work/project/mindtrip_apps/admin',
    frontendWebPath: '/root/mindfront/work/project/mindtrip_apps/web'
  }
};

console.log(chalk.cyan('========================================'));
console.log(chalk.cyan('      绿叶项目统一发布工具 v1.0.0'));
console.log(chalk.cyan('========================================'));
console.log();

// 检查是否在项目根目录
if (!fs.existsSync(path.join(PROJECT_ROOT, 'pom.xml'))) {
  console.log(chalk.red('错误：请在项目根目录执行此脚本'));
  process.exit(1);
}

// 解析命令行参数
const args = process.argv.slice(2);
const isAuto = args.includes('--auto');
const versionArg = args.find(arg => arg.startsWith('--version='));

// 1. 获取版本号
let version;
if (versionArg) {
  version = versionArg.split('=')[1];
} else if (!isAuto) {
  version = readline.question('请输入版本号 (如 1.2.0): v');
} else {
  // 自动获取下一个补丁版本
  try {
    const packageJson = JSON.parse(
      fs.readFileSync(path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend/package.json'), 'utf-8')
    );
    const [major, minor, patch] = packageJson.version.split('.');
    version = `${major}.${minor}.${parseInt(patch) + 1}`;
  } catch (error) {
    console.log(chalk.red('无法自动获取版本号，请手动输入'));
    version = readline.question('请输入版本号 (如 1.2.0): v');
  }
}

if (!version) {
  console.log(chalk.red('版本号不能为空'));
  process.exit(1);
}

// 确保版本号格式正确
if (!version.match(/^\d+\.\d+\.\d+$/)) {
  console.log(chalk.red('版本号格式错误，应为 x.y.z 格式'));
  process.exit(1);
}

// 2. 选择发布内容
let choice = '3'; // 默认全部发布
if (!isAuto) {
  console.log('\n请选择要发布的内容:');
  console.log('  [1] 仅后端');
  console.log('  [2] 仅前端');
  console.log('  [3] 前端 + 后端（默认）');
  choice = readline.question('请选择 (1/2/3): ') || '3';
}

// 3. 确认发布
const releaseContent = choice === '1' ? '后端' : choice === '2' ? '前端' : '前端+后端';
console.log('\n' + chalk.yellow('即将发布:'));
console.log(`  版本: v${version}`);
console.log(`  内容: ${releaseContent}`);
console.log(`  服务器: ${CONFIG.server.host}`);

if (!isAuto) {
  const confirm = readline.question('\n确认发布? (y/N): ');
  if (confirm.toLowerCase() !== 'y') {
    console.log('已取消发布');
    process.exit(0);
  }
}

// 主发布函数
async function release() {
  const startTime = Date.now();
  const changelog = [];
  let hasError = false;
  
  try {
    // 4. 构建和部署后端
    if (choice === '1' || choice === '3') {
      console.log(chalk.blue('\n========== 后端发布 =========='));
      
      // 构建
      console.log(chalk.blue('[1/2] 构建后端...'));
      try {
        process.chdir(PROJECT_ROOT);
        execSync('mvn clean package -DskipTests', { 
          stdio: 'inherit',
          cwd: PROJECT_ROOT 
        });
        console.log(chalk.green('✓ 后端构建成功'));
        
        // 检查 JAR 文件
        const jarPath = path.join(PROJECT_ROOT, 'yudao-server/target/yudao-server.jar');
        if (!fs.existsSync(jarPath)) {
          throw new Error('JAR 文件未生成');
        }
        const jarSize = (fs.statSync(jarPath).size / 1024 / 1024).toFixed(2);
        console.log(chalk.gray(`  JAR 文件大小: ${jarSize} MB`));
      } catch (error) {
        console.log(chalk.red('✗ 后端构建失败'));
        throw error;
      }
      
      // 部署
      console.log(chalk.blue('[2/2] 部署后端...'));
      try {
        execSync('node script/windows/deploy-backend.mjs', { 
          stdio: 'inherit',
          cwd: PROJECT_ROOT 
        });
        console.log(chalk.green('✓ 后端部署成功'));
        changelog.push('- 后端服务更新');
      } catch (error) {
        console.log(chalk.red('✗ 后端部署失败'));
        throw error;
      }
    }
    
    // 5. 构建和部署前端
    if (choice === '2' || choice === '3') {
      console.log(chalk.blue('\n========== 前端发布 =========='));
      
      // Admin 项目
      console.log(chalk.blue('[1/4] 构建 Admin 管理后台...'));
      try {
        const adminPath = path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend/apps/admin');
        process.chdir(adminPath);
        
        // 安装依赖（如果需要）
        if (!fs.existsSync(path.join(adminPath, 'node_modules'))) {
          console.log(chalk.gray('  安装依赖...'));
          execSync('pnpm install --frozen-lockfile', { stdio: 'inherit' });
        }
        
        // 构建
        execSync('pnpm build', { stdio: 'inherit' });
        
        // 检查 dist.zip
        if (!fs.existsSync(path.join(adminPath, 'dist.zip'))) {
          throw new Error('Admin dist.zip 未生成');
        }
        console.log(chalk.green('✓ Admin 构建成功'));
      } catch (error) {
        console.log(chalk.red('✗ Admin 构建失败'));
        throw error;
      }
      
      // Web 项目
      console.log(chalk.blue('[2/4] 构建 Web 前台...'));
      try {
        const webPath = path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend/apps/web');
        process.chdir(webPath);
        
        // 安装依赖（如果需要）
        if (!fs.existsSync(path.join(webPath, 'node_modules'))) {
          console.log(chalk.gray('  安装依赖...'));
          execSync('pnpm install --frozen-lockfile', { stdio: 'inherit' });
        }
        
        // 构建
        execSync('pnpm build', { stdio: 'inherit' });
        
        // 检查 dist.zip
        if (!fs.existsSync(path.join(webPath, 'dist.zip'))) {
          throw new Error('Web dist.zip 未生成');
        }
        console.log(chalk.green('✓ Web 构建成功'));
      } catch (error) {
        console.log(chalk.red('✗ Web 构建失败'));
        throw error;
      }
      
      // 部署前端
      console.log(chalk.blue('[3/4] 部署前端...'));
      try {
        process.chdir(PROJECT_ROOT);
        execSync('node script/windows/deploy-frontend.mjs', { 
          stdio: 'inherit',
          cwd: PROJECT_ROOT 
        });
        console.log(chalk.green('✓ 前端部署成功'));
        changelog.push('- 前端界面优化');
      } catch (error) {
        console.log(chalk.red('✗ 前端部署失败'));
        throw error;
      }
    }
    
    // 6. 生成 Git Tag
    console.log(chalk.blue('\n[4/4] 创建版本标签...'));
    try {
      process.chdir(PROJECT_ROOT);
      
      // 检查是否有未提交的更改
      const gitStatus = execSync('git status --porcelain', { encoding: 'utf-8' });
      if (gitStatus.trim()) {
        console.log(chalk.yellow('警告：存在未提交的更改'));
        if (!isAuto) {
          const continueTag = readline.question('是否继续创建标签? (y/N): ');
          if (continueTag.toLowerCase() !== 'y') {
            console.log('跳过创建标签');
          } else {
            createGitTag(version);
          }
        }
      } else {
        createGitTag(version);
      }
    } catch (error) {
      console.log(chalk.yellow('⚠ Git 标签创建失败（非关键错误）'));
      console.log(chalk.gray(error.message));
    }
    
    // 7. 生成发布日志
    console.log(chalk.blue('\n生成发布日志...'));
    const releaseNotes = await generateReleaseNotes(version, changelog);
    
    // 8. 发送飞书通知
    await notifyFeishu(version, releaseNotes, releaseContent);
    
    // 记录发布
    recordRelease(version, releaseContent);
    
    // 发布成功
    const duration = ((Date.now() - startTime) / 1000 / 60).toFixed(1);
    console.log(chalk.green('\n========================================'));
    console.log(chalk.green('           发布成功！'));
    console.log(chalk.green('========================================'));
    console.log(`  版本: v${version}`);
    console.log(`  内容: ${releaseContent}`);
    console.log(`  耗时: ${duration} 分钟`);
    console.log(`  访问地址: http://${CONFIG.server.host}/`);
    console.log();
    
  } catch (error) {
    hasError = true;
    console.error(chalk.red('\n❌ 发布失败:'), error.message);
    
    // 发送失败通知
    try {
      await notifyFeishuError(version, error.message);
    } catch (notifyError) {
      console.error('发送失败通知失败:', notifyError.message);
    }
    
    process.exit(1);
  }
}

// 创建 Git 标签
function createGitTag(version) {
  execSync(`git tag -a v${version} -m "Release v${version}"`, { stdio: 'inherit' });
  execSync(`git push origin v${version}`, { stdio: 'inherit' });
  console.log(chalk.green(`✓ Git 标签 v${version} 创建成功`));
}

// 生成发布日志（调用 Dify API）
async function generateReleaseNotes(version, changes) {
  try {
    // 获取最近的提交记录
    let commits = '';
    try {
      commits = execSync('git log --oneline -10', { encoding: 'utf-8', cwd: PROJECT_ROOT });
    } catch (error) {
      commits = '无法获取提交记录';
    }
    
    const prompt = `请基于以下信息生成一份简洁的产品更新说明（面向运营团队）：

版本号: v${version}
更新内容: 
${changes.join('\n')}

最近提交记录:
${commits}

要求：
1. 用通俗易懂的语言，避免技术术语
2. 突出对用户的价值和改进
3. 控制在 200 字以内
4. 如果有新功能请重点说明
5. 分类展示：新功能、优化、修复`;

    const response = await axios.post(
      `${CONFIG.dify.apiUrl}/chat-messages`,
      {
        inputs: {},
        query: prompt,
        response_mode: "blocking",
        user: "release-bot"
      },
      {
        headers: {
          'Authorization': `Bearer ${CONFIG.dify.apiKey}`,
          'Content-Type': 'application/json'
        },
        timeout: 30000
      }
    );
    
    console.log(chalk.green('✓ AI 发布日志生成成功'));
    return response.data.answer || `版本 v${version} 已发布\n${changes.join('\n')}`;
    
  } catch (error) {
    console.warn(chalk.yellow('⚠ AI 生成失败，使用默认模板'));
    console.log(chalk.gray(error.message));
    
    // 使用默认模板
    return `📦 **版本 v${version} 更新内容**

${changes.join('\n')}

感谢您的使用和支持！如有问题请及时反馈。`;
  }
}

// 发送飞书通知
async function notifyFeishu(version, notes, content) {
  const message = {
    msg_type: "interactive",
    card: {
      config: { wide_screen_mode: true },
      header: {
        title: { 
          content: `🚀 绿叶项目 v${version} 发布成功`, 
          tag: "plain_text" 
        },
        template: "green"
      },
      elements: [
        {
          tag: "markdown",
          content: notes
        },
        {
          tag: "hr"
        },
        {
          tag: "div",
          fields: [
            {
              is_short: true,
              text: {
                content: `**发布内容：** ${content}`,
                tag: "lark_md"
              }
            },
            {
              is_short: true,
              text: {
                content: `**服务器：** ${CONFIG.server.host}`,
                tag: "lark_md"
              }
            }
          ]
        },
        {
          tag: "action",
          actions: [
            {
              tag: "button",
              text: { 
                content: "访问系统", 
                tag: "plain_text" 
              },
              type: "primary",
              url: `http://${CONFIG.server.host}/`
            },
            {
              tag: "button",
              text: { 
                content: "管理后台", 
                tag: "plain_text" 
              },
              type: "default",
              url: `http://${CONFIG.server.host}/admin/`
            }
          ]
        },
        {
          tag: "note",
          elements: [
            {
              tag: "plain_text",
              content: `发布时间: ${new Date().toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })}`
            }
          ]
        }
      ]
    }
  };

  try {
    await axios.post(CONFIG.feishu.webhook, message, { timeout: 10000 });
    console.log(chalk.green('✓ 飞书通知发送成功'));
  } catch (error) {
    console.warn(chalk.yellow('⚠ 飞书通知发送失败:'), error.message);
  }
}

// 发送失败通知
async function notifyFeishuError(version, errorMessage) {
  const message = {
    msg_type: "interactive",
    card: {
      config: { wide_screen_mode: true },
      header: {
        title: { 
          content: `❌ 绿叶项目 v${version} 发布失败`, 
          tag: "plain_text" 
        },
        template: "red"
      },
      elements: [
        {
          tag: "markdown",
          content: `**错误信息：**\n${errorMessage}\n\n请检查并重试。`
        },
        {
          tag: "note",
          elements: [
            {
              tag: "plain_text",
              content: `时间: ${new Date().toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })}`
            }
          ]
        }
      ]
    }
  };

  try {
    await axios.post(CONFIG.feishu.webhook, message, { timeout: 10000 });
  } catch (error) {
    // 静默失败
  }
}

// 记录发布历史
function recordRelease(version, content) {
  const record = {
    version: `v${version}`,
    content: content,
    timestamp: new Date().toISOString(),
    deployer: process.env.USER || process.env.USERNAME || 'unknown',
    server: CONFIG.server.host
  };
  
  const logFile = path.join(PROJECT_ROOT, 'releases.log');
  
  try {
    fs.appendFileSync(logFile, JSON.stringify(record) + '\n');
  } catch (error) {
    // 静默失败，不影响发布
  }
}

// 执行发布
release().catch(error => {
  console.error(chalk.red('发布过程出现未预期的错误:'), error);
  process.exit(1);
});