#!/usr/bin/env node

/**
 * Mindtrip Project Full Release Tool with Deploy
 * Features: Build, Deploy, Generate logs, Send notifications
 * Usage: npm run release:deploy or node script/release-with-deploy.mjs
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

// Configuration
const CONFIG = {
  dify: {
    apiKey: 'app-LTUF7HU291Ug9LAKD4ZC4ZHO',
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
console.log(chalk.cyan('    Mindtrip Project Release Tool v1.0.0'));
console.log(chalk.cyan('========================================'));
console.log();

// Check current branch must be master
try {
  const currentBranch = execSync('git branch --show-current', {
    encoding: 'utf-8',
    cwd: PROJECT_ROOT
  }).trim();
  
  if (currentBranch !== 'master') {
    console.log(chalk.red('Error: Release must be on master branch'));
    console.log(chalk.yellow(`   Current branch: ${currentBranch}`));
    console.log(chalk.gray('   Please switch to master: git checkout master'));
    process.exit(1);
  }
  
  console.log(chalk.green('✓ Current branch: master'));
  
  // Pull latest code
  console.log(chalk.blue('Syncing latest code...'));
  execSync('git pull origin master', {
    stdio: 'inherit',
    cwd: PROJECT_ROOT
  });
  console.log(chalk.green('✓ Code synced'));
  console.log();
  
} catch (error) {
  console.error(chalk.red('Git operation failed:'), error.message);
  process.exit(1);
}

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
  } catch (error) {
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
console.log(`  Server: ${CONFIG.server.host}`);

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
  let hasError = false;
  
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
      } catch (error) {
        console.log(chalk.red('✗ Backend build failed'));
        throw error;
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
      } catch (error) {
        console.log(chalk.red('✗ Backend deployment failed'));
        throw error;
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
      } catch (error) {
        console.log(chalk.red('✗ Admin build failed'));
        throw error;
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
      } catch (error) {
        console.log(chalk.red('✗ Web build failed'));
        throw error;
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
      } catch (error) {
        console.log(chalk.red('✗ Frontend deployment failed'));
        throw error;
      }
    }
    
    // 6. Create Git Tag
    console.log(chalk.blue('\n[4/4] Creating version tag...'));
    try {
      process.chdir(PROJECT_ROOT);
      
      // Check for uncommitted changes
      const gitStatus = execSync('git status --porcelain', { encoding: 'utf-8' });
      if (gitStatus.trim()) {
        console.log(chalk.yellow('Warning: Uncommitted changes exist'));
        if (!isAuto) {
          const continueTag = readline.question('Continue creating tag? (y/N): ');
          if (continueTag.toLowerCase() !== 'y') {
            console.log('Skip creating tag');
          } else {
            createGitTag(version);
          }
        }
      } else {
        createGitTag(version);
      }
    } catch (error) {
      console.log(chalk.yellow('⚠ Git tag creation failed (non-critical)'));
      console.log(chalk.gray(error.message));
    }
    
    // 7. Generate release log
    console.log(chalk.blue('\nGenerating release notes...'));
    // Determine release type
    const releaseType = determineReleaseType(version);
    const releaseNotes = await generateReleaseNotes(version, changelog, releaseType);
    
    // 8. Send Feishu notification
    await notifyFeishu(version, releaseNotes, releaseContent);
    
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
    console.log(`  Access URL: http://${CONFIG.server.host}/`);
    console.log();
    
  } catch (error) {
    hasError = true;
    console.error(chalk.red('\n❌ Release failed:'), error.message);
    
    // Send failure notification
    try {
      await notifyFeishuError(version, error.message);
    } catch (notifyError) {
      console.error('Failed to send failure notification:', notifyError.message);
    }
    
    process.exit(1);
  }
}

// Determine release type
function determineReleaseType(version) {
  // Based on version number
  const parts = version.split('.');
  const [major, minor, patch] = parts.map(Number);
  
  // Get previous version from git tag
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
    // Default to patch
  }
  
  // Or from command line arguments
  const args = process.argv.slice(2);
  if (args.includes('--major')) return 'major';
  if (args.includes('--minor')) return 'minor';
  if (args.includes('--hotfix')) return 'hotfix';
  
  return 'patch'; // Default
}

// Create Git tag
function createGitTag(version) {
  execSync(`git tag -a v${version} -m "Release v${version}"`, { stdio: 'inherit' });
  execSync(`git push origin v${version}`, { stdio: 'inherit' });
  console.log(chalk.green(`✓ Git tag v${version} created successfully`));
}

// Generate release notes (call Dify Workflow API)
async function generateReleaseNotes(version, changes, releaseType = 'patch') {
  try {
    // Get commit records
    let commits = '';
    try {
      // Get all version tags
      const allTags = execSync('git tag -l "v*" --sort=-version:refname', { 
        encoding: 'utf-8', 
        cwd: PROJECT_ROOT 
      }).trim().split('\n').filter(tag => tag);
      
      const currentTag = `v${version}`;
      
      if (allTags.length > 0 && !allTags.includes(currentTag)) {
        // Preparing new version, get commits from latest tag to HEAD
        const latestTag = allTags[0];
        console.log(chalk.blue(`Getting commits from ${latestTag} to HEAD`));
        commits = execSync(`git log ${latestTag}..HEAD --oneline`, { 
          encoding: 'utf-8', 
          cwd: PROJECT_ROOT 
        });
      } else if (allTags.length >= 2) {
        // Get commits between two latest tags
        console.log(chalk.blue(`Getting commits from ${allTags[1]} to ${allTags[0]}`));
        commits = execSync(`git log ${allTags[1]}..${allTags[0]} --oneline`, { 
          encoding: 'utf-8', 
          cwd: PROJECT_ROOT 
        });
      } else {
        // First release or only one tag
        console.log(chalk.blue('Getting recent 15 commits'));
        commits = execSync('git log --oneline -15', { 
          encoding: 'utf-8', 
          cwd: PROJECT_ROOT 
        });
      }
    } catch (error) {
      // If error, get recent 10 commits
      try {
        commits = execSync('git log --oneline -10', { 
          encoding: 'utf-8', 
          cwd: PROJECT_ROOT 
        });
      } catch (e) {
        commits = 'Unable to get commit records';
      }
    }
    
    // Clean and format commits
    const commitLines = commits.split('\n')
      .filter(line => line.trim())
      .map(line => {
        // Remove commit hash
        return line.replace(/^[a-f0-9]{7,}\s+/, '');
      })
      .slice(0, 8); // Max 8 to avoid too long
    
    // Build concise query
    const prompt = commitLines.join('; ').substring(0, 150); // Limit to 150 chars

    // Debug: Print content to send
    console.log(chalk.blue('\n[Debug] Preparing content for Dify:'));
    console.log(chalk.gray('API URL:'), CONFIG.dify.apiUrl + '/workflows/run');
    console.log(chalk.gray('Git Commits:'));
    commitLines.forEach(commit => console.log(chalk.gray('  - ' + commit)));
    console.log(chalk.gray('Prompt length:'), prompt.length, 'characters');
    console.log(chalk.gray('Release type:'), releaseType);

    // Use Workflow API
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
    
    console.log(chalk.green('✓ AI release notes generated successfully'));
    
    // Workflow API response structure
    const result = response.data.data?.outputs?.result ||
                   response.data.data?.outputs?.text || 
                   response.data.data?.outputs?.answer ||
                   `Version v${version} released\n${changes.join('\n')}`;
    
    return result;
    
  } catch (error) {
    console.warn(chalk.yellow('⚠ AI generation failed, using default template'));
    
    // Detailed error info
    if (error.response) {
      console.log(chalk.red('Error status:'), error.response.status);
      console.log(chalk.red('Error message:'), JSON.stringify(error.response.data, null, 2));
    } else {
      console.log(chalk.gray(error.message));
    }
    
    // Use default template
    return `📦 **Version v${version} Updates**

${changes.join('\n')}

Thank you for your support! Please report any issues.`;
  }
}

// Send Feishu notification
async function notifyFeishu(version, notes, content) {
  const message = {
    msg_type: "interactive",
    card: {
      config: { wide_screen_mode: true },
      header: {
        title: { 
          content: `🚀 Mindtrip Project v${version} Released Successfully`, 
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
                content: `**Content:** ${content}`,
                tag: "lark_md"
              }
            },
            {
              is_short: true,
              text: {
                content: `**Server:** ${CONFIG.server.host}`,
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
                content: "Visit System", 
                tag: "plain_text" 
              },
              type: "primary",
              url: `http://${CONFIG.server.host}/`
            },
            {
              tag: "button",
              text: { 
                content: "Admin Dashboard", 
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
              content: `Release time: ${new Date().toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })}`
            }
          ]
        }
      ]
    }
  };

  try {
    await axios.post(CONFIG.feishu.webhook, message, { timeout: 10000 });
    console.log(chalk.green('✓ Feishu notification sent successfully'));
  } catch (error) {
    console.warn(chalk.yellow('⚠ Feishu notification failed:'), error.message);
  }
}

// Send failure notification
async function notifyFeishuError(version, errorMessage) {
  const message = {
    msg_type: "interactive",
    card: {
      config: { wide_screen_mode: true },
      header: {
        title: { 
          content: `❌ Mindtrip Project v${version} Release Failed`, 
          tag: "plain_text" 
        },
        template: "red"
      },
      elements: [
        {
          tag: "markdown",
          content: `**Error:**\n${errorMessage}\n\nPlease check and retry.`
        },
        {
          tag: "note",
          elements: [
            {
              tag: "plain_text",
              content: `Time: ${new Date().toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })}`
            }
          ]
        }
      ]
    }
  };

  try {
    await axios.post(CONFIG.feishu.webhook, message, { timeout: 10000 });
  } catch (error) {
    // Silent failure
  }
}

// Record release history
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
    // Silent failure
  }
}

// Execute release
release().catch(error => {
  console.error(chalk.red('Unexpected error during release:'), error);
  process.exit(1);
});