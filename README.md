# PatientManagementSystem – Enterprise Microservices Backend

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-black?logo=apachekafka&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC-Protocol%20Buffers%203-blue?logo=grpc&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven%203.9-C71A36?logo=apachemaven&logoColor=white)

---

## 🎯 Project Overview

A **production-ready microservices backend** that demonstrates sophisticated distributed systems engineering for healthcare applications. This repository showcases modern backend development practices, scalable architecture patterns, and enterprise-grade Java implementation.

**Why this project matters for your career:**
- **Real-world architecture**: Mirrors patterns used at FAANG companies (Netflix, Uber, etc.)
- **Enterprise patterns**: Validates understanding of SOLID principles, design patterns, and system design
- **Demonstrates senior-level thinking**: Shows ability to make architectural trade-offs and justify them
- **Fully deployable**: Production-ready with Docker, proper configuration management, and test coverage

---

## 🏗️ Architecture Highlights

### 5 Microservices in One Ecosystem

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Docker Compose Network                       │
│                                                                     │
│  🌐 REST Client                                                    │
│     │                                                              │
│     ▼                                                              │
│  ┌──────────────┐                                                 │
│  │ api-gateway  │ (Port 4004) — Cross-cutting concerns            │
│  └────────┬─────┘                                                 │
│           │                                                        │
│           ├─────────────────────────────────────────────┐        │
│           │                                             │         │
│           ▼                                             ▼         │
│  ┌──────────────────┐  REST    ┌──────────────────────┐        │
│  │ auth-service     │          │ patient-service      │        │
│  │ (Port 4005)      │          │ (Port 4000)          │        │
│  │ H2 In-Memory     │          │ PostgreSQL 17        │        │
│  │ JWT + BCrypt     │          │ UUID Primary Keys    │        │
│  └──────────────────┘          └──────┬───────────────┘        │
│                                      │                          │
│                        ┌─────────────┼──────────────┐           │
│                        │             │              │           │
│          ┌─────────────▼──┐  ┌──────▼──┐   ┌─────▼────┐       │
│          │ gRPC Call      │  │ Kafka   │   │ Billing  │       │
│          │ (Binary)       │  │ Event   │   │ Service  │       │
│          │ Protobuf 3     │  │ Publish │   │ (gRPC)   │       │
│          └────────────────┘  └──────────┘   └──────────┘       │
│                                    │                            │
│                                    ▼                            │
│                        ┌──────────────────────┐                │
│                        │analytics-service     │                │
│                        │(Kafka Consumer)      │                │
│                        │(Port 4002)           │                │
│                        └──────────────────────┘                │
│                                                                │
│          ┌──────────────────────────────────────┐             │
│          │ Apache Kafka (KRaft)                 │             │
│          │ • No Zookeeper (3.x production best) │             │
│          │ • Kafka UI (Port 8080)               │             │
│          └──────────────────────────────────────┘             │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔑 Key Engineering Concepts Demonstrated

### 1. **Synchronous Communication: gRPC + Protocol Buffers**
- **Why gRPC?** Strongly-typed, binary-encoded, contract-first inter-service communication
- **Implementation**: `patient-service` ➜ `billing-service` gRPC stubs
- **Benefit**: Type safety at compile time, enables independent versioning, ~10x faster than JSON

```proto
// billing_service.proto
service BillingService {
  rpc CreateBillingAccount(BillingRequest) returns (BillingResponse);
}

message BillingRequest {
  string patientId = 1;
  string name = 2;
  string email = 3;
}
```

### 2. **Asynchronous Communication: Event-Driven with Apache Kafka**
- **Pattern**: Fire-and-forget async messaging for non-critical operations
- **Implementation**: Kafka producer in `patient-service`, consumer in `analytics-service`
- **Serialization**: Protobuf binary (not JSON) for efficiency and schema safety
- **Benefit**: Services are loosely coupled; `analytics-service` can scale independently or fail without bringing down patient registration

### 3. **Multi-Protocol API Gateway**
- **Purpose**: Single entry point for multiple backend services
- **Features**: Request routing, exception handling, cross-cutting concerns (logging, metrics, etc.)
- **Demonstrates**: Understanding of API gateway pattern and edge computing concerns

### 4. **JWT-Based Stateless Authentication**
- **Technology**: Spring Security + JJWT library
- **Implementation**: `auth-service` issues JWT tokens; all microservices validate
- **Benefit**: Scales horizontally (no session affinity required); stateless authentication

