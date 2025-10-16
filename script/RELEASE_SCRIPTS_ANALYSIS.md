# Release 脚本优化分析

## ✅ 已完成的优化 (2025-01-06)

### 1. 文件组织优化 ✅
- **改进**: 将 `release.mjs` 和 `release-with-deploy.mjs` 移动到 `script/windows/` 目录
- **好处**: 统一管理所有 Windows 部署相关脚本
- **影响**: `package.json` 中的路径已更新

### 2. 命令行参数支持 ⭐ ✅
- **改进**: `release.mjs` 现在支持直接传递 `frontend` 或 `backend` 参数
- **用法**:
  ```bash
  npm run release:frontend  # 自动跳过项目选择，直接发布前端
  npm run release:backend   # 自动跳过项目选择，直接发布后端
  ```
- **好处**: 提高发布效率，特别适合 CI/CD 集成

### 3. PROJECT_ROOT 路径调整 ✅
- **改进**: 更新路径计算从 `__dirname/..` 到 `__dirname/../..`
- **原因**: 脚本移动到子目录后需要调整相对路径

### 4. 配置管理 - 环境变量迁移 🔴 ✅

**完成内容**:
- ✅ 创建 `script/lib/release-utils.mjs` 共享工具库
- ✅ 移除所有硬编码的敏感配置
- ✅ 配置迁移到 `.env` 文件
- ✅ 创建 `.env.example` 模板文件
- ✅ 添加配置验证机制
- ✅ 创建配置说明文档 `ENV_CONFIG_GUIDE.md`

**改进前**:
```javascript
const CONFIG = {
  dify: {
    apiKey: 'app-LTUF7HU291Ug9LAKD4ZC4ZHO',  // ⚠️ 暴露在代码中
    apiUrl: 'http://154.9.255.162/v1'
  },
  feishu: {
    webhook: 'https://open.feishu.cn/...'    // ⚠️ 暴露在代码中
  }
};
```

**改进后**:
```javascript
import { config } from '../lib/release-utils.mjs';

// 自动从 .env 加载
const apiKey = config.dify.apiKey;
const webhook = config.feishu.webhook;

// 自动验证
if (!config.validate()) {
  process.exit(1);
}
```

**影响文件**:
- ✅ `script/windows/release.mjs` - 已重构
- ✅ `script/windows/release-with-deploy.mjs` - 已重构
- ✅ `script/test-dify.mjs` - 已更新
- ✅ `.env` - 已添加新配置
- ✅ `.env.example` - 已创建模板

**收益**:
- ✅ 提高安全性，避免敏感信息泄露
- ✅ 支持多环境配置
- ✅ 更易于团队协作

---

### 5. 代码重复 - 提取公共函数 🟡 ✅

**完成内容**:
- ✅ 创建 `script/lib/release-utils.mjs` 工具库
- ✅ 提取约 200+ 行重复代码
- ✅ 统一错误处理策略
- ✅ 提供清晰的模块化 API

**提取的公共模块**:

#### 1. Config 类 - 配置管理
```javascript
import { config } from './lib/release-utils.mjs';

config.dify.apiKey       // Dify API 密钥
config.feishu.webhook    // 飞书 Webhook
config.server.host       // 服务器地址
config.validate()        // 验证配置完整性
```

#### 2. GitUtils 类 - Git 操作
```javascript
import { git } from './lib/release-utils.mjs';

git.checkBranch('master', cwd)              // 检查分支
git.checkUncommittedChanges(cwd)            // 检查未提交更改
git.pullLatest('master', cwd)               // 拉取最新代码
git.createTag(tagName, message, cwd)        // 创建标签
git.determineReleaseType(version, cwd)      // 判断发布类型
```

#### 3. DifyUtils 类 - AI 日志生成
```javascript
import { dify } from './lib/release-utils.mjs';

await dify.generateReleaseNotes(
  version,
  commitMessages,
  releaseType,
  projectType
);
```

#### 4. FeishuUtils 类 - 飞书通知
```javascript
import { feishu } from './lib/release-utils.mjs';

await feishu.notifySuccess(version, notes, projectType, options);
await feishu.notifyFailure(version, errorMessage, projectType);
```

#### 5. ErrorHandler 类 - 统一错误处理
```javascript
import { error } from './lib/release-utils.mjs';

error.handle(err, {
  critical: false,      // 是否致命错误
  message: '自定义消息',
  showStack: false,     // 是否显示堆栈
  fallback: null        // 失败时返回值
});
```

**代码减少量**:
- `release.mjs`: 457 行 → 338 行 (减少 **26%**)
- `release-with-deploy.mjs`: 619 行 → 300 行 (减少 **52%**)
- 总计减少约 **438 行重复代码**

