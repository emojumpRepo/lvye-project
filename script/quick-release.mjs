#!/usr/bin/env node

/**
 * 快速发布脚本
 * 自动计算版本号并执行发布
 * 使用：npm run release:patch/minor/major
 */

import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import chalk from 'chalk';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const PROJECT_ROOT = path.join(__dirname, '..');

// 获取命令行参数
const args = process.argv.slice(2);
const releaseType = args.find(arg => ['--patch', '--minor', '--major'].includes(arg))?.substring(2) || 'patch';

console.log(chalk.cyan('========================================'));
console.log(chalk.cyan('        快速发布工具'));
console.log(chalk.cyan('========================================'));
console.log();

try {
  // 获取当前版本
  const packageJsonPath = path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend/package.json');
  
  if (!fs.existsSync(packageJsonPath)) {
    console.log(chalk.red('错误：找不到 package.json 文件'));
    process.exit(1);
  }
  
  const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf-8'));
  const currentVersion = packageJson.version;
  
  if (!currentVersion) {
    console.log(chalk.red('错误：package.json 中没有版本号'));
    process.exit(1);
  }
  
  // 解析版本号
  const versionParts = currentVersion.split('.');
  if (versionParts.length !== 3) {
    console.log(chalk.red('错误：版本号格式不正确，应为 x.y.z'));
    process.exit(1);
  }
  
  let [major, minor, patch] = versionParts.map(v => parseInt(v));
  
  // 根据类型增加版本号
  switch (releaseType) {
    case 'major':
      major++;
      minor = 0;
      patch = 0;
      break;
    case 'minor':
      minor++;
      patch = 0;
      break;
    case 'patch':
    default:
      patch++;
      break;
  }
  
  const newVersion = `${major}.${minor}.${patch}`;
  
  console.log(chalk.blue('版本更新:'));
  console.log(`  当前版本: v${currentVersion}`);
  console.log(`  新版本:   v${newVersion} (${releaseType})`);
  console.log();
  
  // 更新 package.json 版本号
  console.log(chalk.blue('更新版本号...'));
  packageJson.version = newVersion;
  fs.writeFileSync(packageJsonPath, JSON.stringify(packageJson, null, 2) + '\n');
  console.log(chalk.green(`✓ package.json 已更新到 v${newVersion}`));
  
  // 同时更新 Maven 版本（如果需要）
  try {
    const pomPath = path.join(PROJECT_ROOT, 'pom.xml');
    if (fs.existsSync(pomPath)) {
      console.log(chalk.blue('更新 Maven 版本...'));
      
      // 读取 pom.xml
      let pomContent = fs.readFileSync(pomPath, 'utf-8');
      
      // 使用正则表达式更新 revision
      // 转换版本格式：1.2.3 -> 2025.01 (使用当前年月)
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const mavenVersion = `${year}.${month}`;
      
      pomContent = pomContent.replace(
        /<revision>.*?<\/revision>/,
        `<revision>${mavenVersion}-SNAPSHOT</revision>`
      );
      
      fs.writeFileSync(pomPath, pomContent);
      console.log(chalk.green(`✓ pom.xml 已更新到 ${mavenVersion}-SNAPSHOT`));
    }
  } catch (error) {
    console.log(chalk.yellow('⚠ 更新 Maven 版本失败（非关键错误）'));
  }
  
  // 提交版本更新
  console.log(chalk.blue('\n提交版本更新...'));
  try {
    execSync('git add -A', { cwd: PROJECT_ROOT });
    execSync(`git commit -m "chore: bump version to v${newVersion}"`, { cwd: PROJECT_ROOT });
    console.log(chalk.green('✓ 版本更新已提交'));
  } catch (error) {
    if (error.message.includes('nothing to commit')) {
      console.log(chalk.gray('  没有需要提交的更改'));
    } else {
      console.log(chalk.yellow('⚠ Git 提交失败（非关键错误）'));
      console.log(chalk.gray(error.message));
    }
  }
  
  // 执行发布
  console.log(chalk.blue('\n开始自动发布...'));
  console.log(chalk.gray('=' . repeat(40)));
  
  execSync(`node ${path.join(__dirname, 'release.mjs')} --version=${newVersion} --auto`, {
    stdio: 'inherit',
    cwd: PROJECT_ROOT
  });
  
} catch (error) {
  console.error(chalk.red('\n❌ 快速发布失败:'), error.message);
  
  // 如果是版本号更新后失败，尝试回滚
  try {
    execSync('git checkout -- yudao-ui/lvye-project-frontend/package.json', { cwd: PROJECT_ROOT });
    execSync('git checkout -- pom.xml', { cwd: PROJECT_ROOT });
    console.log(chalk.yellow('已回滚版本号更改'));
  } catch (rollbackError) {
    // 静默失败
  }
  
  process.exit(1);
}