# 前端项目独立版本发布说明

## 🔧 已修复的问题

### 1. 前端项目 Git 提交获取
- ✅ 前端项目有独立的 Git 仓库（`yudao-ui/lvye-project-frontend/.git`）
- ✅ 修复了获取前端项目提交记录的逻辑
- ✅ 支持前端独立的提交历史

### 2. 编码问题修复
- ✅ 添加了编码过滤，移除非法字符
- ✅ 确保中文和英文正常显示

### 3. Git 提交错误处理
- ✅ 检查是否有文件变化再提交
- ✅ 使用 try-catch 处理可能的错误
- ✅ 标签创建失败不阻止后续流程

## 📝 前端项目发布流程

### 正确的提交记录获取逻辑

```javascript
// 前端项目在独立的 Git 仓库中
workDir = 'yudao-ui/lvye-project-frontend'

// 获取前端项目的提交
git log --oneline -15  // 在前端目录执行
```

### 前端项目的实际提交示例
```
bb1af27a4 feat(workspace): 优化工作台组件的响应式布局和样式
249bf4fd5 fix(心理咨询): 修复查看预约详情时ID获取逻辑
c41c8d220 refactor(components/LyButton): 重构按钮组件样式和尺寸配置
d1bfb73db refactor(assessment): 将类型定义提取到共享文件以提升代码复用性
7c01d6752 feat(admin): 问卷列表增加问卷编码和是否启用字段
```

## 🚀 使用方法

### 发布前端版本
```bash
# 使用新版发布工具
pnpm release:v2

# 选择前端项目
请选择发布项目:
  [1] 后端 (backend)
  [2] 前端 (frontend)  ← 选择这个
  [3] 前后端 (all)
```

### 工作流程
1. **检查分支**：确保在 master 分支
2. **选择项目**：选择前端 (2)
3. **选择版本类型**：patch/minor/major
4. **自动获取提交**：从前端独立仓库获取
5. **创建标签**：`mindtrip-frontend-v0.0.2`
6. **生成发布日志**：基于前端提交
7. **发送通知**：飞书通知

## 📁 项目结构

```
lvye-project/                     # 主仓库（后端）
├── script/
│   └── release-v2.mjs           # 发布脚本
├── version.properties           # 后端版本文件
└── yudao-ui/
    └── lvye-project-frontend/   # 前端独立仓库
        ├── .git/                # 前端独立的 Git
        └── version.json         # 前端版本文件
```

## ⚠️ 注意事项

1. **前端有独立 Git 仓库**
   - 前端提交记录独立管理
   - 前端标签独立创建
   - 版本号独立递增

2. **版本文件位置**
   - 前端：`yudao-ui/lvye-project-frontend/version.json`
   - 后端：`version.properties`

3. **标签命名**
   - 前端：`mindtrip-frontend-v{version}`
   - 后端：`mindtrip-backend-v{version}`

## 💡 问题排查

### 如果提交记录获取失败
1. 检查是否在前端目录有 `.git`
2. 确认前端项目有提交历史
3. 查看错误信息确认路径

### 如果 Git 提交失败
- 正常情况，版本文件可能没有变化
- 脚本会继续执行，不影响发布

### 如果标签创建失败
- 可能是标签已存在
- 检查网络连接
- 确认有推送权限