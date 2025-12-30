# ============================================================================
# Dockerfile for HousingUtilitiesSystemAdmin
# Supports two build modes:
# 1. CI/CD: Uses pre-built JAR (when target/*.jar exists)
# 2. Standalone: Builds from source (multi-stage build)
# ============================================================================

# Stage 1: Build (only if needed)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create directories for file uploads
RUN mkdir -p /app/uploads /app/temp-uploads

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/admin/login || exit 1

# Run with docker profile
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app/app.jar"]
