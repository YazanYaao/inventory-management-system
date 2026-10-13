# ============================================================
# Stage 1: Build Stage (Compile and Package Spring Boot JAR)
# ============================================================
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build

# Copy maven wrapper and project definition files for layer caching
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Download dependencies offline (speeds up repeated builds)
RUN sed -i 's/\r$//' ./mvnw && chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy application source code
COPY src src

# Package the application (skip tests as CI runs tests separately)
RUN ./mvnw clean package -DskipTests -B

# ============================================================
# Stage 2: Production Runtime Stage (Lean, Secure JRE Image)
# ============================================================
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="Inventory Management System (IMS)"
LABEL description="Production container image for IMS Spring Boot application"

# Create a non-root group and user for security compliance
RUN groupadd -r appgroup && useradd -r -g appgroup -m -d /home/appuser appuser

WORKDIR /app

# Copy executable jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Configure JVM memory optimizations for containerized workloads
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
