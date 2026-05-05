# Use a Java 21 base image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy Gradle wrapper files
COPY gradlew .
COPY gradle/ gradle/

# Copy build configuration files
COPY build.gradle settings.gradle ./

# Copy the source code (this includes src/main/resources/application.properties or .yml)
COPY src ./src

# Ensure dos2unix is available in the image
RUN apk add --no-cache dos2unix

# DEBUG: Clean and print the content of application.yml to verify it's the correct one and properly formatted
RUN dos2unix src/main/resources/application.yml \
    && echo "--- Content of application.yml after dos2unix ---" \
    && cat src/main/resources/application.yml \
    && echo "-------------------------------------------------" # <--- MODIFICADO PARA DEPURACIÓN Y LIMPIEZA

# Build the application
RUN ./gradlew bootJar

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "build/libs/pos-0.0.1.jar"]
