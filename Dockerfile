# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy your Maven project files into the container
COPY . .

# Build the fat (shaded) JAR
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre

# Working directory inside runtime image
WORKDIR /app

# Copy the final shaded JAR (which replaced the original)
COPY --from=build /app/target/myspark-1.1-SNAPSHOT.jar myspark.jar

# Expose the port used by SparkJava (default 4567)
EXPOSE 4567

# Run the application
CMD ["java", "-jar", "myspark.jar"]
