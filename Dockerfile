FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app

COPY gradle gradle/
COPY gradlew ./
COPY settings.gradle.kts build.gradle.kts ./

RUN chmod +x gradlew

COPY src ./src
# Если есть другие папки (например, config), можно добавить и их:
# COPY config ./config

RUN ./gradlew clean build -x test

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
