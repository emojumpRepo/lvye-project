#!/usr/bin/env node

/**
 * 心之旅项目独立版本发布工具
 * 支持前后端独立版本管理
 * 用法：
 *   npm run release              - 交互式选择
 *   npm run release:frontend     - 直接发布前端
 *   npm run release:backend      - 直接发布后端
 */

import { execSync } from 'child_process';
import chalk from 'chalk';
import readline from 'readline-sync';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { config, git, dify, feishu, error } from '../lib/release-utils.mjs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const PROJECT_ROOT = path.join(__dirname, '../..');

// 获取项目版本
function getProjectVersion(projectType) {
  try {
    if (projectType === 'backend') {
      const versionFile = fs.readFileSync(path.join(PROJECT_ROOT, 'version.properties'), 'utf-8');
      const match = versionFile.match(/project\.version=(.+)/);
      return match ? match[1] : '0.0.1';
    } else if (projectType === 'frontend') {
      const versionFile = JSON.parse(
        fs.readFileSync(path.join(PROJECT_ROOT, 'mindtrip-ui/lvye-project-frontend/version.json'), 'utf-8')
      );
      return versionFile.version;
    }
  } catch (err) {
    return error.handle(err, {
      message: '无法读取版本文件',
      fallback: '0.0.1'
    });
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
    const filePath = path.join(PROJECT_ROOT, 'mindtrip-ui/lvye-project-frontend/version.json');
    const versionFile = JSON.parse(fs.readFileSync(filePath, 'utf-8'));
    versionFile.version = newVersion;
    versionFile.buildTime = new Date().toLocaleString('zh-CN', {
      timeZone: 'Asia/Shanghai',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }).replace(/\//g, '-');
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

    let workDir = PROJECT_ROOT;
    if (projectType === 'frontend') {
      workDir = path.join(PROJECT_ROOT, 'mindtrip-ui/lvye-project-frontend');
      if (!fs.existsSync(path.join(workDir, '.git'))) {
        console.log(chalk.yellow('前端项目没有独立的 Git 仓库，使用主仓库的提交记录'));
        workDir = PROJECT_ROOT;
      }
    }

    const currentTag = `${tagPrefix}${currentVersion}`;
    const allTags = execSync(`git tag -l "${tagPrefix}*" --sort=-version:refname`, {
      encoding: 'utf-8',
      cwd: workDir
    }).trim().split('\n').filter(tag => tag);

    let commits = '';
    const currentTagIndex = allTags.indexOf(currentTag);

    if (currentTagIndex >= 0 && currentTagIndex < allTags.length - 1) {
      const previousTag = allTags[currentTagIndex + 1];
      console.log(chalk.blue(`获取 ${previousTag} 到 ${currentTag} 之间的提交`));
      commits = execSync(`git log ${previousTag}..${currentTag} --oneline`, {
        encoding: 'utf-8',
        cwd: workDir
      });
    } else if (allTags.length > 0 && !allTags.includes(currentTag)) {
      const latestTag = allTags[0];
      console.log(chalk.blue(`获取 ${latestTag} 到 HEAD 之间的提交（准备发布 v${currentVersion}）`));
      commits = execSync(`git log ${latestTag}..HEAD --oneline`, {
        encoding: 'utf-8',
        cwd: workDir
      });
    } else if (allTags.length === 0) {
      console.log(chalk.blue('首次发布，获取最近 15 条提交'));
      if (projectType === 'frontend' && workDir === PROJECT_ROOT) {
        commits = execSync('git log --oneline -15 -- mindtrip-ui/lvye-project-frontend/', {
          encoding: 'utf-8',
          cwd: PROJECT_ROOT
        });
      } else {
        commits = execSync('git log --oneline -15', {
          encoding: 'utf-8',
          cwd: workDir
        });
      }
    } else {
      console.log(chalk.blue('获取最近 10 条提交'));
      if (projectType === 'frontend' && workDir === PROJECT_ROOT) {
        commits = execSync('git log --oneline -10 -- mindtrip-ui/lvye-project-frontend/', {
          encoding: 'utf-8',
          cwd: PROJECT_ROOT
        });
      } else {
        commits = execSync('git log --oneline -10', {
          encoding: 'utf-8',
          cwd: workDir
        });
      }
    }

    if (!commits.trim()) {
      return ['无新的提交'];
    }

    return commits.split('\n')
      .filter(line => line.trim())
      .map(line => {
        const message = line.replace(/^[a-f0-9]{7,}\s+/, '');
        return message.replace(/[^\x20-\x7E\u4e00-\u9fa5]/g, '');
      })
      .slice(0, 10);
  } catch (err) {
    return error.handle(err, {
      message: '获取提交记录失败',
      fallback: ['无法获取提交记录']
    });
  }
}

// 主流程
async function main() {
  console.log(chalk.cyan('========================================'));
  console.log(chalk.cyan('    心之旅项目独立版本发布工具 v2.0'));
  console.log(chalk.cyan('========================================'));
  console.log();

  // 验证环境配置
  if (!config.validate()) {
    process.exit(1);
  }

  // 检查 Git 状态
  git.checkBranch('master', PROJECT_ROOT);

  // 检查未提交的更改
  if (git.checkUncommittedChanges(PROJECT_ROOT)) {
    console.log(chalk.yellow('⚠ 警告：存在未提交的更改'));
    console.log(chalk.gray('  建议先提交或暂存更改'));
    const continueAnyway = readline.question('Continue anyway? (y/N): ');
    if (continueAnyway.toLowerCase() !== 'y') {
      console.log('已取消发布');
      process.exit(0);
    }
  }

  // 拉取最新代码
  git.pullLatest('master', PROJECT_ROOT);
  console.log();

  // 检查命令行参数 - 支持直接指定项目类型
  const args = process.argv.slice(2);
  let projectType;

  if (args.includes('frontend')) {
    projectType = 'frontend';
    console.log(chalk.blue('📦 快速发布模式：前端'));
  } else if (args.includes('backend')) {
    projectType = 'backend';
    console.log(chalk.blue('📦 快速发布模式：后端'));
  } else {
    // 交互式选择发布类型
    console.log('请选择发布项目:');
    console.log('  [1] 后端 (backend)');
    console.log('  [2] 前端 (frontend)');
    console.log('  [3] 前后端 (all)');
    const projectChoice = readline.question('Please select (1/2/3): ') || '3';
    projectType = projectChoice === '1' ? 'backend' :
                        projectChoice === '2' ? 'frontend' : 'all';
  }

  // 获取当前版本
  const currentVersion = projectType === 'all' ?
    getProjectVersion('backend') :
    getProjectVersion(projectType);

  console.log(`\n当前版本: v${currentVersion}`);

  // 选择发布类型
  console.log('\n请选择版本类型:');
  console.log('  [1] Patch (修复) - ' + getNextVersion(currentVersion, 'patch'));
  console.log('  [2] Minor (功能) - ' + getNextVersion(currentVersion, 'minor'));
  console.log('  [3] Major (重大) - ' + getNextVersion(currentVersion, 'major'));
  const releaseChoice = readline.question('Please select (1/2/3): ') || '1';
  const releaseType = releaseChoice === '3' ? 'major' :
                     releaseChoice === '2' ? 'minor' : 'patch';

  const newVersion = getNextVersion(currentVersion, releaseType);

  // 确认发布
  console.log('\n' + chalk.yellow('即将发布:'));
  console.log(`  项目: ${projectType}`);
  console.log(`  版本: v${currentVersion} → v${newVersion}`);

  const confirm = readline.question('\nConfirm release? (y/N): ');
  if (confirm.toLowerCase() !== 'y') {
    console.log('已取消发布');
    process.exit(0);
  }

  try {
    // 更新版本文件
    updateProjectVersion(projectType, newVersion);

    // 获取提交记录
    const commitMessages = getGitCommits(projectType, currentVersion);
    console.log(chalk.blue('\n相关提交:'));
    commitMessages.forEach((msg, i) => console.log(chalk.gray(`  ${i+1}. ${msg}`)));

    // 创建Git标签
    const tagPrefix = projectType === 'backend' ? 'mindtrip-backend-v' :
                     projectType === 'frontend' ? 'mindtrip-frontend-v' : 'mindtrip-v';
    const tagName = `${tagPrefix}${newVersion}`;

    // 提交版本文件的更改
    try {
      execSync(`git add -A`, { cwd: PROJECT_ROOT });

      const status = execSync('git status --porcelain', {
        encoding: 'utf-8',
        cwd: PROJECT_ROOT
      });

      if (status.trim()) {
        execSync(`git commit -m "chore: release ${projectType} v${newVersion}"`, {
          cwd: PROJECT_ROOT,
          encoding: 'utf-8'
        });
        console.log(chalk.green('✓ 版本文件已提交'));
      } else {
        console.log(chalk.yellow('⚠ 没有文件变化需要提交'));
      }
    } catch (err) {
      error.handle(err, { message: 'Git 提交失败（可能没有变化）' });
    }

    // 创建并推送标签
    try {
      let tagCwd = PROJECT_ROOT;
      if (projectType === 'frontend') {
        const frontendDir = path.join(PROJECT_ROOT, 'mindtrip-ui/lvye-project-frontend');
        if (fs.existsSync(path.join(frontendDir, '.git'))) {
          tagCwd = frontendDir;
        }
      }

      git.createTag(tagName, `Release ${projectType} v${newVersion}`, tagCwd);
    } catch (err) {
      error.handle(err, { message: '创建标签失败' });
      console.log(chalk.yellow('继续执行后续步骤...'));
    }

    // 生成发布日志
    const releaseNotes = await dify.generateReleaseNotes(
      newVersion,
      commitMessages,
      releaseType,
      projectType
    );

    // 发送通知
    await feishu.notifySuccess(newVersion, releaseNotes, projectType);

    console.log(chalk.green('\n========================================'));
    console.log(chalk.green('           发布成功！'));
    console.log(chalk.green('========================================'));
    console.log(`  项目: ${projectType}`);
    console.log(`  版本: v${newVersion}`);
    console.log(`  标签: ${tagName}`);

  } catch (err) {
    error.handle(err, {
      critical: true,
      message: '发布失败'
    });
  }
}

// 执行
main().catch(err => {
  error.handle(err, {
    critical: true,
    message: '发生未预期的错误'
  });
});
