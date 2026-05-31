# Build stage — curl/bash required for Maven wrapper (only-script) on Alpine
FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache bash curl
WORKDIR /app
COPY mvnw pom.xml .mvn/ ./
COPY src ./src
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
