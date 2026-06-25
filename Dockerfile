FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Копируем файлы сборки Gradle
COPY gradle gradle/
COPY gradlew ./
COPY settings.gradle.kts build.gradle.kts ./

# ВАЖНО: даём права на gradlew
RUN chmod +x gradlew

# Скачиваем зависимости и собираем jar
RUN ./gradlew clean build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
