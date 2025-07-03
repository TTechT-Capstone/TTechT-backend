FROM maven:3.9.4-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render will override this with $PORT)
EXPOSE 8080

# Run the application
CMD ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]