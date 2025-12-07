# Multi-stage build for collector-service (builds jar inside container)
FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :backend:collector-service:bootJar -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/backend/collector-service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