### 5. **Containerized Deployment**
- **Multi-stage Docker builds**: Optimizes image sizes (builder stage ≠ runtime stage)
- **Docker Compose orchestration**: One-command startup for entire stack
- **Production-ready**: Proper logging, health checks, resource limits

---

## 📊 Services Deep Dive

### 🏥 `patient-service` (REST API, Port 4000)
**Business capability:** Patient lifecycle management

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/patients` | GET | Fetch all patients (paginated) |
| `/api/patients/{id}` | GET | Fetch single patient by UUID |
| `/api/patients` | POST | Create patient → triggers gRPC + Kafka |
| `/api/patients/{id}` | PUT | Update patient |
| `/api/patients/{id}` | DELETE | Soft delete with timestamps |

**Technology**: Spring Data JPA, PostgreSQL, UUID primary keys, custom validation groups

**Demonstrates**: 
- Domain-driven design (Patient entity + repository pattern)
- Validation framework (Jakarta Bean Validation with custom groups)
- REST API best practices (proper HTTP status codes, DTOs, exception mapping)

---

### 💳 `billing-service` (gRPC Server, Port 9001)
**Business capability:** Billing account provisioning

**Technology**: gRPC, Protocol Buffers, Spring Boot for IoC

**Demonstrates**:
- gRPC service implementation (unary RPC)
- Service-to-service contract enforcement via `.proto` files
- Internal service architecture (no public REST API)

---

### 📈 `analytics-service` (Kafka Consumer, Port 4002)
**Business capability:** Event stream processing and analytics

**Technology**: Kafka consumer groups, Protobuf deserialization, event processing patterns

**Demonstrates**:
- Event-driven architecture (decoupling producers and consumers)
- Kafka consumer configuration (group IDs, offsets, error handling)
- Background job/async processing patterns

---

### 🔐 `auth-service` (JWT Issuer, Port 4005)
**Business capability:** User authentication and token issuance

**Technology**: Spring Security, BCrypt password hashing, H2 in-memory database, Hibernate JPA

**Demonstrates**:
- Security best practices (BCrypt hashing, never storing plaintext passwords)
- Stateless JWT token generation
- Integration testing of authentication flows

---

### 🌉 `api-gateway` (Reverse Proxy, Port 4004)
**Business capability:** API routing and cross-cutting concerns

**Technology**: Spring Cloud Gateway patterns, routing rules, exception handling

**Demonstrates**:
- Edge computing (centralized logging, metrics, exception handling)
- API composition pattern
- Request/response filtering

---

## 🧪 Testing & Quality Assurance

### Integration Tests (`integration-tests` module)
- **Framework**: JUnit 5 (Jupiter), REST-Assured 5.3.0
- **Coverage**: End-to-end authentication flows, patient CRUD operations with token validation
- **Execution**: Tests run against containerized services, validating real inter-service communication

```bash
mvn clean test  # Runs integration tests with proper setup/teardown
```

**Demonstrates**:
- Proper test isolation (services started/stopped per test)
- Testing authentication flows (token generation, validation, expiry)
- RESTful API testing best practices

---

## 🛠️ Technology Stack

| Concern | Technology | Why Chosen |
|---------|-----------|-----------|
| **Language** | Java 21 | Latest LTS, modern language features (records, sealed classes) |
| **Framework** | Spring Boot 3.4.1 | Industry standard, massive ecosystem |
| **Dependency Injection** | Spring Context | Enables loose coupling, testability |
| **Web Framework** | Spring MVC | Battle-tested REST API framework |
| **Data Access** | Spring Data JPA + Hibernate | ORM abstraction, query DSL |
| **Database** | PostgreSQL 17 | ACID compliance, JSON support, mature |
| **Messaging** | Apache Kafka 7.x (KRaft) | Distributed event log, high throughput |
| **RPC Framework** | gRPC + Protobuf 3 | Type-safe, binary-encoded, contract-first |
| **Security** | Spring Security + JJWT | Standards-based auth, JWT token management |
| **Validation** | Jakarta Bean Validation | Declarative input validation, custom groups |
| **API Documentation** | SpringDoc OpenAPI 2.5 | Auto-generated Swagger UI |
| **Build Tool** | Maven 3.9 | Reproducible builds, plugin ecosystem |
| **Containerization** | Docker + Compose | Production-ready deployments |

---

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose (recommended for one-command startup)
- Java 21 (if running locally)
- Maven 3.9+ (if building locally)

### Option 1: Docker Compose (5-minute setup) ⭐ Recommended
```bash
git clone <repo>
cd PatientManagementSystem
docker compose up --build
```

All services start automatically with proper health checks and inter-service connectivity.

### Option 2: Local Development
```bash
# Build entire project
mvn clean install

