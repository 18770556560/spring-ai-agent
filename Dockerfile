# 使用轻量级 jdk21 JRE 运行镜像（Alpine 版本体积更小）
FROM eclipse-temurin:21-jre

WORKDIR /app

# 直接复制已构建好的 jar 包（无需重新编译）
COPY target/spring-ai-agent-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 8080

# 启动应用，可通过环境变量覆盖端口
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8080"]
