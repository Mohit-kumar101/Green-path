# Build with official Maven image (avoids mvnw/Alpine issues on Render)
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline -DskipTests

COPY src ./src
ENV MAVEN_OPTS="-Xmx768m"
RUN mvn -B -ntp -DskipTests package

# Slim runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
