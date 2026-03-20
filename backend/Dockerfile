# Stage 1: Build
FROM maven:3.9.4-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY backend/pom.xml .
RUN mvn dependency:go-offline

COPY backend/src ./src
RUN mvn clean package -X -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Install mongo_crypt_v1.so if it exists in the build context
# Using a directory copy to /tmp then moving avoids failures if the specific file is missing
COPY docker/lib/ /tmp/docker-lib/
RUN if [ -f /tmp/docker-lib/mongo_crypt_v1.so ]; then cp /tmp/docker-lib/mongo_crypt_v1.so /usr/lib/; fi && rm -rf /tmp/docker-lib

# Set up the shared library path
ENV LD_LIBRARY_PATH=/usr/lib

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
