# -------------------------------------------------------
# Build context must be the repo ROOT (see docker-compose)
# so Maven can resolve the parent POM.
# -------------------------------------------------------

# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy parent POM first to benefit from Docker layer caching
COPY pom.xml ./pom.xml
COPY patient-service/pom.xml ./patient-service/pom.xml

# Download all dependencies (cached unless pom changes)
RUN mvn dependency:go-offline -pl patient-service -am -B

# Copy source and build the module
COPY patient-service/src ./patient-service/src
RUN mvn clean package -pl patient-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

COPY --from=builder /app/patient-service/target/*.jar ./app.jar

EXPOSE 4000

ENTRYPOINT ["java", "-jar", "app.jar"]
