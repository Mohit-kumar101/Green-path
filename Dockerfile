# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY mvnw pom.xml .mvn/ ./
COPY src ./src
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

# Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
