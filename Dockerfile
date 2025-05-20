FROM maven:3.8.3-openjdk-17 AS builder

WORKDIR /usr/src/app
COPY . .

RUN chmod +x mvnw && ./mvnw clean package -DskipTests=false -Dcheckstyle.skip=true -Dmaven.test.skip=false

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=builder /usr/src/app/target/*.jar app.jar


ENTRYPOINT ["java", "-jar", "/app/app.jar"]
