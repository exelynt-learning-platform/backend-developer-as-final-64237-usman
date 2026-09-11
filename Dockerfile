# ── Stage 1: Build ──────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn

# Cache dependencies
RUN chmod +x mvnw && ./mvnw dependency:resolve -B

COPY src src

# Build the application
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Runtime ────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy the exact jar name (ensure <finalName> is set in pom.xml)
COPY --from=builder /app/target/resource-booking.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]