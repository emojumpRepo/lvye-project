# 心之旅心理健康管理平台

<p align="center">
 <img src="https://img.shields.io/badge/Spring%20Boot-3.4.5-blue.svg" alt="Spring Boot">
 <img src="https://img.shields.io/badge/Vue-3.5-blue.svg" alt="Vue">
 <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License" />
</p>

## 🌟 项目简介

**心之旅（Mindtrip）**是一个专业的心理健康管理平台，为学校和教育机构提供全方位的心理健康服务解决方案。平台支持多角色权限管理、心理测评、咨询预约、档案管理等核心功能，助力构建健康的校园心理服务体系。

## 🚀 快速开始

### 环境要求

- **Java**: JDK 17 或更高版本
- **Node.js**: 20.10.0 或更高版本
- **包管理器**: pnpm 10.10.0 或更高版本
- **数据库**: MySQL 8.0+ / PostgreSQL 13+
- **缓存**: Redis 6.0+

### 后端启动

```bash
# 1. 安装依赖
mvn clean install -DskipTests

# 2. 启动服务（开发环境）
cd mindtrip-server
mvn spring-boot:run -Dspring.profiles.active=local

# 或使用 IDEA 直接运行 MindtripServerApplication
```

### 前端启动

```bash
# 1. 进入前端目录
cd mindtrip-ui/lvye-project-frontend

# 2. 安装依赖（使用 pnpm）
pnpm install

# 3. 启动开发服务器
pnpm dev:admin  # 管理后台
pnpm dev:web    # 用户前台
```

### 访问地址

- 管理后台：http://localhost:5173
- 用户前台：http://localhost:5174
- API 文档：http://localhost:48080/swagger-ui

## 📁 项目结构

```
mindtrip-project/
├── mindtrip-dependencies/          # Maven 依赖版本管理
├── mindtrip-framework/            # 框架核心
│   ├── mindtrip-common/          # 公共工具类
│   ├── mindtrip-spring-boot-starter-*  # 各类 Spring Boot Starter
│   └── ...
├── mindtrip-server/              # 服务端主程序
├── mindtrip-module-*/            # 业务模块
│   ├── mindtrip-module-system/   # 系统管理
│   ├── mindtrip-module-psychology/  # 心理健康模块（核心）
│   ├── mindtrip-module-infra/    # 基础设施
│   └── ...
├── mindtrip-ui/                  # 前端项目
│   └── lvye-project-frontend/ # Vue3 前端应用
├── script/                    # 部署和工具脚本
└── sql/                      # 数据库脚本
```

## ✨ 核心功能

### 🧠 心理健康管理
- **心理测评系统**：支持多种心理量表和问卷
- **咨询预约管理**：在线预约心理咨询服务
- **学生心理档案**：完整的心理健康档案管理
- **危机预警系统**：及时发现和干预心理危机
- **心理知识库**：心理健康知识普及和教育

### 👥 多角色支持
- **系统管理员**：全局配置和系统管理
- **心理咨询师**：咨询服务和个案管理
- **班级辅导员**：学生心理状况跟踪
- **学生用户**：测评、咨询、学习

### 🔧 系统功能
- **用户权限管理**：基于 RBAC 的权限控制
- **租户管理**：支持 SaaS 多租户架构
- **工作流引擎**：基于 Flowable 的审批流程
- **数据报表**：可视化数据分析和报表
- **消息通知**：短信、邮件、站内信通知

## 🛠 技术栈

### 后端技术
| 技术 | 说明 | 版本 |
| --- | --- | --- |
| Spring Boot | 应用框架 | 3.4.5 |
| Spring Security | 安全框架 | 6.x |
| MyBatis Plus | ORM 框架 | 3.5.x |
| Flowable | 工作流引擎 | 7.x |
| Redis | 缓存数据库 | 6.0+ |
| MySQL | 关系型数据库 | 8.0+ |
| Elasticsearch | 搜索引擎 | 7.x |

### 前端技术
| 技术 | 说明 | 版本 |
| --- | --- | --- |
| Vue | 前端框架 | 3.5.13 |
| Vite | 构建工具 | 6.2.5 |
| Ant Design Vue | UI 组件库 | 4.2.6 |
| TypeScript | 开发语言 | 5.8.3 |
| Pinia | 状态管理 | 2.3.1 |
| Vue Router | 路由管理 | 4.x |

## 📦 部署指南

### Docker 部署

```bash
# 构建镜像
docker build -t mindtrip-backend .

# 运行容器
docker-compose up -d
```

### 传统部署

详见 [部署文档](./docs/deployment.md)

## 🔨 开发指南

### 代码规范
- 后端遵循《阿里巴巴 Java 开发手册》
- 前端遵循 Vue 3 风格指南
- Git 提交遵循 Conventional Commits 规范

### 分支管理
- `master`: 主分支，稳定版本
- `develop`: 开发分支
- `feature/*`: 功能分支
- `hotfix/*`: 紧急修复分支

### 版本发布

```bash
# 使用发布脚本
npm run release:v2

# 或手动发布
node script/release-v2.mjs
```

## 📊 项目统计

- **后端代码**: 100,000+ 行 Java 代码
- **前端代码**: 50,000+ 行 TypeScript/Vue 代码
- **测试覆盖率**: 核心功能 80%+ 覆盖
- **API 接口**: 500+ RESTful API

## 🤝 贡献指南

我们欢迎所有形式的贡献，包括但不限于：

- 提交 Bug 报告和功能建议
- 改进文档和代码注释
- 提交代码改进和新功能
- 分享使用经验和最佳实践

请查看 [贡献指南](./CONTRIBUTING.md) 了解更多信息。

## 📄 开源协议

本项目采用 [MIT License](./LICENSE) 开源协议，个人和企业可 100% 免费使用。

## 🌐 相关链接

- 项目文档：[https://mindtrip.com/docs](https://mindtrip.com/docs)
- 在线演示：[https://demo.mindtrip.com](https://demo.mindtrip.com)
- 技术支持：admin@mindtrip.com

## 🙏 致谢

感谢所有为本项目做出贡献的开发者和用户！

特别感谢以下开源项目：
- Spring Boot 及 Spring 生态
- Vue.js 及相关生态
- Ant Design Vue 组件库
- 其他所有依赖的开源项目

---

**心之旅团队** ❤️ 用心守护心理健康