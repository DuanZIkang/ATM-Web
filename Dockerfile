# 使用 OpenJDK 17 轻量级镜像
FROM openjdk:17-slim

# 设置工作目录
WORKDIR /app

# 将 jar 包复制进镜像
COPY atm-1.5.2.jar app.jar

# 暴露端口
EXPOSE 8090

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]