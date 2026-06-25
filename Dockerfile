# Stage 1: сборка (Gradle + Java 26)
FROM eclipse-temurin:26-jdk AS builder
WORKDIR /app

# Копируем только то, что нужно для инициализации wrapper
COPY gradlew .
COPY gradle/ gradle/

# Проверяем, что Gradle нужный версии (опционально, можно убрать)
RUN ./gradlew --version

# Копируем весь проект
COPY . .

# Собираем bootJar без тестов (для скорости)
# Если позже захочешь тесты — убери -x test
RUN ./gradlew clean bootJar --no-daemon -x test

# Stage 2: рантайм (только JRE, без Gradle)
FROM eclipse-temurin:26-jre
WORKDIR /app
EXPOSE 8080

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]