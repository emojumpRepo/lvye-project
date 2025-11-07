#!/bin/bash

# Post Tool Use Tracker Hook
# 
# 这个 hook 在 Claude 使用工具后运行
# 用于追踪文件编辑、搜索等操作，帮助下一次激活正确的技能
# 
# 触发时机：每次工具使用后（如 edit_file, read_file, grep 等）

TOOL_NAME="$1"
TOOL_RESULT="$2"

# 可以在这里添加追踪逻辑
# 例如：记录最近编辑的文件、搜索的内容等

# 目前保持简单，只记录日志（可选）
# echo "[$(date)] Tool used: $TOOL_NAME" >> .claude/tool-usage.log

exit 0