**收益**:
- ✅ 大幅减少代码重复
- ✅ 提高可维护性
- ✅ 统一行为和错误处理
- ✅ 更易于测试

---

## 📊 优化成果总结

| 优化项 | 状态 | 代码减少 | 安全性 | 可维护性 |
|--------|------|----------|--------|----------|
| 配置管理（环境变量） | ✅ 完成 | - | ⬆️ 大幅提升 | ⬆️ 提升 |
| 代码重复（提取公共函数） | ✅ 完成 | -438 行 | - | ⬆️ 大幅提升 |
| 错误处理统一 | ✅ 完成 | - | - | ⬆️ 提升 |
| 命令行参数支持 | ✅ 完成 | - | - | ⬆️ 提升 |

---

## 🔍 发现的优化机会 (待实施)

**问题**: 敏感配置硬编码在脚本中
```javascript
const CONFIG = {
  dify: {
    apiKey: 'app-LTUF7HU291Ug9LAKD4ZC4ZHO',  // ⚠️ 暴露在代码中
    apiUrl: 'http://154.9.255.162/v1'
  },
  feishu: {
    webhook: 'https://open.feishu.cn/...'    // ⚠️ 暴露在代码中
  }
};
```

**建议**: 迁移到 `.env` 文件
```javascript
// 改进后：
import dotenv from 'dotenv';
dotenv.config({ path: path.join(__dirname, '../../.env') });

const CONFIG = {
  dify: {
    apiKey: process.env.DIFY_API_KEY,
    apiUrl: process.env.DIFY_API_URL
  },
  feishu: {
    webhook: process.env.FEISHU_WEBHOOK
  }
};
```

**影响文件**:
- `script/windows/release.mjs`
- `script/windows/release-with-deploy.mjs`
- `script/test-dify.mjs`

---

### 2. 代码重复 🟡 中优先级

**问题**: 两个脚本之间有大量重复代码

**重复的函数** (占比约 40%):
1. `generateReleaseNotes()` - 完全相同 (~70 行)
2. `notifyFeishu()` 系列 - 基本相同 (~80 行)
3. `determineReleaseType()` - 完全相同 (~30 行)
4. Git 分支检查逻辑 - 相似 (~30 行)
5. CONFIG 配置对象 - 部分重复

**建议**: 创建共享工具库
```javascript
// script/lib/release-utils.mjs
export class ReleaseUtils {
  constructor(config) {
    this.config = config;
  }

  async generateReleaseNotes(version, commits, releaseType) { ... }
  async notifyFeishu(version, projectType, notes) { ... }
  determineReleaseType(version) { ... }
  checkGitBranch() { ... }
}

// 在 release.mjs 中使用
import { ReleaseUtils } from '../lib/release-utils.mjs';
const utils = new ReleaseUtils(CONFIG);
await utils.generateReleaseNotes(...);
```

**收益**:
- 减少约 200 行重复代码
- 更易维护和测试
- 统一错误处理

---

### 3. 错误处理不一致 🟡 中优先级

**问题**: 两个脚本的错误处理风格不同

**release.mjs** 风格:
```javascript
try {
  execSync(`git tag ...`);
  console.log(chalk.green('✓ 成功'));
} catch (error) {
  console.error(chalk.red('失败:'), error.message);
  // 不阻止流程继续
}
```

**release-with-deploy.mjs** 风格:
```javascript
try {
  execSync(`git tag ...`);
  console.log(chalk.green('✓ 成功'));
} catch (error) {
  console.log(chalk.yellow('⚠ 失败（非关键）'));
  // 继续执行
}
```

**建议**: 统一错误处理策略
```javascript
// script/lib/error-handler.mjs
export class ErrorHandler {
  static handle(error, options = {}) {
    const { critical = false, fallback = null } = options;

    console.error(chalk.red('✗ 错误:'), error.message);

    if (critical) {
      process.exit(1);
    }

    return fallback;
  }
}
```

---

### 4. Dry-run 模式 🟢 低优先级

**建议**: 添加 `--dry-run` 参数支持
```bash
npm run release:frontend -- --dry-run
```

**功能**:
- 显示将要执行的操作，但不实际执行
- 检查版本号计算是否正确
- 预览将要生成的发布日志
- 验证 Git 状态

**实现示例**:
```javascript
const args = process.argv.slice(2);
const isDryRun = args.includes('--dry-run');

if (isDryRun) {
  console.log(chalk.yellow('[DRY RUN] 预览模式，不会实际执行'));
}

// 在执行关键操作前检查
if (!isDryRun) {
  execSync(`git tag -a ${tagName} ...`);
} else {
  console.log(chalk.gray(`[DRY RUN] 将创建标签: ${tagName}`));
}
```

