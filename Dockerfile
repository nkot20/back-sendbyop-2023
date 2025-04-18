# Build stage
FROM maven:3.8.4-openjdk-17-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Add application user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy the built artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 9002

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]