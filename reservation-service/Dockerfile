FROM eclipse-temurin:21 AS build

# Install Maven in the JDK21 image so compilation supports --release 21
RUN apt-get update \
    && apt-get install -y maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
# copy project files and build
COPY . .
# run a proper maven package goal in batch mode and skip tests
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
