FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app

COPY gradle gradle/
COPY gradlew ./
COPY settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew clean build -x test

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/

ENTRYPOINT ["java", "-jar", "/app/vacancy-scout-0.0.1-SNAPSHOT.jar"]
