# Technology Stack

## Overview

This document locks the technologies used to build and run VibeUs. Choices favor **industry-standard Java microservice tooling** that runs reliably on a developer laptop via Docker Compose.

Companion documents cover [architecture overview](overview.md), [service boundaries](service-boundaries.md), and [deployment topology](deployment-topology.md).

---

## Stack Summary

| Layer | Technology | Version (target) |
|-------|------------|------------------|
| Language | Java | 21 (LTS) |
| Application framework | Spring Boot | 3.5.x |
| Cloud abstractions | Spring Cloud | 2025.0.x |
| API gateway | Spring Cloud Gateway | via Spring Cloud BOM |
| HTTP clients | Spring Cloud OpenFeign | via Spring Cloud BOM |
| Security | Spring Security + JWT (JJWT) | Boot-managed + JJWT 0.12.x |
| Persistence | Spring Data JPA + Hibernate | Boot-managed |
| Database | PostgreSQL | 17 |
| Cache (infra only in MVP) | Redis | 8 |
| Object storage | MinIO | latest stable image |
| Messaging | Apache Kafka + Spring Kafka | KRaft single-node (local) |
| Kafka tooling | Kafka UI (Provectus) | latest stable image |
| Build | Maven + Maven Wrapper | 3.9+ |
| Boilerplate reduction | Lombok | Boot-managed |
| Container runtime | Docker + Docker Compose | Engine 24+ recommended |

---

## Application Runtime

### Java 21

- **Why:** Current LTS with modern language features (records, pattern matching, virtual threads available for future use).
- **Usage:** All four services compile and run on Java 21.
- **Configuration:** `<java.version>21</java.version>` in each service `pom.xml`.

### Spring Boot 3.5.x

- **Why:** Mature ecosystem, auto-configuration, actuator, validation, and security integrations.
- **Parent POM:** `spring-boot-starter-parent` at `3.5.15` (or latest 3.5 patch at implementation time).

### Spring Cloud 2025.0.x

- **Why:** Aligns Gateway, OpenFeign, and dependency BOM with Boot 3.5.
- **Import:** `spring-cloud-dependencies` BOM in each service that needs cloud components.
- **Note:** Gateway may use a slightly newer cloud patch (e.g. `2025.0.3`) if required for Gateway compatibility — keep documented in service POMs.

---

## Service-Specific Dependencies

### gateway-service

| Dependency | Purpose |
|------------|---------|
| `spring-cloud-starter-gateway` | Reactive gateway routing and filters |
| `spring-boot-starter-actuator` | Health checks |
| JJWT (api, impl, jackson) | Parse and validate JWT at the edge |
| `spring-boot-configuration-processor` | Type-safe configuration metadata |

**Does not use:** JPA, PostgreSQL driver, Kafka, MinIO.

---

### auth-service

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-web` | REST controllers |
| `spring-boot-starter-data-jpa` | Credential persistence |
| `spring-boot-starter-security` | Password encoding, security filter chain |
| `spring-boot-starter-validation` | Request DTO validation |
| `spring-cloud-starter-openfeign` | HTTP client to user service on registration |
| `spring-boot-starter-actuator` | Health checks |
| `postgresql` | JDBC driver |
| JJWT (api, impl, jackson) | Token generation and signing |
| `lombok` | Entity/DTO boilerplate |

**Optional in MVP:** `spring-kafka` if auth publishes events later; MVP event is produced by **user service**.

---

### user-service

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-web` | REST + internal controllers |
| `spring-boot-starter-data-jpa` | Profile persistence |
| `spring-boot-starter-security` | JWT-aware request context |
| `spring-boot-starter-validation` | DTO validation |
| `spring-boot-starter-actuator` | Health checks |
| `spring-kafka` | Publish `UserRegisteredEvent` |
| `postgresql` | JDBC driver |
| `lombok` | Entity/DTO boilerplate |

---

