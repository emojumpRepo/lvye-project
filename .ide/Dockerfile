# 可以去 dockerhub 上: https://hub.docker.com/_/maven 找到需要的 maven 和 jdk 版本
# FROM maven:3.8.6-openjdk-8
FROM maven:3.8.5-openjdk-17

# 腾讯云软件源使用示例: https://cnb.cool/examples/mirrors/mirrors.cloud.tencent.com

# 以及按需安装其他软件
# RUN apt-get update && apt-get install -y git

# 安装 code-server 和 vscode 常用插件
RUN curl -fsSL https://code-server.dev/install.sh | sh \
  && code-server --install-extension cnbcool.cnb-welcome \
  && code-server --install-extension redhat.vscode-yaml \
  && code-server --install-extension vscjava.vscode-java-pack \
  && code-server --install-extension waderyan.gitblame \
  && code-server --install-extension mhutchie.git-graph \
  && echo done

# 安装 ssh 服务，用于支持 VSCode 客户端通过 Remote-SSH 访问开发环境, 根据需要选择包管理器安装
# maven:3.8.6-openjdk-8使用apt-get安装依赖
# RUN apt-get update && apt-get install -y wget unzip openssh-server

# maven:3.8.5-openjdk-17使用microdnf安装依赖
RUN microdnf update -y && microdnf install -y wget unzip openssh-server

# 指定字符集支持命令行输入中文（根据需要选择字符集）
ENV LANG C.UTF-8
ENV LANGUAGE C.UTF-8