# Gemini 代码助手上下文

## 项目概述

使用 Java、Spring Boot 和 Vue.js 构建。该项目被设计为高度可扩展，并通过模块化提供了广泛的功能。

后端是一个多模块的 Maven 项目，其中 `yudao-server` 是主应用程序。前端使用 Vue.js 构建，并提供多种 UI 库选项。

## 关键技术

*   **后端:** Java 17, Spring Boot 3.4.5, Spring Security, MyBatis Plus, Redis, Flowable
*   **前端:** Vue.js 3, Element Plus, vben (Ant Design Vue), uni-app
*   **数据库:** MySQL, Oracle, PostgreSQL, SQL Server 等
*   **构建工具:** Maven

## 构建和运行项目

### 环境要求

*   Java 17
*   Maven
*   Redis
*   MySQL

### 后端

1.  **构建项目:**
    ```bash
    mvn clean install
    ```

2.  **运行应用:**
    主应用程序位于 `yudao-server` 模块中。您可以通过运行 `YudaoServerApplication` 类在您的 IDE 中启动它，或者通过命令行运行：
    ```bash
    java -jar yudao-server/target/yudao-server.jar
    ```

### 前端

前端代码位于 `yudao-ui` 目录下。项目提供了不同版本的前端实现。例如，要运行 `yudao-ui-admin-vben` 前端：

1.  **进入前端目录:**
    ```bash
    cd yudao-ui/yudao-ui-admin-vben
    ```

2.  **安装依赖:**
    ```bash
    npm install
    ```

3.  **启动开发服务器:**
    ```bash
    npm run dev
    ```

## 开发规范

*   项目遵循《阿里巴巴 Java 开发手册》。
*   代码库有良好的注释。
*   项目非常注重模块化和清晰的架构。
*   使用单元测试来保证代码质量。