# Terminal 1: Billing service (requires PostgreSQL + Kafka running)
cd billing-service && mvn spring-boot:run

# Terminal 2: Analytics service  
cd analytics-service && mvn spring-boot:run

# Terminal 3: Auth service
cd auth-service && mvn spring-boot:run

# Terminal 4: Patient service
cd patient-service && mvn spring-boot:run

# Terminal 5: API gateway  
cd api-gateway && mvn spring-boot:run
```

### Service Endpoints Once Running

| Service | URL | Purpose |
|---------|-----|---------|
| Patient Service | http://localhost:4000 | REST API for patient CRUD |
| Swagger UI | http://localhost:4000/swagger-ui.html | Interactive API documentation |
| Auth Service | http://localhost:4005 | JWT token issuance |
| API Gateway | http://localhost:4004 | Unified entry point |
| Analytics Service | http://localhost:4002 | Event consumer (no public API) |
| Kafka UI | http://localhost:8080 | Kafka topic visualization |

---

## 📋 Example Workflow: Creating a Patient

1. **API Call** (port 4004 gateway or 4000 patient service):
```bash
curl -X POST http://localhost:4000/api/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "dateOfBirth": "1990-01-15",
    "address": "123 Healthcare St"
  }'
```

2. **What Happens Behind the Scenes:**
   - ✅ Spring Security validates JWT token
   - ✅ `PatientService` saves patient to PostgreSQL (with UUID)
   - ✅ **Synchronous gRPC call**: `patient-service` ➜ `billing-service` creates billing account
   - ✅ **Asynchronous Kafka publish**: `PatientEvent` published to `patient-events` topic (Protobuf binary)
   - ✅ `analytics-service` automatically consumes event and processes
   - ✅ Response returned to client

**This demonstrates:**
- Microservices orchestration
- Multiple communication patterns (REST, gRPC, Kafka)
- Eventual consistency (analytics updates asynchronously)
- Error handling across service boundaries

---

## 🏆 Design Patterns Implemented

| Pattern | Used For | Location |
|---------|----------|----------|
| **Repository** | Data access abstraction | `patient-service` PatientRepository |
| **Service Layer** | Business logic encapsulation | `patient-service` PatientService |
| **Data Transfer Object (DTO)** | Request/response mapping | PatientRequestDTO, PatientResponseDTO |
| **Global Exception Handler** | Centralized error handling | GlobalExceptionHandler @RestControllerAdvice |
| **Validation Groups** | Conditional validation | CreatePatientValidationGroup |
| **Entity Mapper** | Entity ↔ DTO conversion | PatientMapper (MapStruct ready) |
| **gRPC Stub** | Type-safe service client | BillingServiceGrpcClient |
| **Kafka Producer/Consumer** | Event-driven messaging | KafkaProducer, KafkaConsumer |
| **API Gateway** | Request routing and filtering | api-gateway with Spring Cloud patterns |

---

## 🔐 Security Features

- **JWT Authentication**: Stateless, scalable token-based auth
- **BCrypt Password Hashing**: Salt-based hashing prevents rainbow table attacks
- **Spring Security**: Framework-wide authorization checks
- **Input Validation**: Jakarta Bean Validation prevents injection attacks
- **HTTPS-Ready**: Docker Compose can be extended with TLS

---

## 📈 Production Considerations

### Already Implemented
✅ Docker containerization with multi-stage builds
✅ Health checks in docker-compose
✅ Proper logging configuration  
✅ Input validation and error handling
✅ Integration tests validating deployment

### Future Enhancements (Enterprise-Ready)
- [ ] Distributed tracing (Jaeger/Zipkin)
- [ ] Centralized logging (ELK Stack)
- [ ] Prometheus metrics and alerting
- [ ] Kubernetes deployment manifests
- [ ] Database migrations (Flyway/Liquibase)
- [ ] Circuit breaker pattern (Resilience4j)
- [ ] Caching layer (Redis)

---

## 📚 Project Structure

```
PatientManagementSystem/
├── pom.xml                       # Parent POM (shared versions)
├── docker-compose.yml            # Full stack orchestration
├── 
├── patient-service/              # 🏥 Core patient domain
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/org/example/
│       │   ├── controller/       # REST endpoints
│       │   ├── service/          # Business logic
│       │   ├── repository/       # Spring Data JPA
│       │   ├── model/            # JPA entities
│       │   ├── dto/              # Request/response objects
│       │   ├── mapper/           # Entity conversion
│       │   ├── grpc/             # gRPC client stubs
│       │   ├── kafka/            # Kafka producer
│       │   └── exception/        # Exception handling
│       └── test/                 # Unit + integration tests
│
├── billing-service/              # 💳 gRPC billing service
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/
│   │   ├── java/org/example/grpc/
│   │   └── proto/billing_service.proto
│   └── src/test/
│
├── analytics-service/            # 📊 Kafka event consumer
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/
│   │   ├── java/org/example/
│   │   └── proto/patient_event.proto
│   └── src/test/
│
├── auth-service/                 # 🔐 JWT token issuer
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/
│   │   ├── java/org/example/
│   │   └── resources/
│   │       ├── data.sql          # Seed data
│   │       └── application.properties
│   └── src/test/
│
├── api-gateway/                  # 🌉 API gateway
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/
│   │   ├── java/org/example/
│   │   │   ├── exception/
│   │   │   └── filter/
│   │   └── resources/application.yml
│   └── src/test/
│
├── integration-tests/            # 🧪 End-to-end tests
│   ├── pom.xml
│   └── src/test/java/
│       ├── AuthIntegrationTest.java
│       └── PatientIntegrationTest.java
│
└── infrastructure/               # 🏗️ IaC (future: Terraform, CDK)
    └── pom.xml
