#!/usr/bin/env node

/**
 * 心之旅项目统一发布工具
 * 功能：构建、部署、生成日志、发送通知
 * 使用：npm run release 或 node script/release.mjs
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
    apiKey: 'app-27WVHbSe1uUxcd54gDGKySl1',
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
console.log(chalk.cyan('      心之旅项目统一发布工具 v1.0.0'));
console.log(chalk.cyan('========================================'));
console.log();

// 检查当前分支必须是 master
try {
  const currentBranch = execSync('git branch --show-current', {
    encoding: 'utf-8',
    cwd: PROJECT_ROOT
  }).trim();
  
  if (currentBranch !== 'master') {
    console.log(chalk.red('❌ 错误：发布必须在 master 分支进行'));
    console.log(chalk.yellow(`   当前分支：${currentBranch}`));
    console.log(chalk.gray('   请先切换到 master 分支：git checkout master'));
    process.exit(1);
  }
  
  console.log(chalk.green('✓ 当前分支：master'));
  
  // 拉取最新代码
  console.log(chalk.blue('正在同步最新代码...'));
  execSync('git pull origin master', {
    stdio: 'inherit',
    cwd: PROJECT_ROOT
  });
  console.log(chalk.green('✓ 代码已同步'));
  console.log();
  
} catch (error) {
  console.error(chalk.red('Git 操作失败:'), error.message);
  process.exit(1);
}

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
        const frontendPath = path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend');
        process.chdir(frontendPath);
        
        // 构建 Admin
        execSync('pnpm build:admin', { stdio: 'inherit' });
        
        // 检查 dist.zip
        const adminDistZip = path.join(frontendPath, 'apps/admin/dist.zip');
        if (!fs.existsSync(adminDistZip)) {
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
        const frontendPath = path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend');
        process.chdir(frontendPath);
        
        // 构建 Web
        execSync('pnpm build:web', { stdio: 'inherit' });
        
        // 检查 dist.zip
        const webDistZip = path.join(frontendPath, 'apps/web/dist.zip');
        if (!fs.existsSync(webDistZip)) {
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
    // 确定发布类型
    const releaseType = determineReleaseType(version);
    const releaseNotes = await generateReleaseNotes(version, changelog, releaseType);
    
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

// 确定发布类型
function determineReleaseType(version) {
  // 根据版本号判断发布类型
  const parts = version.split('.');
  const [major, minor, patch] = parts.map(Number);
  
  // 获取上一个版本（可以从 git tag 获取）
  try {
    const tags = execSync('git tag -l "v*" --sort=-version:refname | head -2', { encoding: 'utf-8' })
      .trim()
      .split('\n');
    
    if (tags.length > 1) {
      const prevVersion = tags[1].replace('v', '');
      const [prevMajor, prevMinor, prevPatch] = prevVersion.split('.').map(Number);
      
      if (major > prevMajor) return 'major';
      if (minor > prevMinor) return 'minor';
      if (patch > prevPatch) return 'patch';
    }
  } catch (error) {
    // 默认为 patch
  }
  
  // 或者根据命令行参数
  const args = process.argv.slice(2);
  if (args.includes('--major')) return 'major';
  if (args.includes('--minor')) return 'minor';
  if (args.includes('--hotfix')) return 'hotfix';
  
  return 'patch'; // 默认
}

// 创建 Git 标签
function createGitTag(version) {
  execSync(`git tag -a v${version} -m "Release v${version}"`, { stdio: 'inherit' });
  execSync(`git push origin v${version}`, { stdio: 'inherit' });
  console.log(chalk.green(`✓ Git 标签 v${version} 创建成功`));
}

// 生成发布日志（调用 Dify Workflow API）
async function generateReleaseNotes(version, changes, releaseType = 'patch') {
  try {
    // 获取提交记录 - 智能判断获取方式
    let commits = '';
    try {
      // 获取所有版本 tags
      const allTags = execSync('git tag -l "v*" --sort=-version:refname', { 
        encoding: 'utf-8', 
        cwd: PROJECT_ROOT 
      }).trim().split('\n').filter(tag => tag);
      
      const currentTag = `v${version}`;
      
      if (allTags.length > 0 && !allTags.includes(currentTag)) {
        // 准备发布新版本，获取最新 tag 到 HEAD 的提交
        const latestTag = allTags[0];
        console.log(chalk.blue(`获取 ${latestTag} 到 HEAD 之间的提交`));
        commits = execSync(`git log ${latestTag}..HEAD --oneline`, { 
          encoding: 'utf-8', 
          cwd: PROJECT_ROOT 
        });
      } else if (allTags.length >= 2) {
        // 获取最新两个 tag 之间的提交
        console.log(chalk.blue(`获取 ${allTags[1]} 到 ${allTags[0]} 之间的提交`));
        commits = execSync(`git log ${allTags[1]}..${allTags[0]} --oneline`, { 
          encoding: 'utf-8', 
          cwd: PROJECT_ROOT 
        });
      } else {
        // 首次发布或只有一个 tag
        console.log(chalk.blue('获取最近 15 条提交'));
        commits = execSync('git log --oneline -15', { 
          encoding: 'utf-8', 
          cwd: PROJECT_ROOT 
        });
      }
    } catch (error) {
      // 如果出错，默认获取最近10条
      try {
        commits = execSync('git log --oneline -10', { 
          encoding: 'utf-8', 
          cwd: PROJECT_ROOT 
        });
      } catch (e) {
        commits = '无法获取提交记录';
      }
    }
    
    // 清理和格式化commits，移除commit hash，只保留commit message
    const commitLines = commits.split('\n')
      .filter(line => line.trim())
      .map(line => {
        // 移除开头的commit hash
        return line.replace(/^[a-f0-9]{7,}\s+/, '');
      })
      .slice(0, 8); // 最多8条，避免太长
    
    // 构建精简的query - 直接使用commit信息
    const prompt = commitLines.join('；').substring(0, 150); // 限制在150字符内

    // 调试：打印发送的内容
    console.log(chalk.blue('\n[调试] 准备发送给 Dify 的内容：'));
    console.log(chalk.gray('API URL:'), CONFIG.dify.apiUrl + '/workflows/run');
    console.log(chalk.gray('Git Commits:'));
    commitLines.forEach(commit => console.log(chalk.gray('  - ' + commit)));
    console.log(chalk.gray('Prompt 长度:'), prompt.length, '字符');
    console.log(chalk.gray('发布类型:'), releaseType);

    // 使用 Workflow API - 简化的输入
    const requestBody = {
      inputs: {
        query: prompt,  // 主要输入（50-100字的极简内容）
        version: version,
        release_type: releaseType,
        target_audience: 'operation'  // 默认运营团队，可根据需要调整
      },
      response_mode: "blocking",
      user: "release-bot"
    };

    const response = await axios.post(
      `${CONFIG.dify.apiUrl}/workflows/run`,  // 使用 workflows/run 端点
      requestBody,
      {
        headers: {
          'Authorization': `Bearer ${CONFIG.dify.apiKey}`,
          'Content-Type': 'application/json'
        },
        timeout: 30000
      }
    );
    
    console.log(chalk.green('✓ AI 发布日志生成成功'));
    
    // Workflow API 返回的数据结构
    const result = response.data.data?.outputs?.result ||  // 正确的输出路径
                   response.data.data?.outputs?.text || 
                   response.data.data?.outputs?.answer ||
                   `版本 v${version} 已发布\n${changes.join('\n')}`;
    
    return result;
    
  } catch (error) {
    console.warn(chalk.yellow('⚠ AI 生成失败，使用默认模板'));
    
    // 详细错误信息
    if (error.response) {
      console.log(chalk.red('错误状态码:'), error.response.status);
      console.log(chalk.red('错误信息:'), JSON.stringify(error.response.data, null, 2));
    } else {
      console.log(chalk.gray(error.message));
    }
    
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
          content: `🚀 心之旅项目 v${version} 发布成功`, 
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
          content: `❌ 心之旅项目 v${version} 发布失败`, 
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