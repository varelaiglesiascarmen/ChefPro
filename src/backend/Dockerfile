# ============================================
# Multi-stage Dockerfile for ChefPro Backend
# ============================================
# Stage 1: Build
# Uses an image with Maven and JDK 17 to compile the project.
# Stage 2: Runtime
# Uses a lightweight JRE-only image to run the resulting .jar.
# ============================================

# ---------- STAGE 1: BUILD ----------
FROM maven:3.9-eclipse-temurin-17 AS build

# Working directory inside the container
WORKDIR /app

# Copy pom.xml first (to leverage Docker layer caching)
# If pom.xml hasn't changed, Docker reuses this layer and skips downloading dependencies.
COPY src/backend/pom.xml .

# Download dependencies without compiling the code (this gets cached)
RUN mvn dependency:go-offline -B

# Now copy all the source code
COPY src/backend/src src

# Build the project and generate the .jar (skip tests to speed up the build)
RUN mvn package -DskipTests -B

# ---------- STAGE 2: RUNTIME ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the .jar generated in the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (Spring Boot 4.0 default)
EXPOSE 8080

# Start the application
# Railway injects PORT; we pass it as server.port so Spring listens on the right port
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
