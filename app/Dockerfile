# Multi-stage build for production
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Production image
FROM eclipse-temurin:21-jre-alpine

# Install shadow for user management
RUN apk add --no-cache shadow

# Create non-root user
RUN addgroup -g 1000 appuser && adduser -u 1000 -G appuser -s /bin/sh -D appuser

WORKDIR /app

# Copy the JAR file
COPY --from=builder /app/target/app-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:9090/health/ready || exit 1

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