```

---

## 🎓 Learning Outcomes

By studying this codebase, you'll understand:

1. **Microservices Architecture**
   - Service decomposition and domain boundaries
   - Independent deployability and scaling
   - Service communication patterns

2. **Spring Boot Ecosystem**
   - Spring Data JPA and ORM patterns
   - Spring Security and authentication/authorization
   - Spring MVC REST API development
   - Dependency injection and IoC containers

3. **Distributed Systems**
   - Synchronous (gRPC + Protobuf)
   - Asynchronous (Kafka event streams)
   - Trade-offs between patterns

4. **Enterprise Software Engineering**
   - SOLID principles
   - Design patterns (Repository, DTO, Service, etc.)
   - Testing strategies (unit, integration)
   - Docker containerization

5. **Production-Grade Practices**
   - Multi-module Maven projects
   - Proper error handling
   - Validation and input sanitization
   - API documentation (OpenAPI)

---

## 💡 Interview Talking Points

**"Tell me about a complex project you've worked on..."**

This project demonstrates:
- ✅ **System Design Skills**: Explain why you chose microservices, gRPC vs REST, Kafka for async
- ✅ **Engineering Maturity**: Containerization, testing, following architectural patterns
- ✅ **Full Stack Development**: Backend, database, messaging, API design
- ✅ **Problem-Solving**: Handle trade-offs (eventual consistency, service isolation, etc.)
- ✅ **Code Quality**: Clean code, SOLID principles, proper separation of concerns

**Key Discussion Points:**
- "Why gRPC instead of REST for inter-service communication?"
  - Answer: Type safety, binary encoding, contract enforced at compile time, ~10x faster
  
- "How do you handle service failures?"
  - Answer: Async Kafka means analytics failure doesn't impact patient registration; could add circuit breakers
  
- "How would you scale this?"
  - Answer: Services are independently deployable; Kafka enables horizontal scaling of analytics
  
- "Why Kafka instead of direct API calls?"
  - Answer: Loose coupling, services can scale independently, replay capability for debugging

---

## 📝 License

MIT License - Feel free to use for portfolio, learning, or interview preparation.

---

## 🤝 Contributing

This is a portfolio/learning project. Feel free to:
- Clone and extend with additional features
- Add Kubernetes manifests for production deployment
- Implement circuit breaker pattern with Resilience4j
- Add caching with Redis
- Deploy to AWS/Azure and add CDK/Terraform code

---

**Created by**: Abbas  
**Purpose**: Demonstrating enterprise backend engineering for career growth  
**Last Updated**: March 2026

---

## 📞 Quick Links

- 📖 [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- 📖 [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- 📖 [gRPC Java Guide](https://grpc.io/docs/languages/java/)
- 📖 [Protocol Buffers Guide](https://developers.google.com/protocol-buffers)
- 📖 [Docker Compose Reference](https://docs.docker.com/compose/)
