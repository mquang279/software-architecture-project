# Build
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B -Dmaven.wagon.http.retryHandler.count=3 \
    dependency:resolve dependency:resolve-plugins

COPY src ./src

RUN mvn -B -DskipTests package

# Run Stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
COPY opentelemetry-javaagent.jar /opt/opentelemetry-javaagent.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]