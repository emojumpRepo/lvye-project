#!/usr/bin/env node

/**
 * Release Utilities - 发布工具共享库
 * 提供发布流程中的公共功能
 */

import { execSync } from 'child_process';
import chalk from 'chalk';
import axios from 'axios';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 加载环境变量
dotenv.config({ path: path.join(__dirname, '../../.env') });

/**
 * 配置管理类
 */
export class Config {
  static get dify() {
    return {
      apiKey: process.env.DIFY_API_KEY,
      apiUrl: process.env.DIFY_API_URL || 'http://154.9.255.162/v1'
    };
  }

  static get feishu() {
    return {
      webhook: process.env.FEISHU_WEBHOOK
    };
  }

  static get server() {
    return {
      host: process.env.SERVER_HOST || '42.194.163.176',
      backendPath: process.env.SERVER_BACKEND_PATH || '/root/mindfront/work/project/mindtrip_server',
      frontendAdminPath: process.env.SERVER_FRONTEND_ADMIN_PATH || '/root/mindfront/work/project/mindtrip_apps/admin',
      frontendWebPath: process.env.SERVER_FRONTEND_WEB_PATH || '/root/mindfront/work/project/mindtrip_apps/web'
    };
  }

  static validate() {
    const required = [
      { key: 'DIFY_API_KEY', value: this.dify.apiKey },
      { key: 'FEISHU_WEBHOOK', value: this.feishu.webhook }
    ];

    const missing = required.filter(({ value }) => !value);

    if (missing.length > 0) {
      console.log(chalk.red('❌ Missing required environment variables:'));
      missing.forEach(({ key }) => console.log(chalk.yellow(`   - ${key}`)));
      console.log(chalk.gray('\nPlease configure in .env file'));
      console.log(chalk.gray('See .env.example for reference'));
      return false;
    }

    return true;
  }
}

/**
 * 错误处理类
 */
export class ErrorHandler {
  /**
   * 处理错误
   * @param {Error} error - 错误对象
   * @param {Object} options - 选项
   * @param {boolean} options.critical - 是否为关键错误（退出进程）
   * @param {string} options.message - 自定义错误消息
   * @param {boolean} options.showStack - 是否显示堆栈
   * @param {any} options.fallback - 失败时的返回值
   */
  static handle(error, options = {}) {
    const {
      critical = false,
      message = error.message,
      showStack = false,
      fallback = null
    } = options;

    if (critical) {
      console.error(chalk.red('✗ 致命错误:'), message);
    } else {
      console.warn(chalk.yellow('⚠ 警告:'), message);
    }

    if (showStack && error.stack) {
      console.log(chalk.gray(error.stack));
    }

    if (critical) {
      process.exit(1);
    }

    return fallback;
  }
}

/**
 * Git 工具类
 */
export class GitUtils {
  /**
   * 检查当前分支
   * @param {string} requiredBranch - 要求的分支名
   * @param {string} cwd - 工作目录
   */
  static checkBranch(requiredBranch = 'master', cwd = process.cwd()) {
    try {
      const currentBranch = execSync('git branch --show-current', {
        encoding: 'utf-8',
        cwd
      }).trim();

      if (currentBranch !== requiredBranch) {
        console.log(chalk.red(`❌ 错误：发布必须在 ${requiredBranch} 分支进行`));
        console.log(chalk.yellow(`   当前分支：${currentBranch}`));
        console.log(chalk.gray(`   请先切换到 ${requiredBranch} 分支：git checkout ${requiredBranch}`));
        process.exit(1);
      }

      console.log(chalk.green(`✓ 当前分支：${requiredBranch}`));
      return true;
    } catch (error) {
      return ErrorHandler.handle(error, {
        critical: true,
        message: 'Git 操作失败'
      });
    }
  }

