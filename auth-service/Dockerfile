FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY patient-service/pom.xml  ./patient-service/pom.xml
COPY analytics-service/pom.xml ./analytics-service/pom.xml
COPY api-gateway/pom.xml      ./api-gateway/pom.xml
COPY billing-service/pom.xml  ./billing-service/pom.xml
COPY integration-tests/pom.xml ./integration-tests/pom.xml
COPY infrastructure/pom.xml   ./infrastructure/pom.xml

RUN mvn dependency:go-offline -pl auth-service -B

COPY auth-service/src ./auth-service/src

RUN mvn -pl auth-service clean package -DskipTests

FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

COPY --from=builder /app/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4005

ENTRYPOINT ["java", "-jar", "app.jar"]
