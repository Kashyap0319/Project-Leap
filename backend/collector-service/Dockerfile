# Build stage
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy Gradle files (skip gradle.properties to avoid local Java home issues)
COPY settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY gradlew gradlew.bat ./
RUN chmod +x gradlew

# Copy backend source
COPY backend ./backend

# Build the application (Java is provided by gradle:8.5-jdk21 image)
RUN ./gradlew :backend:collector-service:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR
COPY --from=build --chown=spring:spring /app/backend/collector-service/build/libs/collector-service.jar app.jar

# Expose port
EXPOSE 8080

# Note: Health check can be configured via Render's health check path
# For now, relying on Render's built-in health checks

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

