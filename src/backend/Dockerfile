# ============================================
# Multi-stage Dockerfile for ChefPro Backend
# ============================================
# Stage 1: Build
# Uses an image with Maven and JDK 17 to compile the project.
# Stage 2: Runtime
# Uses a lightweight JRE-only image to run the resulting .jar.
# ============================================

# ---------- STAGE 1: BUILD ----------
FROM eclipse-temurin:17-jdk AS build

# Working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml first (to leverage Docker layer caching)
# If pom.xml hasn't changed, Docker reuses this layer and skips downloading dependencies.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Grant execute permission to the Maven wrapper
RUN chmod +x mvnw

# Download dependencies without compiling the code (this gets cached)
RUN ./mvnw dependency:go-offline -B

# Now copy all the source code
COPY src src

# Build the project and generate the .jar (skip tests to speed up the build)
RUN ./mvnw package -DskipTests -B

# ---------- STAGE 2: RUNTIME ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the .jar generated in the build stage
COPY --from=build /app/target/*.jar app.jar

# Railway injects the PORT variable automatically.
# Spring Boot reads it via ${PORT:8081} in application.yml
EXPOSE ${PORT:-8081}

# Command to start the application
# - Uses Railway's PORT variable as the server port
ENTRYPOINT ["java", "-jar", "app.jar"]
