#!/usr/bin/env node

/**
 * 测试 Dify Workflow API 连接和功能
 * 用于调试 AI 日志生成功能
 */

import axios from 'axios';
import chalk from 'chalk';
import { execSync } from 'child_process';

const CONFIG = {
  apiKey: 'app-27WVHbSe1uUxcd54gDGKySl1',
  apiUrl: 'http://154.9.255.162/v1'
};

console.log(chalk.cyan('========================================'));
console.log(chalk.cyan('      Dify Workflow API 测试工具'));
console.log(chalk.cyan('========================================'));
console.log();

async function testDifyWorkflowAPI() {
  try {
    // 获取最近的 Git 提交（作为 query 内容）
    let commits = '';
    let commitMessages = [];
    try {
      // 尝试获取两个tag之间的提交
      const tags = execSync('git tag -l "v*" --sort=-version:refname | head -2', { 
        encoding: 'utf-8' 
      }).trim().split('\n');
      
      if (tags.length >= 2 && tags[0] && tags[1]) {
        console.log(chalk.blue(`获取 ${tags[1]} 到 ${tags[0]} 之间的提交：`));
        commits = execSync(`git log ${tags[1]}..${tags[0]} --oneline`, { 
          encoding: 'utf-8' 
        });
      } else {
        console.log(chalk.blue('获取最近10条 Git 提交：'));
        commits = execSync('git log --oneline -10', { encoding: 'utf-8' });
      }
      
      // 提取commit messages（移除hash）
      commitMessages = commits.split('\n')
        .filter(line => line.trim())
        .map(line => line.replace(/^[a-f0-9]{7,}\s+/, ''))
        .slice(0, 8); // 最多8条
        
      console.log(chalk.gray(commitMessages.map((m, i) => `  ${i + 1}. ${m}`).join('\n')));
    } catch (error) {
      commitMessages = ['暂无提交记录'];
    }
    
    // 构建测试 prompt - 使用真实的commit信息
    const testPrompt = commitMessages.join('；').substring(0, 150);

    console.log(chalk.blue('\n发送的 Prompt：'));
    console.log(chalk.gray('=' . repeat(40)));
    console.log(chalk.gray(testPrompt));
    console.log(chalk.gray('=' . repeat(40)));
    console.log();
    
    console.log(chalk.blue('API 配置：'));
    console.log('  URL:', CONFIG.apiUrl);
    console.log('  Endpoint: /workflows/run');
    console.log('  Key:', CONFIG.apiKey.substring(0, 10) + '...');
    console.log();
    
    // 测试 Workflow API - 尝试不同的输入字段名
    console.log(chalk.blue('测试 1: 使用 query 作为输入字段'));
    const request1 = {
      inputs: {
        query: "你好，请简单介绍一下自己"
      },
      response_mode: "blocking",
      user: "test-user"
    };
    
    try {
      const response1 = await axios.post(
        `${CONFIG.apiUrl}/workflows/run`,
        request1,
        {
          headers: {
            'Authorization': `Bearer ${CONFIG.apiKey}`,
            'Content-Type': 'application/json'
          },
          timeout: 30000
        }
      );
      
      console.log(chalk.green('✓ 请求成功'));
      console.log('响应结构:', Object.keys(response1.data));
      
      // 打印完整响应以了解数据结构
      console.log('完整响应:', JSON.stringify(response1.data, null, 2));
      
      // 尝试提取输出
      const output = response1.data.data?.outputs?.text || 
                    response1.data.data?.outputs?.answer ||
                    response1.data.data?.outputs?.output ||
                    response1.data.data?.text ||
                    response1.data.outputs?.text ||
                    response1.data.text ||
                    '(未找到输出内容)';
      
      console.log('提取的输出:', output);
    } catch (error) {
      console.log(chalk.red('✗ 使用 query 字段失败'));
      if (error.response) {
        console.log('状态码:', error.response.status);
        console.log('错误信息:', JSON.stringify(error.response.data, null, 2));
      } else {
        console.log('错误:', error.message);
      }
    }
    
    console.log();
    
    // 测试其他可能的字段名
    console.log(chalk.blue('测试 2: 使用 text 作为输入字段'));
    const request2 = {
      inputs: {
        text: "测试消息"
      },
      response_mode: "blocking",
      user: "test-user"
    };
    
    try {
      const response2 = await axios.post(
        `${CONFIG.apiUrl}/workflows/run`,
        request2,
        {
          headers: {
            'Authorization': `Bearer ${CONFIG.apiKey}`,
            'Content-Type': 'application/json'
          },
          timeout: 30000
        }
      );
      
      console.log(chalk.green('✓ 使用 text 字段成功'));
      console.log('响应:', JSON.stringify(response2.data, null, 2));
    } catch (error) {
      if (error.response?.status === 400) {
        console.log(chalk.yellow('✗ text 字段不正确'));
      }
    }
    
    console.log();
    
    // 测试完整的发布日志生成 - 简化版本
    console.log(chalk.blue('测试 3: 极简发布日志生成'));
    const fullRequest = {
      inputs: {
        query: testPrompt,  // 50-100字的极简输入
        version: '1.0.0',
        release_type: 'minor',  // 功能版本
        target_audience: 'operation'  // 运营团队
      },
      response_mode: "blocking",
      user: "release-bot"
    };
    
    const startTime = Date.now();
    
    try {
      console.log('发送请求体:', JSON.stringify(fullRequest, null, 2));
      
      const response3 = await axios.post(
        `${CONFIG.apiUrl}/workflows/run`,
        fullRequest,
        {
          headers: {
            'Authorization': `Bearer ${CONFIG.apiKey}`,
            'Content-Type': 'application/json'
          },
          timeout: 30000
        }
      );
      
      const duration = ((Date.now() - startTime) / 1000).toFixed(1);
      
      console.log(chalk.green('✓ 发布日志生成成功'));
      console.log('耗时:', duration, '秒');
      console.log();
      
      // 打印完整响应
      console.log(chalk.blue('完整响应数据：'));
      console.log(JSON.stringify(response3.data, null, 2));
      
      // 尝试从不同路径提取结果
      const possiblePaths = [
        'data.outputs.text',
        'data.outputs.answer',
        'data.outputs.output',
        'data.outputs.result',
        'data.text',
        'outputs.text',
        'outputs.answer',
        'text',
        'answer',
        'result'
      ];
      
      console.log();
      console.log(chalk.blue('尝试提取输出：'));
      for (const path of possiblePaths) {
        const value = path.split('.').reduce((obj, key) => obj?.[key], response3.data);
        if (value) {
          console.log(chalk.green(`✓ 找到输出 (${path}):`), value.substring(0, 100) + '...');
          
          console.log();
          console.log(chalk.blue('生成的发布日志：'));
          console.log(chalk.gray('=' . repeat(40)));
          console.log(value);
          console.log(chalk.gray('=' . repeat(40)));
          break;
        }
      }
      
    } catch (error) {
      console.log(chalk.red('✗ 发布日志生成失败'));
      
      if (error.response) {
        console.log();
        console.log(chalk.red('错误详情：'));
        console.log('状态码:', error.response.status);
        console.log('错误信息:', JSON.stringify(error.response.data, null, 2));
        
        // 可能的原因
        console.log();
        console.log(chalk.yellow('可能的原因：'));
        if (error.response.status === 400) {
          console.log('- Workflow 的输入变量名不是 "query"');
          console.log('- 需要检查 Dify Workflow 中定义的输入变量名');
          console.log('- API Key 可能没有执行权限');
        } else if (error.response.status === 401) {
          console.log('- API Key 认证失败');
        } else if (error.response.status === 404) {
          console.log('- Workflow 未发布或不存在');
        } else if (error.response.status === 500) {
          console.log('- Dify 服务器内部错误');
        }
      } else {
        console.log('网络错误:', error.message);
      }
    }
    
  } catch (error) {
    console.error(chalk.red('测试过程出现错误:'), error.message);
  }
}

// 运行测试
console.log(chalk.blue('开始测试 Dify Workflow API...'));
console.log();

testDifyWorkflowAPI().then(() => {
  console.log();
  console.log(chalk.green('测试完成！'));
  console.log();
  console.log(chalk.cyan('重要提示：'));
  console.log('1. 确保您的 Dify 应用是 Workflow 类型');
  console.log('2. Workflow 必须已发布');
  console.log('3. 检查 Workflow 中的输入变量名（可能是 query、text、input 等）');
  console.log('4. 检查 Workflow 的输出变量名');
  console.log('5. API Key 需要有执行权限');
});