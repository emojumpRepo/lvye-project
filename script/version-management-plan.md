# 心之旅项目版本管理方案

## 当前问题
1. 前端 package.json 使用的是 Vben 框架版本（5.5.9）
2. Git tags 使用的是项目版本（v2.6.0）
3. 版本体系混乱，不利于管理

## 建议方案

### 方案一：统一从 0.0.1 开始（推荐）
为心之旅项目建立独立的版本体系：

```
mindtrip-backend: v0.0.1  (后端)
mindtrip-frontend: v0.0.1 (前端)
```

#### 优势
- 清晰独立的版本管理
- 前后端可以独立发版
- 避免与上游框架版本混淆

#### 实施步骤
```bash
# 后端项目
cd /path/to/mindtrip-project
git tag mindtrip-backend-v0.0.1 -m "心之旅后端初始版本"
git push origin mindtrip-backend-v0.0.1

# 前端项目  
cd /path/to/mindtrip-project-frontend
git tag mindtrip-frontend-v0.0.1 -m "心之旅前端初始版本"
git push origin mindtrip-frontend-v0.0.1
```

### 方案二：继续现有版本（从 v2.7.0 开始）
保持现有版本序列：

```
后端: v2.7.0
前端: v2.7.0
```

#### 优势
- 保持版本连续性
- 前后端版本同步

#### 劣势
- 与框架版本仍有混淆风险

## 版本号规范

采用语义化版本：`MAJOR.MINOR.PATCH`

- **MAJOR**：重大更新，不兼容的 API 修改
- **MINOR**：功能性新增，向下兼容
- **PATCH**：修复问题，向下兼容

### 示例
- `v0.1.0` - 第一个功能版本
- `v0.1.1` - 修复 bug
- `v0.2.0` - 新增功能
- `v1.0.0` - 第一个稳定版本

## 独立版本管理配置

### 1. 前端版本文件
创建 `mindtrip-project-frontend/version.json`：
```json
{
  "name": "mindtrip-frontend",
  "version": "0.0.1",
  "description": "心之旅项目前端版本"
}
```

### 2. 后端版本文件
创建 `mindtrip-project/version.properties`：
```properties
project.name=mindtrip-backend
project.version=0.0.1
project.description=心之旅项目后端版本
```

### 3. 更新发布脚本

修改 `release.mjs` 支持独立版本：

```javascript
// 读取项目独立版本
function getProjectVersion(projectType) {
  if (projectType === 'backend') {
    // 读取后端 version.properties
    const versionFile = fs.readFileSync('version.properties', 'utf-8');
    const match = versionFile.match(/project.version=(.+)/);
    return match ? match[1] : '0.0.1';
  } else if (projectType === 'frontend') {
    // 读取前端 version.json
    const versionFile = JSON.parse(fs.readFileSync('yudao-ui/mindtrip-project-frontend/version.json', 'utf-8'));
    return versionFile.version;
  }
}

// 更新版本号
function updateVersion(projectType, newVersion) {
  if (projectType === 'backend') {
    // 更新 version.properties
    let content = fs.readFileSync('version.properties', 'utf-8');
    content = content.replace(/project.version=.+/, `project.version=${newVersion}`);
    fs.writeFileSync('version.properties', content);
  } else if (projectType === 'frontend') {
    // 更新 version.json
    const versionFile = JSON.parse(fs.readFileSync('yudao-ui/mindtrip-project-frontend/version.json', 'utf-8'));
    versionFile.version = newVersion;
    fs.writeFileSync('yudao-ui/mindtrip-project-frontend/version.json', JSON.stringify(versionFile, null, 2));
  }
}
```

## 发布流程

### 1. 后端发布
```bash
npm run release:backend
# 或
node script/release.mjs --type=backend --version=0.0.2
```

### 2. 前端发布
```bash
npm run release:frontend
# 或
node script/release.mjs --type=frontend --version=0.0.2
```

### 3. 前后端一起发布
```bash
npm run release:all
# 或
node script/release.mjs --type=all --version=0.0.2
```

## Git Tag 命名规范

### 独立版本标签
- 后端：`mindtrip-backend-v0.0.1`
- 前端：`mindtrip-frontend-v0.0.1`
- 全栈：`mindtrip-v0.0.1`

### 获取版本间的提交
```bash
# 后端版本间的提交
git log mindtrip-backend-v0.0.1..mindtrip-backend-v0.0.2 --oneline

# 前端版本间的提交
git log mindtrip-frontend-v0.0.1..mindtrip-frontend-v0.0.2 --oneline
```

## 实施建议

1. **立即行动**：从 v0.0.1 开始，建立独立版本体系
2. **版本文件**：创建独立的版本配置文件
3. **自动化**：更新脚本支持独立版本管理
4. **文档**：记录版本发布历史

## 版本发布检查清单

- [ ] 确定发布类型（backend/frontend/all）
- [ ] 确定版本号（major/minor/patch）
- [ ] 运行测试
- [ ] 更新版本文件
- [ ] 创建 Git Tag
- [ ] 构建部署
- [ ] 生成发布日志
- [ ] 发送通知