  /**
   * 检查是否有未提交的更改
   * @param {string} cwd - 工作目录
   */
  static checkUncommittedChanges(cwd = process.cwd()) {
    try {
      const gitStatus = execSync('git status --porcelain', {
        encoding: 'utf-8',
        cwd
      });

      return gitStatus.trim() !== '';
    } catch (error) {
      return ErrorHandler.handle(error, {
        message: '无法检查 Git 状态',
        fallback: false
      });
    }
  }

  /**
   * 拉取最新代码
   * @param {string} branch - 分支名
   * @param {string} cwd - 工作目录
   */
  static pullLatest(branch = 'master', cwd = process.cwd()) {
    try {
      console.log(chalk.blue('正在拉取最新代码...'));
      execSync(`git pull origin ${branch}`, {
        stdio: 'inherit',
        cwd
      });
      console.log(chalk.green('✓ 代码已更新到最新'));
      return true;
    } catch (error) {
      return ErrorHandler.handle(error, {
        critical: true,
        message: 'Git pull 失败'
      });
    }
  }

  /**
   * 创建并推送 Git 标签
   * @param {string} tagName - 标签名
   * @param {string} message - 标签消息
   * @param {string} cwd - 工作目录
   */
  static createTag(tagName, message, cwd = process.cwd()) {
    try {
      execSync(`git tag -a ${tagName} -m "${message}"`, { cwd, stdio: 'inherit' });
      execSync(`git push origin ${tagName}`, { cwd, stdio: 'inherit' });
      console.log(chalk.green(`✓ Git标签 ${tagName} 创建成功`));
      return true;
    } catch (error) {
      return ErrorHandler.handle(error, {
        message: '创建标签失败（非关键）',
        fallback: false
      });
    }
  }

  /**
   * 确定发布类型
   * @param {string} version - 版本号
   * @param {string} cwd - 工作目录
   */
  static determineReleaseType(version, cwd = process.cwd()) {
    // 从命令行参数判断
    const args = process.argv.slice(2);
    if (args.includes('--major')) return 'major';
    if (args.includes('--minor')) return 'minor';
    if (args.includes('--hotfix')) return 'hotfix';

    // 从版本号变化判断
    try {
      const tags = execSync('git tag -l "v*" --sort=-version:refname | head -2', {
        encoding: 'utf-8',
        cwd
      }).trim().split('\n');

      if (tags.length > 1) {
        const currentVersion = version;
        const prevVersion = tags[1].replace('v', '');

        const [major, minor, patch] = currentVersion.split('.').map(Number);
        const [prevMajor, prevMinor, prevPatch] = prevVersion.split('.').map(Number);

        if (major > prevMajor) return 'major';
        if (minor > prevMinor) return 'minor';
        if (patch > prevPatch) return 'patch';
      }
    } catch (error) {
      // 默认为 patch
    }

    return 'patch';
  }
}

/**
 * Dify AI 工具类
 */
export class DifyUtils {
  /**
   * 生成发布日志
   * @param {string} version - 版本号
   * @param {Array<string>} commitMessages - 提交信息列表
   * @param {string} releaseType - 发布类型
   * @param {string} projectType - 项目类型
   */
  static async generateReleaseNotes(version, commitMessages, releaseType = 'patch', projectType = '') {
    try {
      const config = Config.dify;

      if (!config.apiKey) {
        throw new Error('Dify API Key 未配置');
      }

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
        `${config.apiUrl}/workflows/run`,
        requestBody,
        {
          headers: {
            'Authorization': `Bearer ${config.apiKey}`,
            'Content-Type': 'application/json'
          },
          timeout: 30000
        }
      );

      console.log(chalk.green('✓ AI 发布日志生成成功'));
      return response.data.data?.outputs?.result ||
             response.data.data?.outputs?.text ||
             response.data.data?.outputs?.answer ||
             `${projectType} v${version} 已发布`;

    } catch (error) {
      console.warn(chalk.yellow('⚠ AI 生成失败，使用默认模板'));

      if (error.response) {
        console.log(chalk.gray('错误状态:'), error.response.status);
        console.log(chalk.gray('错误详情:'), JSON.stringify(error.response.data, null, 2));
      } else {
        console.log(chalk.gray(error.message));
      }

      // 使用默认模板
      return `📦 **${projectType || 'Version'} v${version} 更新**

${commitMessages.slice(0, 3).map(msg => `- ${msg}`).join('\n')}

感谢您的使用！`;
    }
  }
}

