#!/usr/bin/env node

/**
 * Mindtrip Project Full Release Tool with Deploy
 * Features: Build, Deploy, Generate logs, Send notifications
 * Usage: npm run release:deploy or node script/windows/release-with-deploy.mjs
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

console.log(chalk.cyan('========================================'));
console.log(chalk.cyan('    Mindtrip Project Release Tool v1.0.0'));
console.log(chalk.cyan('========================================'));
console.log();

// 验证环境配置
if (!config.validate()) {
  process.exit(1);
}

// Check current branch must be master
git.checkBranch('master', PROJECT_ROOT);

// Pull latest code
git.pullLatest('master', PROJECT_ROOT);
console.log();

// Check if in project root
if (!fs.existsSync(path.join(PROJECT_ROOT, 'pom.xml'))) {
  console.log(chalk.red('Error: Please run this script from project root'));
  process.exit(1);
}

// Parse command line arguments
const args = process.argv.slice(2);
const isAuto = args.includes('--auto');
const versionArg = args.find(arg => arg.startsWith('--version='));

// 1. Get version number
let version;
if (versionArg) {
  version = versionArg.split('=')[1];
} else if (!isAuto) {
  version = readline.question('Enter version number (e.g. 1.2.0): v');
} else {
  // Auto get next patch version
  try {
    const packageJson = JSON.parse(
      fs.readFileSync(path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend/package.json'), 'utf-8')
    );
    const [major, minor, patch] = packageJson.version.split('.');
    version = `${major}.${minor}.${parseInt(patch) + 1}`;
  } catch (err) {
    console.log(chalk.red('Cannot get version automatically, please enter manually'));
    version = readline.question('Enter version number (e.g. 1.2.0): v');
  }
}

if (!version) {
  console.log(chalk.red('Version cannot be empty'));
  process.exit(1);
}

// Ensure correct version format
if (!version.match(/^\d+\.\d+\.\d+$/)) {
  console.log(chalk.red('Invalid version format, should be x.y.z'));
  process.exit(1);
}

// 2. Select release content
let choice = '3'; // Default all
if (!isAuto) {
  console.log('\nSelect content to release:');
  console.log('  [1] Backend only');
  console.log('  [2] Frontend only');
  console.log('  [3] Frontend + Backend (default)');
  choice = readline.question('Please select (1/2/3): ') || '3';
}

// 3. Confirm release
const releaseContent = choice === '1' ? 'Backend' : choice === '2' ? 'Frontend' : 'Frontend+Backend';
console.log('\n' + chalk.yellow('About to release:'));
console.log(`  Version: v${version}`);
console.log(`  Content: ${releaseContent}`);
console.log(`  Server: ${config.server.host}`);

if (!isAuto) {
  const confirm = readline.question('\nConfirm release? (y/N): ');
  if (confirm.toLowerCase() !== 'y') {
    console.log('Release cancelled');
    process.exit(0);
  }
}

// Main release function
async function release() {
  const startTime = Date.now();
  const changelog = [];

  try {
    // 4. Build and deploy backend
    if (choice === '1' || choice === '3') {
      console.log(chalk.blue('\n========== Backend Release =========='));

      // Build
      console.log(chalk.blue('[1/2] Building backend...'));
      try {
        process.chdir(PROJECT_ROOT);
        execSync('mvn clean package -DskipTests', {
          stdio: 'inherit',
          cwd: PROJECT_ROOT
        });
        console.log(chalk.green('✓ Backend build successful'));

        // Check JAR file
        const jarPath = path.join(PROJECT_ROOT, 'yudao-server/target/yudao-server.jar');
        if (!fs.existsSync(jarPath)) {
          throw new Error('JAR file not generated');
        }
        const jarSize = (fs.statSync(jarPath).size / 1024 / 1024).toFixed(2);
        console.log(chalk.gray(`  JAR file size: ${jarSize} MB`));
      } catch (err) {
        console.log(chalk.red('✗ Backend build failed'));
        throw err;
      }

      // Deploy
      console.log(chalk.blue('[2/2] Deploying backend...'));
      try {
        execSync('node script/windows/deploy-backend.mjs', {
          stdio: 'inherit',
          cwd: PROJECT_ROOT
        });
        console.log(chalk.green('✓ Backend deployed successfully'));
        changelog.push('- Backend service updated');
      } catch (err) {
        console.log(chalk.red('✗ Backend deployment failed'));
        throw err;
      }
    }

    // 5. Build and deploy frontend
    if (choice === '2' || choice === '3') {
      console.log(chalk.blue('\n========== Frontend Release =========='));

      // Admin project
      console.log(chalk.blue('[1/4] Building Admin dashboard...'));
      try {
        const frontendPath = path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend');
        process.chdir(frontendPath);

        // Build Admin
        execSync('pnpm build:admin', { stdio: 'inherit' });

        // Check dist.zip
        const adminDistZip = path.join(frontendPath, 'apps/admin/dist.zip');
        if (!fs.existsSync(adminDistZip)) {
          throw new Error('Admin dist.zip not generated');
        }
        console.log(chalk.green('✓ Admin build successful'));
      } catch (err) {
        console.log(chalk.red('✗ Admin build failed'));
        throw err;
      }

      // Web project
      console.log(chalk.blue('[2/4] Building Web frontend...'));
      try {
        const frontendPath = path.join(PROJECT_ROOT, 'yudao-ui/lvye-project-frontend');
        process.chdir(frontendPath);

        // Build Web
        execSync('pnpm build:web', { stdio: 'inherit' });

        // Check dist.zip
        const webDistZip = path.join(frontendPath, 'apps/web/dist.zip');
        if (!fs.existsSync(webDistZip)) {
          throw new Error('Web dist.zip not generated');
        }
        console.log(chalk.green('✓ Web build successful'));
      } catch (err) {
        console.log(chalk.red('✗ Web build failed'));
        throw err;
      }

      // Deploy frontend
      console.log(chalk.blue('[3/4] Deploying frontend...'));
      try {
        process.chdir(PROJECT_ROOT);
        execSync('node script/windows/deploy-frontend.mjs', {
          stdio: 'inherit',
          cwd: PROJECT_ROOT
        });
        console.log(chalk.green('✓ Frontend deployed successfully'));
        changelog.push('- Frontend UI optimized');
      } catch (err) {
        console.log(chalk.red('✗ Frontend deployment failed'));
        throw err;
      }
    }

    // 6. Create Git Tag
    console.log(chalk.blue('\n[4/4] Creating version tag...'));
    try {
      process.chdir(PROJECT_ROOT);

      // Check for uncommitted changes
      if (git.checkUncommittedChanges(PROJECT_ROOT)) {
        console.log(chalk.yellow('Warning: Uncommitted changes exist'));
        if (!isAuto) {
          const continueTag = readline.question('Continue creating tag? (y/N): ');
          if (continueTag.toLowerCase() !== 'y') {
            console.log('Skip creating tag');
          } else {
            git.createTag(`v${version}`, `Release v${version}`, PROJECT_ROOT);
          }
        }
      } else {
        git.createTag(`v${version}`, `Release v${version}`, PROJECT_ROOT);
      }
    } catch (err) {
      error.handle(err, { message: 'Git tag creation failed (non-critical)' });
    }

    // 7. Generate release log
    console.log(chalk.blue('\nGenerating release notes...'));
    const releaseType = git.determineReleaseType(version, PROJECT_ROOT);
    const releaseNotes = await dify.generateReleaseNotes(version, changelog, releaseType);

    // 8. Send Feishu notification
    await feishu.notifySuccess(version, releaseNotes, releaseContent, {
      content: releaseContent,
      server: config.server.host
    });

    // Record release
    recordRelease(version, releaseContent);

    // Release success
    const duration = ((Date.now() - startTime) / 1000 / 60).toFixed(1);
    console.log(chalk.green('\n========================================'));
    console.log(chalk.green('           Release Successful!'));
    console.log(chalk.green('========================================'));
    console.log(`  Version: v${version}`);
    console.log(`  Content: ${releaseContent}`);
    console.log(`  Duration: ${duration} minutes`);
    console.log(`  Access URL: http://${config.server.host}/`);
    console.log();

  } catch (err) {
    console.error(chalk.red('\n❌ Release failed:'), err.message);

    // Send failure notification
    try {
      await feishu.notifyFailure(version, err.message, releaseContent);
    } catch (notifyError) {
      console.error('Failed to send failure notification:', notifyError.message);
    }

    process.exit(1);
  }
}

// Record release history
function recordRelease(version, content) {
  const record = {
    version: `v${version}`,
    content: content,
    timestamp: new Date().toISOString(),
    deployer: process.env.USER || process.env.USERNAME || 'unknown',
    server: config.server.host
  };

  const logFile = path.join(PROJECT_ROOT, 'releases.log');

  try {
    fs.appendFileSync(logFile, JSON.stringify(record) + '\n');
  } catch (err) {
    // Silent failure
  }
}

// Execute release
release().catch(err => {
  error.handle(err, {
    critical: true,
    message: 'Unexpected error during release'
  });
});
