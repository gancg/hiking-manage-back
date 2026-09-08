# 构建阶段：使用 Java 17 编译并打包应用
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 单独复制依赖配置，便于复用依赖缓存
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# 运行阶段：仅保留 Java 运行环境和应用文件
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 使用普通用户运行，数据库路径与 application.yaml 保持一致
RUN groupadd --system app && useradd --system --gid app app \
    && mkdir -p /app/data && chown app:app /app/data
COPY --from=build --chown=app:app /build/target/hiking-manage-back-*.jar /app/app.jar

USER app
VOLUME ["/app/data"]
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
