# PatientManagementSystem

A healthcare backend built around a microservices architecture — five services communicating via REST, gRPC, and Kafka, all wired together with Docker Compose.

![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot&logoColor=white)
![Kafka KRaft](https://img.shields.io/badge/Apache%20Kafka-KRaft-black?logo=apachekafka&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC-Protobuf%203-blue?logo=grpc&logoColor=white)
![PostgreSQL 17](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

---

## Overview

The system handles patient registration and propagates events downstream — billing gets a synchronous gRPC call, analytics gets an async Kafka event. The split is intentional: billing needs a confirmation before the response goes back, analytics doesn't.

```
REST Client → api-gateway (4004)
                    ↓
         ┌──────────┴──────────┐
         ↓                     ↓
    auth-service         patient-service
    (JWT / H2)           (PostgreSQL)
                              ↓
                    ┌─────────┼──────────┐
                    ↓                    ↓
             billing-service     analytics-service
             (gRPC server)       (Kafka consumer)

Apache Kafka (KRaft) — no Zookeeper, patient-events topic (Protobuf)
```

---

## Services

| Service | Port | Role |
|---------|------|------|
| api-gateway | 4004 | JWT validation, request routing |
| patient-service | 4000 | Core CRUD, triggers gRPC + Kafka |
| auth-service | 4005 | Token issuance, BCrypt hashing |
| billing-service | 9001 | gRPC server, billing account management |
| analytics-service | 4002 | Kafka consumer, event processing |

---

## Getting Started

```bash
docker compose up --build
```

Starts everything — PostgreSQL, Kafka, all five services. No manual wiring needed.

For local dev, start billing-service first (patient-service makes a gRPC call to it on startup):

```bash
cd billing-service && mvn spring-boot:run
cd analytics-service && mvn spring-boot:run
cd auth-service && mvn spring-boot:run
cd patient-service && mvn spring-boot:run
```

---

## Example: Creating a Patient

```bash
# Authenticate
curl -X POST http://localhost:4005/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}'

# Create patient
curl -X POST http://localhost:4000/api/patients \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","dateOfBirth":"1990-01-15"}'
```

On creation, patient-service saves to PostgreSQL, makes a synchronous gRPC call to billing-service, and publishes a Protobuf-encoded event to Kafka. The analytics-service picks it up asynchronously — its failure doesn't affect the response.

---

## Testing

```bash
mvn clean test -f integration-tests/pom.xml
```

Integration tests cover the full flow: auth token lifecycle, patient CRUD behind JWT, gRPC round-trip to billing, and Kafka event publishing.

---

## Project Structure

```
PatientManagementSystem/
├── patient-service/       # REST API, gRPC client, Kafka producer
├── auth-service/          # JWT util, BCrypt, H2 seed data
├── billing-service/       # gRPC server impl, billing.proto
├── analytics-service/     # Kafka listener, patient_event.proto
├── api-gateway/           # JWT filter, global exception handling
├── integration-tests/     # End-to-end test suite
├── docker-compose.yml
└── pom.xml                # Parent POM with shared dependency versions
```

---

## Stack

- **Java 21** — records, sealed classes, modern LTS
- **Spring Boot 3.2** — web, security, data JPA
- **Apache Kafka (KRaft)** — no Zookeeper, 3.x production config
- **gRPC + Protobuf 3** — binary RPC, contract-first
- **PostgreSQL 17** — patient data, ACID guarantees
- **Spring Security + JJWT** — stateless JWT auth
- **Docker Compose** — single-command local environment
- **JUnit 5 + REST-Assured** — integration test coverage

---

## What's Next

- Circuit breakers (Resilience4j)
- Distributed tracing (Jaeger or OpenTelemetry)
- Prometheus + Grafana metrics
- Flyway for schema migrations
- Kubernetes manifests
