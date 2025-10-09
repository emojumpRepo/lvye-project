#!/usr/bin/env node

/**
 * 配置加载工具
 * 读取YAML配置文件并替换环境变量
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import dotenv from 'dotenv';
import yaml from 'js-yaml';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 加载 .env 文件
const envPath = path.join(__dirname, '../../.env');
dotenv.config({ path: envPath });

/**
 * 递归替换对象中的环境变量占位符
 * @param {any} obj - 要处理的对象
 * @returns {any} 处理后的对象
 */
function replaceEnvVariables(obj) {
    if (typeof obj === 'string') {
        // 匹配 ${VAR_NAME} 格式的环境变量
        return obj.replace(/\$\{([^}]+)\}/g, (match, varName) => {
            return process.env[varName] || match;
        });
    }
    
    if (Array.isArray(obj)) {
        return obj.map(item => replaceEnvVariables(item));
    }
    
    if (obj && typeof obj === 'object') {
        const result = {};
        for (const key in obj) {
            result[key] = replaceEnvVariables(obj[key]);
        }
        return result;
    }
    
    return obj;
}

/**
 * 加载并处理YAML配置文件
 * @param {string} filePath - YAML文件路径
 * @returns {object} 处理后的配置对象
 */
export function loadConfig(filePath) {
    try {
        // 读取YAML文件
        const fileContent = fs.readFileSync(filePath, 'utf8');
        
        // 解析YAML
        let config = yaml.load(fileContent);
        
        // 替换环境变量
        config = replaceEnvVariables(config);
        
        return config;
    } catch (error) {
        throw new Error(`Failed to load config from ${filePath}: ${error.message}`);
    }
}

/**
 * 检查必需的环境变量
 * @param {string[]} required - 必需的环境变量名列表
 */
export function checkRequiredEnv(required) {
    const missing = [];
    
    for (const varName of required) {
        if (!process.env[varName]) {
            missing.push(varName);
        }
    }
    
    if (missing.length > 0) {
        console.error('Missing required environment variables:');
        missing.forEach(varName => {
            console.error(`  - ${varName}`);
        });
        console.error('\\nPlease set these in your .env file');
        process.exit(1);
    }
}

// 如果直接运行此文件，测试配置加载
if (import.meta.url === `file://${process.argv[1]}`) {
    const configPath = path.join(__dirname, 'cos-deployment-config.yaml');
    
    // 检查必需的环境变量
    checkRequiredEnv(['TENCENT_SECRET_ID', 'TENCENT_SECRET_KEY']);
    
    // 加载配置
    const config = loadConfig(configPath);
    
    console.log('Loaded configuration:');
    console.log(JSON.stringify(config, null, 2));
}