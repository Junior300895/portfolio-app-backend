# ── Build stage ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cache layer)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Runtime stage ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create uploads directory
RUN mkdir -p /uploads/photos /uploads/videos /uploads/thumbnails

# Copy jar
COPY --from=build /app/target/*.jar app.jar

# Non-root user for security
RUN useradd -m -u 1001 appuser && chown -R appuser:appuser /app /uploads
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
