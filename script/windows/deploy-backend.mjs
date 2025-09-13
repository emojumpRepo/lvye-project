#!/usr/bin/env zx

// 曼朗-心之旅 后端部署脚本（zx 版本）
// 使用方法: npx zx script/windows/deploy-backend.mjs
// 或: npm run deploy:backend

import 'zx/globals'
import { promises as fs } from 'fs'
import os from 'os'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

// zx 配置
$.verbose = false

// 服务器配置（与前端一致，可按需修改）
const SERVER_HOST = '42.194.163.176'
const SERVER_USER = 'root'
const SERVER_PORT = 22

// 本地 JAR 路径
const LOCAL_JAR = join(__dirname, '../../yudao-server/target/yudao-server.jar')
const JAR_NAME = 'yudao-server.jar'

console.log('========================================')
console.log('      曼朗-心之旅后端部署到宝塔')
console.log('========================================')
console.log()

// 询问远程部署目录，提供常见默认值
let REMOTE_DIR = await question('请输入后端远程目录（默认: /www/wwwroot/lvye-server）: ')
if (!REMOTE_DIR.trim()) REMOTE_DIR = '/www/wwwroot/lvye-server'

console.log('\n========================================')
console.log(`服务器: ${SERVER_HOST}`)
console.log(`远程目录: ${REMOTE_DIR}`)
console.log(`本地JAR: ${LOCAL_JAR}`)
console.log('========================================\n')

// 检查本地 JAR 是否存在
try {
  await fs.access(LOCAL_JAR)
  console.log('[信息] 找到JAR文件，准备部署...')
} catch {
  console.log('[错误] 未找到本地 JAR 文件，请先构建：')
  console.log('  mvn -f yudao-server clean package -DskipTests')
  process.exit(1)
}

// 询问服务器密码（隐藏输入）
const password = await question('请输入服务器密码: ', { type: 'password' })
if (!password) {
  console.log('[错误] 密码不能为空')
  process.exit(1)
}

// 检查 WinSCP 是否安装（优先 WinSCP.com；必要时用 winscp.exe + /console）
async function findWinScp() {
  const exists = async (p) => { try { await fs.access(p); return true } catch { return false } }
  try { const p = await which('WinSCP.com'); if (p) return { path: p, useConsole: false } } catch {}
  try {
    const exe = await which('winscp');
    if (exe) {
      const dir = dirname(exe);
      const com = join(dir, 'WinSCP.com');
      if (await exists(com)) return { path: com, useConsole: false }
      return { path: exe, useConsole: true }
    }
  } catch {}
  if (process.platform === 'win32') {
    const comCandidates = [
      join(process.env['PROGRAMFILES'] || 'C:/Program Files', 'WinSCP', 'WinSCP.com'),
      join(process.env['PROGRAMFILES(X86)'] || 'C:/Program Files (x86)', 'WinSCP', 'WinSCP.com'),
      join(os.homedir(), 'scoop', 'apps', 'winscp', 'current', 'WinSCP.com')
    ]
    for (const c of comCandidates) { if (await exists(c)) return { path: c, useConsole: false } }
    const exeCandidates = [ join(os.homedir(), 'scoop', 'shims', 'winscp.exe') ]
    for (const e of exeCandidates) { if (await exists(e)) return { path: e, useConsole: true } }
  }
  return null
}

const winScp = await findWinScp()
if (!winScp) {
  console.log('[错误] 未找到WinSCP，请先安装: https://winscp.net')
  console.log('  - scoop install winscp')
  process.exit(1)
}
const winScpPath = winScp.path
const winScpConsoleSwitch = winScp.useConsole ? ' /console' : ''
console.log(`[信息] 找到WinSCP: ${winScpPath}${winScpConsoleSwitch}`)

// 创建临时 WinSCP 脚本
const tempDir = os.tmpdir()
const scriptName = `deploy_backend_${Date.now()}.txt`
const scriptPath = join(tempDir, scriptName)
const logPath = join(tempDir, 'winscp_deploy_backend.log')

const winscpScript = `open "sftp://${SERVER_USER}@${SERVER_HOST}:${SERVER_PORT}" -password=%1
option batch abort
option confirm off

# 时间戳
call TS=$(date +%Y%m%d_%H%M%S)

# 目录准备
call mkdir -p "${REMOTE_DIR}"
call mkdir -p "${REMOTE_DIR}/temp"

# 停止旧进程（若存在）
call PIDS=$(ps -ef | grep '${JAR_NAME}' | grep -v grep | awk '{print $2}')
call if [ -n "$PIDS" ]; then echo "[停止] 发送TERM信号..."; kill -TERM $PIDS; sleep 5; fi
call PIDS2=$(ps -ef | grep '${JAR_NAME}' | grep -v grep | awk '{print $2}')
call if [ -n "$PIDS2" ]; then echo "[强制] 发送KILL信号..."; kill -KILL $PIDS2; fi

# 备份现有JAR
call if [ -f "${REMOTE_DIR}/${JAR_NAME}" ]; then cp "${REMOTE_DIR}/${JAR_NAME}" "${REMOTE_DIR}/temp/${JAR_NAME}.$TS.bak"; echo "[备份] 备份为 ${JAR_NAME}.$TS.bak"; fi

# 上传新的 JAR（.new）
put "${LOCAL_JAR}" "${REMOTE_DIR}/${JAR_NAME}.new"

# 原子替换
call mv -f "${REMOTE_DIR}/${JAR_NAME}.new" "${REMOTE_DIR}/${JAR_NAME}"

# 启动服务（可按需修改 JVM 参数）
call nohup java -jar "${REMOTE_DIR}/${JAR_NAME}" >/dev/null 2>&1 &
call sleep 3

# 校验进程
call if pgrep -f "${JAR_NAME}" >/dev/null; then echo "[成功] 服务已启动"; else echo "[警告] 服务可能未启动，请检查日志"; fi

close
exit`

await fs.writeFile(scriptPath, winscpScript, 'utf8')

try {
  console.log('[部署] 正在上传并部署后端...')
  console.log(`[调试] ${winScpPath}${winScpConsoleSwitch} /script="${scriptPath}" /log="${logPath}" /parameter ****`)
  await $`${winScpPath} ${winScpConsoleSwitch} /script=${scriptPath} /log=${logPath} /parameter ${password}`

  console.log('\n========================================')
  console.log('         后端部署成功完成！')
  console.log('========================================\n')
  console.log(`服务器: ${SERVER_HOST}`)
  console.log(`远程目录: ${REMOTE_DIR}`)
  console.log(`[日志] WinSCP日志: ${logPath}`)
} catch (e) {
  console.log('[错误] 部署失败，请检查 WinSCP 日志与服务日志')
  console.log(`[日志] WinSCP日志: ${logPath}`)
  const view = await question('是否查看WinSCP日志？(y/n): ')
  if (view.toLowerCase() === 'y') {
    try { const s = await fs.readFile(logPath, 'utf8'); console.log(s) } catch {}
  }
  process.exit(1)
} finally {
  try { await fs.unlink(scriptPath) } catch {}
}

