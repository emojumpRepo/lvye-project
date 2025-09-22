# 发布脚本编码问题修复

## 🔧 问题诊断

### 1. **乱码来源**
- readline-sync 在 Windows 中文环境下的编码问题
- 控制台输出中文时出现乱码：`璇烽€夋嫨` 和 `纭鍙戝竷`

### 2. **解决方案**

#### 方案 A：使用英文提示（已实施）
将所有中文提示改为英文，避免编码问题：

```javascript
// 之前（中文）
readline.question('请选择 (1/2/3): ')
readline.question('确认发布? (y/N): ')

// 之后（英文）
readline.question('Please select (1/2/3): ')
readline.question('Confirm release? (y/N): ')
```

#### 方案 B：设置控制台编码（备选）
在 Windows 下运行前设置编码：
```bash
# PowerShell
[Console]::OutputEncoding = [Text.Encoding]::UTF8
chcp 65001

# 或在脚本开头添加
process.stdout.setEncoding('utf8');
process.stdin.setEncoding('utf8');
```

## ✅ 已修复内容

### release-v2.mjs 修改
- `是否继续?` → `Continue anyway?`
- `请选择` → `Please select`
- `确认发布?` → `Confirm release?`

## 📝 使用示例

### 修复后的交互界面
```
✓ 当前分支：master
✓ 代码已更新到最新

请选择发布项目:
  [1] 后端 (backend)
  [2] 前端 (frontend)
  [3] 前后端 (all)
Please select (1/2/3): 2    ← 英文提示，无乱码

当前版本: v0.0.3

请选择版本类型:
  [1] Patch (修复) - 0.0.4
  [2] Minor (功能) - 0.1.0
  [3] Major (重大) - 1.0.0
Please select (1/2/3): 1     ← 英文提示，无乱码

即将发布:
  项目: frontend
  版本: v0.0.3 → v0.0.4

Confirm release? (y/N): y    ← 英文提示，无乱码
```

## 💡 其他编码相关优化

### Git 提交信息编码
已添加编码过滤，确保提交信息正确显示：
```javascript
// 移除非法字符
message.replace(/[^\x20-\x7E\u4e00-\u9fa5]/g, '');
```

### 前端项目提交示例（无乱码）
```
feat(workspace): 优化工作台组件的响应式布局和样式
fix(心理咨询): 修复查看预约详情时ID获取逻辑
refactor(components/LyButton): 重构按钮组件样式和尺寸配置
```

## 🚀 运行命令

```bash
# 使用新版发布工具（英文提示）
pnpm release:v2

# 或直接运行
node script/release-v2.mjs
```

## ⚠️ 注意事项

1. **Windows 环境**
   - 推荐使用 Windows Terminal 或 PowerShell
   - 避免使用旧版 CMD

2. **编码设置**
   - 确保项目文件使用 UTF-8 编码
   - Git 配置：`git config --global core.quotepath false`

3. **中文显示**
   - 静态文本（如标题）仍使用中文
   - 交互提示使用英文避免乱码

## 📚 相关问题

### 如果仍有乱码
1. 检查终端编码：`chcp` 应显示 65001
2. 更新 Node.js 到最新版本
3. 考虑使用 inquirer 替代 readline-sync

### 版本文件未提交
- 这是正常的，版本文件可能已经在之前提交
- 脚本会检查并跳过，不影响发布流程