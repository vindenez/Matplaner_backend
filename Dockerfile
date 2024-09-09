# Use an OpenJDK 22 runtime as a parent image
FROM eclipse-temurin:22-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the packaged jar file into the container
COPY target/tasterj-0.0.1-SNAPSHOT.jar /app/spring-boot-app.jar


# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/spring-boot-app.jar"]
