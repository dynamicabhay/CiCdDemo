# syntax=docker/dockerfile:1

# Use Distroless Java 17 runtime (Debian 12 base)
FROM gcr.io/distroless/java17-debian12

# Set working directory
WORKDIR /app

# Copy the built JAR from your local machine into the container
COPY target/*.jar app.jar

# Copy the otel agent in CWD
COPY opentelemetry-javaagent.jar .

# Set environment variable to load the agent automatically
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"

# Expose the application port (adjust if your app uses a different one)
EXPOSE 8080
# Define the entrypoint — run the JAR
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/app.jar"]