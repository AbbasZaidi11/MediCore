# =======================================================
# WHY IS THE BUILD CONTEXT THE REPO ROOT?
# This service is part of a Maven multi-module project.
# The parent pom.xml lives at the root and lists all modules.
# Maven needs to read it to understand the project structure,
# so the Docker build context must be the repo root (set in
# docker-compose.yml), not just this service's folder.
# =======================================================

# =======================================================
# WHY TWO STAGES?
# Stage 1 (builder) compiles the code with Maven + JDK.
# Stage 2 (runner)  only runs the JAR with a slim JRE.
# The final image contains NO Maven, NO compiler, NO source
# code — just the JAR. This keeps the image small and secure.
# =======================================================

# -------------------------------------------------------
# STAGE 1 — BUILD
# This image ships both Maven and Java 21 so we can compile.
# 'AS builder' gives this stage a name we reference below.
# -------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# All subsequent commands run inside /app inside the container.
WORKDIR /app

# Copy ALL pom.xml files BEFORE copying any source code.
#
# WHY all of them?
#   The parent pom.xml lists every module. Maven reads it and
#   expects every module's pom.xml to exist, even when only
#   building one module (-pl api-gateway). Without them
#   Maven exits with an error.
#
# WHY poms before source?
#   Docker caches each line as a layer. If only source files
#   change (no pom change), Docker reuses the cached
#   dependency-download layer and skips the slow download.
COPY pom.xml .
COPY api-gateway/pom.xml       ./api-gateway/pom.xml
COPY patient-service/pom.xml   ./patient-service/pom.xml
COPY analytics-service/pom.xml ./analytics-service/pom.xml
COPY billing-service/pom.xml   ./billing-service/pom.xml
COPY auth-service/pom.xml      ./auth-service/pom.xml
COPY integration-tests/pom.xml ./integration-tests/pom.xml
COPY infrastructure/pom.xml    ./infrastructure/pom.xml

# Download all dependencies for api-gateway into the
# local Maven cache inside this image layer.
# -pl api-gateway = only resolve this module's dependencies.
# -B              = non-interactive mode for cleaner logs.
RUN mvn dependency:go-offline -pl api-gateway -B

# NOW copy the source code. Intentionally done after the
# dependency download so changing source files doesn't bust
# the expensive download cache layer above.
COPY api-gateway/src ./api-gateway/src

# Compile and package into a fat JAR.
# -DskipTests = don't run unit tests during Docker build.
RUN mvn -pl api-gateway clean package -DskipTests

# -------------------------------------------------------
# STAGE 2 — RUNTIME
# Much smaller image — only the Java runtime, no build tools.
# -------------------------------------------------------
FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

# Pull ONLY the compiled JAR out of the builder stage.
# Nothing else (source code, Maven cache) ends up in this image.
COPY --from=builder /app/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar ./app.jar

# Document that the gateway listens on port 4004.
# (The actual port mapping is done in docker-compose.yml)
EXPOSE 4004

# Command that runs when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]