/**
 * 飞书通知工具类
 */
export class FeishuUtils {
  /**
   * 发送成功通知
   * @param {string} version - 版本号
   * @param {string} notes - 发布日志
   * @param {string} projectType - 项目类型
   * @param {Object} options - 额外选项
   */
  static async notifySuccess(version, notes, projectType, options = {}) {
    const config = Config.feishu;

    if (!config.webhook) {
      console.warn(chalk.yellow('⚠ Feishu Webhook 未配置，跳过通知'));
      return false;
    }

    const projectName = this._getProjectName(projectType);
    const { content = '', server = Config.server.host } = options;

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
            tag: "hr"
          }
        ]
      }
    };

    // 如果有额外内容，添加字段
    if (content) {
      message.card.elements.push({
        tag: "div",
        fields: [
          {
            is_short: true,
            text: {
              content: `**内容:** ${content}`,
              tag: "lark_md"
            }
          },
          {
            is_short: true,
            text: {
              content: `**服务器:** ${server}`,
              tag: "lark_md"
            }
          }
        ]
      });

      // 添加访问按钮
      message.card.elements.push({
        tag: "action",
        actions: [
          {
            tag: "button",
            text: {
              content: "访问系统",
              tag: "plain_text"
            },
            type: "primary",
            url: `http://${server}/`
          },
          {
            tag: "button",
            text: {
              content: "管理后台",
              tag: "plain_text"
            },
            type: "default",
            url: `http://${server}/admin/`
          }
        ]
      });
    }

    // 添加时间戳
    message.card.elements.push({
      tag: "note",
      elements: [
        {
          tag: "plain_text",
          content: `发布时间: ${new Date().toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })}`
        }
      ]
    });

    try {
      await axios.post(config.webhook, message, { timeout: 10000 });
      console.log(chalk.green('✓ 飞书通知发送成功'));
      return true;
    } catch (error) {
      return ErrorHandler.handle(error, {
        message: '飞书通知发送失败',
        fallback: false
      });
    }
  }

  /**
   * 发送失败通知
   * @param {string} version - 版本号
   * @param {string} errorMessage - 错误消息
   * @param {string} projectType - 项目类型
   */
  static async notifyFailure(version, errorMessage, projectType = '') {
    const config = Config.feishu;

    if (!config.webhook) {
      return false;
    }

    const projectName = this._getProjectName(projectType);

    const message = {
      msg_type: "interactive",
      card: {
        config: { wide_screen_mode: true },
        header: {
          title: {
            content: `❌ ${projectName} v${version} 发布失败`,
            tag: "plain_text"
          },
          template: "red"
        },
        elements: [
          {
            tag: "markdown",
            content: `**错误:**\n${errorMessage}\n\n请检查并重试。`
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
      await axios.post(config.webhook, message, { timeout: 10000 });
      return true;
    } catch (error) {
      // 静默失败
      return false;
    }
  }

  /**
   * 获取项目中文名称
   * @private
   */
  static _getProjectName(projectType) {
    const names = {
      'backend': '心之旅后端',
      'frontend': '心之旅前端',
      'all': '心之旅平台',
      'Backend': 'Mindtrip Backend',
      'Frontend': 'Mindtrip Frontend',
      'Frontend+Backend': 'Mindtrip Platform'
    };
    return names[projectType] || projectType || '心之旅平台';
  }
}

/**
 * 导出便捷函数
 */
export const config = Config;
export const git = GitUtils;
export const dify = DifyUtils;
export const feishu = FeishuUtils;
export const error = ErrorHandler;