### music-service

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-web` | REST + multipart uploads |
| `spring-boot-starter-data-jpa` | Catalog persistence |
| `spring-boot-starter-security` | JWT-aware endpoints |
| `spring-boot-starter-validation` | DTO validation |
| `spring-boot-starter-actuator` | Health checks |
| `minio` (Java SDK) | S3-compatible object storage client |
| `postgresql` | JDBC driver |
| `lombok` | Entity/DTO boilerplate |

---

## Security & Tokens

| Component | Choice |
|-----------|--------|
| Password hashing | BCrypt via `PasswordEncoder` |
| Token format | JWT (signed, with expiry) |
| Library | JJWT `0.12.x` (api + impl + jackson modules) |
| Session model | Stateless — no server-side HTTP sessions |
| Gateway validation | Verify signature and `exp` before route forward |

**Secret management:** `JWT_SECRET` (or equivalent) supplied via environment variable — minimum length enforced at startup in implementation.

---

## Data Layer

| Component | Choice | Notes |
|-----------|--------|-------|
| RDBMS | PostgreSQL 17 | Single instance; multiple logical databases |
| ORM | Hibernate via Spring Data JPA | `ddl-auto: update` acceptable for local MVP |
| Migrations | JPA auto-DDL (MVP) | Flyway/Liquibase post-MVP |
| IDs | UUID | User id from auth; generated UUIDs for catalog entities |
| Auditing | `BaseEntity` | `createdAt`, `updatedAt` on persisted entities |

### Database instances (logical)

| Database | Service |
|----------|---------|
| `vibeus_auth_db` | auth-service |
| `vibeus_user_db` | user-service |
| `vibeus_music_db` | music-service |

---

## Object Storage

| Component | Choice |
|-----------|--------|
| Product | MinIO |
| API | S3-compatible |
| Java client | `io.minio:minio` |
| Buckets | Separate buckets for artists, albums, tracks (names configurable) |
| Local endpoints | API `9000`, Console `9001` |

---

## Messaging

| Component | Choice |
|-----------|--------|
| Broker | Apache Kafka (KRaft mode, single broker for local dev) |
| Spring integration | `spring-kafka` |
| Serialization | JSON (Jackson) for event payloads |
| MVP producer | user-service → `UserRegisteredEvent` |
| MVP consumers | None required |
| Developer UI | Kafka UI on port `8080` |

**Local broker address:** `localhost:9092` from host; `vibeus-kafka:29092` (internal listener) from other containers.

---

## Caching (Infrastructure Only)

| Component | Choice | MVP usage |
|-----------|--------|-----------|
| Redis | Redis 8 image | Container started in compose; **no application integration** in MVP |

Redis is provisioned for future hot-read caching without blocking MVP delivery.

---

## API & Integration

| Concern | Choice |
|---------|--------|
| Public API style | REST over HTTP/JSON |
| Versioning | URI prefix `/api/v1` |
| Sync service calls | OpenFeign (auth → user) or `RestTemplate`/`RestClient` |
| Async integration | Kafka topics |
| Response envelope | `ApiResponse<T>` / `ErrorResponse` per service |
| File upload | `multipart/form-data` |

---

## Build & Project Metadata

| Setting | Value |
|---------|-------|
| Build tool | Maven |
| Wrapper | `mvnw` / `mvnw.cmd` per service |
| Group ID | `com.vibeus` |
| Artifact IDs | `gateway-service`, `auth-service`, `user-service`, `music-service` |
| Packaging | Executable JAR per service |
| SNAPSHOT version | `0.0.1-SNAPSHOT` until first release tag |

---

## Infrastructure Images (Docker Compose)

| Service | Image | Host port(s) |
|---------|-------|--------------|
| PostgreSQL | `postgres:17` | `5433` → `5432` |
| Redis | `redis:8` | `6379` |
| MinIO | `minio/minio` | `9000`, `9001` |
| Kafka | `apache/kafka:latest` | `9092` |
| Kafka UI | `provectuslabs/kafka-ui:latest` | `8080` |

Container names use `vibeus-` prefix (e.g. `vibeus-postgres`, `vibeus-kafka`).

---

## Application Service Ports

| Service | Port |
|---------|------|
| gateway-service | 8081 |
| auth-service | 8082 |
| user-service | 8083 |
| music-service | 8084 |

---

## Development Tooling

| Tool | Purpose |
|------|---------|
| `spring-boot-devtools` | Optional hot reload during local dev |
| Spring Boot Actuator | `/actuator/health` smoke checks |
| Kafka UI | Inspect topics and messages |
| MinIO Console | Browse buckets and uploaded objects |
| Postman / curl | Manual API testing through gateway |

**Post-MVP:** Testcontainers, OpenAPI/Swagger, centralized logging, CI pipelines.

---

## Explicitly Not in MVP Stack

| Technology | Reason deferred |
|------------|-----------------|
| Kubernetes | Compose sufficient for learning scope |
| Service mesh | Unnecessary local complexity |
| Elasticsearch | Search service out of scope |
| GraphQL | REST meets MVP clients |
| gRPC | REST keeps debugging simple |
| Flyway/Liquibase | JPA DDL adequate for first iteration |
| Spring Cloud Config | Environment variables per service |

---

## Version Alignment Checklist

At implementation time, verify:

- [ ] Spring Boot parent version consistent (or intentionally documented per service)
- [ ] Spring Cloud BOM compatible with Boot version
- [ ] JJWT version explicit in gateway and auth POMs
- [ ] PostgreSQL JDBC driver from Boot BOM
- [ ] MinIO SDK version pinned in music-service POM
- [ ] Java 21 enforced in compiler plugin settings

---

## Related Documents

| Document | Link |
|----------|------|
| Architecture overview | [overview.md](overview.md) |
| Service boundaries | `service-boundaries.md` (upcoming) |
| Communication patterns | `communication-patterns.md` (upcoming) |
| Deployment topology | `deployment-topology.md` (upcoming) |
| Naming conventions | `conventions.md` (upcoming) |
| MVP scope | [mvp-scope.md](../requirements/mvp-scope.md) |