---

### 5. 日志记录改进 🟢 低优先级

**问题**: `recordRelease()` 使用简单的 append 方式，不便查询

**当前实现**:
```javascript
fs.appendFileSync(logFile, JSON.stringify(record) + '\n');
```

**建议**: 使用结构化日志
```javascript
// script/lib/release-logger.mjs
export class ReleaseLogger {
  constructor(logPath) {
    this.logPath = logPath;
  }

  log(record) {
    // 读取现有日志
    let logs = [];
    if (fs.existsSync(this.logPath)) {
      logs = JSON.parse(fs.readFileSync(this.logPath, 'utf-8'));
    }

    // 添加新记录
    logs.push({
      ...record,
      id: Date.now(),
      timestamp: new Date().toISOString()
    });

    // 保持最近 50 条
    if (logs.length > 50) {
      logs = logs.slice(-50);
    }

    // 写回文件（格式化）
    fs.writeFileSync(this.logPath, JSON.stringify(logs, null, 2));
  }

  getHistory(limit = 10) {
    if (!fs.existsSync(this.logPath)) return [];
    const logs = JSON.parse(fs.readFileSync(this.logPath, 'utf-8'));
    return logs.slice(-limit).reverse();
  }
}
```

---

### 6. Git Commit 编码问题处理 🟢 低优先级

**当前实现**:
```javascript
return message.replace(/[^\x20-\x7E\u4e00-\u9fa5]/g, '');
```

**问题**: 可能过滤掉一些有效字符（如 emoji）

**建议**: 更精确的处理
```javascript
function cleanCommitMessage(message) {
  // 移除控制字符，但保留 emoji 和其他有效 Unicode
  return message
    .replace(/[\x00-\x1F\x7F-\x9F]/g, '')  // 移除控制字符
    .replace(/\s+/g, ' ')                   // 规范化空白
    .trim();
}
```

---

## 📊 优化优先级总结

| 优化项 | 优先级 | 工作量 | 收益 | 建议时间 |
|--------|--------|--------|------|----------|
| 配置管理（环境变量） | 🔴 高 | 1 小时 | 安全性提升 | 立即 |
| 代码重复（提取公共函数） | 🟡 中 | 3 小时 | 可维护性提升 | 1-2 周内 |
| 错误处理统一 | 🟡 中 | 2 小时 | 稳定性提升 | 1-2 周内 |
| Dry-run 模式 | 🟢 低 | 2 小时 | 开发体验提升 | 可选 |
| 日志记录改进 | 🟢 低 | 1 小时 | 可追溯性提升 | 可选 |
| Commit 编码处理 | 🟢 低 | 30 分钟 | 兼容性提升 | 可选 |

---

## 🎯 建议的实施计划

### 阶段 1：安全性（本周内）
- [ ] 将敏感配置迁移到 `.env`
- [ ] 添加 `.env.example` 模板文件
- [ ] 更新文档说明如何配置环境变量

### 阶段 2：代码质量（下周）
- [ ] 创建 `script/lib/release-utils.mjs` 工具库
- [ ] 重构两个 release 脚本，使用共享函数
- [ ] 统一错误处理策略
- [ ] 添加单元测试（可选）

### 阶段 3：开发体验（按需）
- [ ] 实现 dry-run 模式
- [ ] 改进日志记录系统
- [ ] 优化 commit 消息处理

---

## 💡 其他建议

### 1. CI/CD 集成
考虑添加 GitHub Actions 或其他 CI/CD 支持：
```yaml
# .github/workflows/release.yml
name: Release
on:
  push:
    tags:
      - 'mindtrip-*-v*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Release
        run: npm run release:deploy -- --auto
        env:
          DIFY_API_KEY: ${{ secrets.DIFY_API_KEY }}
          FEISHU_WEBHOOK: ${{ secrets.FEISHU_WEBHOOK }}
```

### 2. 版本号验证
添加语义化版本验证：
```javascript
function validateSemanticVersion(version) {
  const semverRegex = /^(\d+)\.(\d+)\.(\d+)$/;
  if (!semverRegex.test(version)) {
    throw new Error('Invalid semantic version');
  }
  return true;
}
```

### 3. 预发布支持
支持 beta/rc 版本：
```bash
npm run release:frontend -- --prerelease=beta
# 生成版本: 1.2.3-beta.1
```

---

## 📝 变更记录

- **2025-01-06**: 完成文件移动到 `script/windows/` 目录
- **2025-01-06**: 添加命令行参数支持（frontend/backend）
- **2025-01-06**: 完成优化分析文档
