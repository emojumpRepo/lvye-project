/**
 * 部署配置文件
 * 集中管理所有部署相关的配置
 */

module.exports = {
  // 服务器配置
  server: {
    host: process.env.SSH_HOST || '42.194.163.176',
    port: process.env.SSH_PORT || 22,
    user: process.env.SSH_USER || 'root',
    password: process.env.SSH_PASSWORD || 'MF@Luye!996', // 默认密码，生产环境应使用环境变量
    
    // 部署路径
    paths: {
      backend: {
        root: '/root/mindfront/work/project/mindtrip_server',
        backup: '/root/mindfront/work/project/mindtrip_server/backup',
        build: '/root/mindfront/work/project/mindtrip_server/build',
        target: '/root/mindfront/work/project/mindtrip_server/target',
      },
      frontend: {
        admin: {
          project: '/root/mindfront/work/project/mindtrip_apps/admin',
          nginx: '/root/mindfront/work/nginx/html/admin',
          backup: '/root/mindfront/work/project/mindtrip_apps/admin_backup'
        },
        web: {
          project: '/root/mindfront/work/project/mindtrip_apps/web',
          nginx: '/root/mindfront/work/nginx/html/web',
          backup: '/root/mindfront/work/project/mindtrip_apps/web_backup'
        }
      }
    }
  },
  
  // Docker 配置
  docker: {
    backend: {
      imageName: 'yudao-server',
      containerName: 'mindtrip-server',
      port: '48080:48080',
      restart: 'always'
    }
  },
  
  // 构建配置
  build: {
    backend: {
      command: 'mvn clean package -DskipTests',
      jarName: 'yudao-server.jar',
      targetPath: 'yudao-server/target'
    },
    frontend: {
      admin: {
        path: 'yudao-ui/lvye-project-frontend/apps/admin',
        command: 'pnpm build',
        output: 'dist.zip'
      },
      web: {
        path: 'yudao-ui/lvye-project-frontend/apps/web',
        command: 'pnpm build',
        output: 'dist.zip'
      }
    }
  },
  
  // API 配置
  api: {
    dify: {
      enabled: true,
      apiKey: process.env.DIFY_API_KEY || 'app-LTUF7HU291Ug9LAKD4ZC4ZHO',
      apiUrl: process.env.DIFY_API_URL || 'http://154.9.255.162/v1',
      timeout: 30000,
      // AI 提示词模板
      promptTemplate: `请基于以下信息生成一份简洁的产品更新说明（面向运营团队）：

版本号: {version}
更新内容: 
{changes}

最近提交记录:
{commits}

要求：
1. 用通俗易懂的语言，避免技术术语
2. 突出对用户的价值和改进
3. 控制在 200 字以内
4. 如果有新功能请重点说明
5. 分类展示：新功能、优化、修复`
    },
    feishu: {
      enabled: true,
      webhook: process.env.FEISHU_WEBHOOK_URL || 'https://open.feishu.cn/open-apis/bot/v2/hook/cd69ceec-aaa6-422f-b23a-fac71382ebb0',
      timeout: 10000,
      // 消息模板
      messageTemplate: {
        success: {
          template: 'green',
          title: '🚀 绿叶项目 {version} 发布成功'
        },
        failure: {
          template: 'red',
          title: '❌ 绿叶项目 {version} 发布失败'
        }
      }
    }
  },
  
  // OSS 配置（预留）
  oss: {
    enabled: false,
    provider: process.env.OSS_PROVIDER || 'aliyun', // aliyun | tencent | qiniu | aws
    
    // 阿里云 OSS
    aliyun: {
      region: process.env.OSS_REGION,
      bucket: process.env.OSS_BUCKET,
      accessKeyId: process.env.OSS_ACCESS_KEY,
      accessKeySecret: process.env.OSS_SECRET_KEY,
      // 自定义域名（可选）
      customDomain: process.env.OSS_CUSTOM_DOMAIN
    },
    
    // 腾讯云 COS
    tencent: {
      secretId: process.env.COS_SECRET_ID,
      secretKey: process.env.COS_SECRET_KEY,
      bucket: process.env.COS_BUCKET,
      region: process.env.COS_REGION
    },
    
    // 部署产物路径
    paths: {
      prefix: 'releases/', // OSS 路径前缀
      keepVersions: 10, // 保留的历史版本数
      autoUpload: false, // 是否自动上传到 OSS
      autoClean: true // 是否自动清理旧版本
    }
  },
  
  // 备份配置
  backup: {
    enabled: true,
    keepCount: 4, // 保留的备份数量
    // 备份文件命名格式
    nameFormat: '{name}_{timestamp}.{ext}',
    // 是否压缩备份
    compress: false
  },
  
  // 通知配置
  notification: {
    // 发布成功后的通知
    onSuccess: ['feishu'], // 可选: feishu, email, dingtalk, webhook
    // 发布失败后的通知
    onFailure: ['feishu'],
    // 是否发送详细日志
    includeDetails: true
  },
  
  // 发布配置
  release: {
    // 是否自动创建 Git 标签
    autoTag: true,
    // 是否推送标签到远程
    pushTag: true,
    // 标签前缀
    tagPrefix: 'v',
    // 是否生成 CHANGELOG
    generateChangelog: true,
    // CHANGELOG 文件路径
    changelogPath: 'CHANGELOG.md',
    // 是否自动提交版本更新
    autoCommit: true,
    // 提交信息模板
    commitMessage: 'chore: release version {version}'
  },
  
  // 环境配置
  environments: {
    // 开发环境
    development: {
      server: { host: '42.194.163.176' },
      autoTag: false,
      notification: { onSuccess: [], onFailure: ['feishu'] }
    },
    // 测试环境
    staging: {
      server: { host: '42.194.163.176' },
      autoTag: false
    },
    // 生产环境
    production: {
      server: { host: '42.194.163.176' },
      autoTag: true,
      backup: { keepCount: 10 }
    }
  },
  
  // 健康检查
  healthCheck: {
    enabled: true,
    // 后端健康检查
    backend: {
      url: 'http://42.194.163.176:48080/actuator/health',
      timeout: 10000,
      retries: 3,
      retryDelay: 5000
    },
    // 前端健康检查
    frontend: {
      admin: {
        url: 'http://42.194.163.176/admin/',
        expectedStatus: 200
      },
      web: {
        url: 'http://42.194.163.176/',
        expectedStatus: 200
      }
    }
  },
  
  // 回滚配置
  rollback: {
    enabled: true,
    // 快速回滚脚本路径
    scriptPath: 'scripts/rollback.mjs',
    // 是否自动创建回滚点
    autoSnapshot: true
  },
  
  // 日志配置
  logging: {
    // 日志级别: debug | info | warn | error
    level: process.env.LOG_LEVEL || 'info',
    // 日志文件路径
    file: 'releases.log',
    // 是否输出到控制台
    console: true,
    // 是否记录到远程
    remote: false
  }
};

// 获取当前环境配置
function getConfig(env = process.env.NODE_ENV || 'production') {
  const baseConfig = module.exports;
  const envConfig = baseConfig.environments[env] || {};
  
  // 深度合并配置
  return deepMerge(baseConfig, envConfig);
}

// 深度合并对象
function deepMerge(target, source) {
  const result = { ...target };
  
  for (const key in source) {
    if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
      result[key] = deepMerge(target[key] || {}, source[key]);
    } else {
      result[key] = source[key];
    }
  }
  
  return result;
}

// 导出配置和辅助函数
module.exports.getConfig = getConfig;
module.exports.deepMerge = deepMerge;