# Use a base image with Java installed
FROM  openjdk:17

# Set a working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/ita-online-testing-1.0.0.jar app.jar

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]