FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy source including checkstyle at root
COPY pom.xml .
COPY checkstyle.xml .
COPY src ./src

# Build without running checkstyle (checkstyle runs in validate which precedes resources)
RUN mvn package -DskipTests -Dcheckstyle.skip=true -B

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user
RUN groupadd -r stotracker && useradd -r -g stotracker stotracker

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

# Create data directory with proper permissions for mounted volume
RUN mkdir -p /data && chmod 777 /data

EXPOSE 4545

ENTRYPOINT ["java", "-jar", "-Dserver.port=4545", "-Dspring.datasource.url=jdbc:sqlite:/data/stotracker.db", "app.jar"]