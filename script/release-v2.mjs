#!/usr/bin/env node

/**
 * 心之旅项目独立版本发布工具
 * 支持前后端独立版本管理
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
  }
};

// 获取项目版本
function getProjectVersion(projectType) {
  try {
    if (projectType === 'backend') {
      const versionFile = fs.readFileSync(path.join(PROJECT_ROOT, 'version.properties'), 'utf-8');
      const match = versionFile.match(/project\.version=(.+)/);
      return match ? match[1] : '0.0.1';
    } else if (projectType === 'frontend') {
      const versionFile = JSON.parse(
        fs.readFileSync(path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend/version.json'), 'utf-8')
      );
      return versionFile.version;
    }
  } catch (error) {
    return '0.0.1';
  }
}

// 更新版本号
function updateProjectVersion(projectType, newVersion) {
  if (projectType === 'backend' || projectType === 'all') {
    const filePath = path.join(PROJECT_ROOT, 'version.properties');
    let content = fs.readFileSync(filePath, 'utf-8');
    content = content.replace(/project\.version=.+/, `project.version=${newVersion}`);
    content = content.replace(/project\.build\.time=.+/, `project.build.time=${new Date().toISOString().split('T')[0]}`);
    fs.writeFileSync(filePath, content);
    console.log(chalk.green(`✓ 后端版本更新为 ${newVersion}`));
  }
  
  if (projectType === 'frontend' || projectType === 'all') {
    const filePath = path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend/version.json');
    const versionFile = JSON.parse(fs.readFileSync(filePath, 'utf-8'));
    versionFile.version = newVersion;
    versionFile.buildTime = new Date().toISOString().split('T')[0];
    fs.writeFileSync(filePath, JSON.stringify(versionFile, null, 2));
    console.log(chalk.green(`✓ 前端版本更新为 ${newVersion}`));
  }
}

// 计算下一个版本号
function getNextVersion(currentVersion, releaseType) {
  const [major, minor, patch] = currentVersion.split('.').map(Number);
  
  switch(releaseType) {
    case 'major':
      return `${major + 1}.0.0`;
    case 'minor':
      return `${major}.${minor + 1}.0`;
    case 'patch':
    default:
      return `${major}.${minor}.${patch + 1}`;
  }
}

// 获取Git提交记录
function getGitCommits(projectType, currentVersion) {
  try {
    const tagPrefix = projectType === 'backend' ? 'mindtrip-backend-v' : 
                      projectType === 'frontend' ? 'mindtrip-frontend-v' : 'mindtrip-v';
    
    // 获取当前项目的所有 tags（按版本排序）
    const currentTag = `${tagPrefix}${currentVersion}`;
    const allTags = execSync(`git tag -l "${tagPrefix}*" --sort=-version:refname`, {
      encoding: 'utf-8',
      cwd: PROJECT_ROOT
    }).trim().split('\n').filter(tag => tag);
    
    let commits = '';
    
    // 查找当前版本的 tag 位置
    const currentTagIndex = allTags.indexOf(currentTag);
    
    if (currentTagIndex >= 0 && currentTagIndex < allTags.length - 1) {
      // 如果找到当前版本的 tag，获取它和上一个 tag 之间的提交
      const previousTag = allTags[currentTagIndex + 1];
      console.log(chalk.blue(`获取 ${previousTag} 到 ${currentTag} 之间的提交`));
      commits = execSync(`git log ${previousTag}..${currentTag} --oneline`, {
        encoding: 'utf-8',
        cwd: PROJECT_ROOT
      });
    } else if (allTags.length > 0 && !allTags.includes(currentTag)) {
      // 如果当前版本还没有 tag（新版本），获取最新 tag 到 HEAD 的提交
      const latestTag = allTags[0];
      console.log(chalk.blue(`获取 ${latestTag} 到 HEAD 之间的提交（准备发布 v${currentVersion}）`));
      commits = execSync(`git log ${latestTag}..HEAD --oneline`, {
        encoding: 'utf-8',
        cwd: PROJECT_ROOT
      });
    } else if (allTags.length === 0) {
      // 如果没有任何 tag，获取最近的提交
      console.log(chalk.blue('首次发布，获取最近 15 条提交'));
      commits = execSync('git log --oneline -15', {
        encoding: 'utf-8',
        cwd: PROJECT_ROOT
      });
    } else {
      // 其他情况，获取最近的提交
      console.log(chalk.blue('获取最近 10 条提交'));
      commits = execSync('git log --oneline -10', {
        encoding: 'utf-8',
        cwd: PROJECT_ROOT
      });
    }
    
    // 如果没有提交，返回提示信息
    if (!commits.trim()) {
      return ['无新的提交'];
    }
    
    return commits.split('\n')
      .filter(line => line.trim())
      .map(line => line.replace(/^[a-f0-9]{7,}\s+/, ''))
      .slice(0, 10); // 最多返回10条
  } catch (error) {
    console.warn(chalk.yellow('获取提交记录失败:'), error.message);
    return ['无法获取提交记录'];
  }
}

// 生成发布日志
async function generateReleaseNotes(version, projectType, commitMessages, releaseType) {
  try {
    const prompt = commitMessages.join('；').substring(0, 150);
    
    const requestBody = {
      inputs: {
        query: prompt,
        version: version,
        release_type: releaseType,
        target_audience: 'operation'
      },
      response_mode: "blocking",
      user: "release-bot"
    };
    
    const response = await axios.post(
      `${CONFIG.dify.apiUrl}/workflows/run`,
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
    return response.data.data?.outputs?.result || `${projectType} v${version} 已发布`;
    
  } catch (error) {
    console.warn(chalk.yellow('⚠ AI 生成失败，使用默认模板'));
    return `📦 **${projectType} v${version} 更新**\n\n${commitMessages.slice(0, 3).join('\n- ')}\n\n感谢您的使用！`;
  }
}

// 发送飞书通知
async function notifyFeishu(version, projectType, notes) {
  const projectName = projectType === 'backend' ? '心之旅后端' : 
                     projectType === 'frontend' ? '心之旅前端' : '心之旅平台';
  
  const message = {
    msg_type: "interactive",
    card: {
      config: { wide_screen_mode: true },
      header: {
        title: { 
          content: `🚀 ${projectName} v${version} 发布成功`, 
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
    console.warn(chalk.yellow('⚠ 飞书通知发送失败'));
  }
}

// 主流程
async function main() {
  console.log(chalk.cyan('========================================'));
  console.log(chalk.cyan('    心之旅项目独立版本发布工具 v2.0'));
  console.log(chalk.cyan('========================================'));
  console.log();
  
  // 检查当前分支
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
    
    // 检查是否有未提交的更改
    const gitStatus = execSync('git status --porcelain', { 
      encoding: 'utf-8',
      cwd: PROJECT_ROOT 
    });
    
    if (gitStatus.trim()) {
      console.log(chalk.yellow('⚠ 警告：存在未提交的更改'));
      console.log(chalk.gray('  建议先提交或暂存更改'));
      const continueAnyway = readline.question('是否继续? (y/N): ');
      if (continueAnyway.toLowerCase() !== 'y') {
        console.log('已取消发布');
        process.exit(0);
      }
    }
    
    // 拉取最新代码
    console.log(chalk.blue('正在拉取最新代码...'));
    execSync('git pull origin master', {
      stdio: 'inherit',
      cwd: PROJECT_ROOT
    });
    console.log(chalk.green('✓ 代码已更新到最新'));
    
  } catch (error) {
    console.error(chalk.red('Git 操作失败:'), error.message);
    process.exit(1);
  }
  
  console.log();
  
  // 选择发布类型
  console.log('请选择发布项目:');
  console.log('  [1] 后端 (backend)');
  console.log('  [2] 前端 (frontend)');  
  console.log('  [3] 前后端 (all)');
  const projectChoice = readline.question('请选择 (1/2/3): ') || '3';
  const projectType = projectChoice === '1' ? 'backend' : 
                      projectChoice === '2' ? 'frontend' : 'all';
  
  // 获取当前版本
  const currentVersion = projectType === 'all' ? 
    getProjectVersion('backend') : // 使用后端版本作为主版本
    getProjectVersion(projectType);
  
  console.log(`\n当前版本: v${currentVersion}`);
  
  // 选择发布类型
  console.log('\n请选择版本类型:');
  console.log('  [1] Patch (修复) - ' + getNextVersion(currentVersion, 'patch'));
  console.log('  [2] Minor (功能) - ' + getNextVersion(currentVersion, 'minor'));
  console.log('  [3] Major (重大) - ' + getNextVersion(currentVersion, 'major'));
  const releaseChoice = readline.question('请选择 (1/2/3): ') || '1';
  const releaseType = releaseChoice === '3' ? 'major' :
                     releaseChoice === '2' ? 'minor' : 'patch';
  
  const newVersion = getNextVersion(currentVersion, releaseType);
  
  // 确认发布
  console.log('\n' + chalk.yellow('即将发布:'));
  console.log(`  项目: ${projectType}`);
  console.log(`  版本: v${currentVersion} → v${newVersion}`);
  
  const confirm = readline.question('\n确认发布? (y/N): ');
  if (confirm.toLowerCase() !== 'y') {
    console.log('已取消发布');
    process.exit(0);
  }
  
  try {
    // 更新版本文件
    updateProjectVersion(projectType, newVersion);
    
    // 获取提交记录（传递当前版本用于查找对应的 tag）
    const commitMessages = getGitCommits(projectType, currentVersion);
    console.log(chalk.blue('\n相关提交:'));
    commitMessages.forEach((msg, i) => console.log(chalk.gray(`  ${i+1}. ${msg}`)));
    
    // 创建Git标签
    const tagPrefix = projectType === 'backend' ? 'mindtrip-backend-v' :
                     projectType === 'frontend' ? 'mindtrip-frontend-v' : 'mindtrip-v';
    const tagName = `${tagPrefix}${newVersion}`;
    
    execSync(`git add -A`, { cwd: PROJECT_ROOT });
    execSync(`git commit -m "chore: release ${projectType} v${newVersion}"`, { cwd: PROJECT_ROOT });
    execSync(`git tag -a ${tagName} -m "Release ${projectType} v${newVersion}"`, { cwd: PROJECT_ROOT });
    execSync(`git push origin ${tagName}`, { cwd: PROJECT_ROOT });
    console.log(chalk.green(`✓ Git标签 ${tagName} 创建成功`));
    
    // 生成发布日志
    const releaseNotes = await generateReleaseNotes(newVersion, projectType, commitMessages, releaseType);
    
    // 发送通知
    await notifyFeishu(newVersion, projectType, releaseNotes);
    
    console.log(chalk.green('\n========================================'));
    console.log(chalk.green('           发布成功！'));
    console.log(chalk.green('========================================'));
    console.log(`  项目: ${projectType}`);
    console.log(`  版本: v${newVersion}`);
    console.log(`  标签: ${tagName}`);
    
  } catch (error) {
    console.error(chalk.red('发布失败:'), error.message);
    process.exit(1);
  }
}

// 执行
main().catch(error => {
  console.error(chalk.red('错误:'), error);
  process.exit(1);
});