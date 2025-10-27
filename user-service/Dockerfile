FROM eclipse-temurin:17 AS build

# Install Maven in the JDK17 image so compilation supports Java 17
RUN apt-get update \
    && apt-get install -y maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
# Build the application (skip tests in container build)
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
